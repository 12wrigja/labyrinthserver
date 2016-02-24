package edu.cwru.eecs395_s16.core;

import com.corundumstudio.socketio.BroadcastOperations;
import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.UnauthorizedActionException;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.maps.FromJSONGameMap;
import edu.cwru.eecs395_s16.interfaces.Jsonable;
import edu.cwru.eecs395_s16.interfaces.objects.GameAction;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;
import edu.cwru.eecs395_s16.utils.JSONDiff;
import edu.cwru.eecs395_s16.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by james on 1/19/16.
 */
public class Match implements Jsonable {

    private final TimerTask pingTask;

    //Players
    private static final String PLAYER_OBJ_KEY = "players";
    private static final String HERO_PLAYER_KEY = "heroes";
    private static final String ARCHITECT_PLAYER_KEY = "architect";
    private final Player heroPlayer;
    private final Player architectPlayer;

    private final Set<Player> spectators;

    //Match Identifier
    private final UUID matchIdentifier;
    private static final String MATCH_ID_KEY = "match_identifier";

    //Game Map
    private final GameMap gameMap;
    private static final String GAME_MAP_KEY = "map";

    //Game state
    private GameState gameState;
    private static final String GAME_STATE_KEY = "game_state";

    //Board Object collection
    private final GameObjectCollection boardObjects;
    private static final String BOARD_COLLECTION_KEY = "board_objects";


    //Sequence Numbers for actions
    private static final String GAME_SEQUENCE_KEY = ":SEQUENCE:";
    private static final String CURRENT_SEQUENCE_CACHE_KEY = ":CURRENTSEQUENCE";
    private static final String CURRENT_SEQUENCE_JSON_KEY = "current_sequence";
    private static final int INITIAL_SEQUENCE_NUMBER = 0;
    private int gameSequenceID = INITIAL_SEQUENCE_NUMBER;

    //TODO Turn numbers

    public static Match InitNewMatch(Player heroPlayer, Player dmPlayer, GameMap gameMap) {
        //TODO check and see if either specified player is already in a match
        UUID randMatchID = UUID.randomUUID();
        Match m = new Match(heroPlayer, dmPlayer, randMatchID, gameMap);
        m.startInitialGameTasks();
        return m;
    }

    public static Optional<Match> fromCacheWithMatchIdentifier(UUID id) {
        //This method is used to retrieve the match from a cache
        CacheService cache = GameEngine.instance().getCacheService();
        Optional<String> temp = cache.getString(id.toString() + CURRENT_SEQUENCE_CACHE_KEY);
        Optional<String> base = cache.getString(id.toString() + GAME_SEQUENCE_KEY + INITIAL_SEQUENCE_NUMBER);
        if (temp.isPresent() && base.isPresent()) {
            int latestSequenceNumber = Integer.parseInt(temp.get());
            JSONObject matchData;
            try {
                matchData = new JSONObject(base.get());
                for (int i = INITIAL_SEQUENCE_NUMBER + 1; i <= latestSequenceNumber; i++) {
                    Optional<String> snapshot = cache.getString(id.toString() + GAME_SEQUENCE_KEY + i);
                    JSONObject jSnapshot = new JSONObject(snapshot.get());
                    JSONObject stateChanges = (JSONObject) jSnapshot.get("new_state");
                    JSONDiff jDiff = new JSONDiff((JSONObject) stateChanges.get("removed"), (JSONObject) stateChanges.get("added"), (JSONObject) stateChanges.get("changed"));
                    matchData = JSONUtils.patch(matchData, jDiff);
                }

                //Retrieve players
                JSONObject players = (JSONObject) matchData.get(PLAYER_OBJ_KEY);
                Optional<Player> heroPlayer = GameEngine.instance().getSessionRepository().findPlayer(players.getString(HERO_PLAYER_KEY));
                Optional<Player> architectPlayer = GameEngine.instance().getSessionRepository().findPlayer(players.getString(ARCHITECT_PLAYER_KEY));

                //Retrieve Map
                GameMap mp = new FromJSONGameMap((JSONObject) matchData.get("map"));

                //Build match as we have all the basics we need
                Match m;
                if (heroPlayer.isPresent() && architectPlayer.isPresent()) {
                    m = new Match(heroPlayer.get(), architectPlayer.get(), id, mp);
                } else {
                    return Optional.empty();
                }

                //Retrieve Game State
                m.gameState = GameState.valueOf(matchData.getString(GAME_STATE_KEY).toUpperCase());
                m.gameSequenceID = latestSequenceNumber;

                //Retrieve Game Board Objects
                JSONObject gameObjectCollectionData = matchData.getJSONObject(BOARD_COLLECTION_KEY);
                m.boardObjects.fillFromJSONData(gameObjectCollectionData);

                return Optional.of(m);
            } catch (Exception e) {
                if (GameEngine.instance().IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Match> fromCacheWithMatchIdentifier(String id) {
        UUID uuidID = UUID.fromString(id);
        return fromCacheWithMatchIdentifier(uuidID);
    }

    private Match(Player heroPlayer, Player architectPlayer, UUID matchIdentifier, GameMap gameMap) {
        this.heroPlayer = heroPlayer;
        this.architectPlayer = architectPlayer;
        this.matchIdentifier = matchIdentifier;
        this.gameMap = gameMap;
        this.boardObjects = new GameObjectCollection();

        pingTask = new TimerTask() {
            @Override
            public void run() {
                GameEngine.instance().getBroadcastServiceForRoom(matchIdentifier.toString()).sendEvent("room_ping", "You are in room " + matchIdentifier.toString());
            }
        };

        spectators = new HashSet<>(5);
    }

    private void startInitialGameTasks() {
        GameEngine.instance().getGameTimer().scheduleAtFixedRate(pingTask, 0, 1000);
        this.heroPlayer.getClient().joinRoom(matchIdentifier.toString());
        this.architectPlayer.getClient().joinRoom(matchIdentifier.toString());
        this.heroPlayer.setCurrentMatch(Optional.of(this.matchIdentifier));
        this.architectPlayer.setCurrentMatch(Optional.of(this.matchIdentifier));
        this.gameState = GameState.HERO_TURN;
        this.gameSequenceID = INITIAL_SEQUENCE_NUMBER;

        //TODO update to give the heros proper starting locations based on the map
        List<Hero> heroPlayerHeroes = GameEngine.instance().getHeroRepository().getPlayerHeroes(this.heroPlayer);
        this.boardObjects.addAll(heroPlayerHeroes);

        //TODO update to add all the architect's monsters and traps to the board instead of their heroes
        List<Hero> architectPlayerHeroes = GameEngine.instance().getHeroRepository().getPlayerHeroes(this.architectPlayer);
        this.boardObjects.addAll(architectPlayerHeroes);
        setCurrentSequence(this.gameSequenceID);
        JSONObject matchBaseline = this.getJSONRepresentation();
        storeSnapshotForSequence(this.gameSequenceID, matchBaseline);
        broadcastToAllParties("match_found", matchBaseline);
    }

    public synchronized boolean updateGameState(Player p, GameAction action) throws UnauthorizedActionException, InvalidGameStateException {
        //First check and see if it is your turn
        if (isPlayerTurn(p)) {
            //Assemble the required lists of stuff.
            action.checkCanDoAction(this.gameMap, this.boardObjects);
            JSONObject matchState = this.getJSONRepresentation();
            action.doGameAction(this.gameMap, this.boardObjects);
            this.gameSequenceID++;
            setCurrentSequence(this.gameSequenceID);
            JSONObject newMatchState = this.getJSONRepresentation();
            JSONObject matchDifferences = JSONUtils.getDiff(matchState, newMatchState).asJSONObject();
            JSONObject gameUpdate = new JSONObject();
            try {
                gameUpdate.put("action", action.getJSONRepresentation());
                gameUpdate.put("new_state", matchDifferences);
            } catch (JSONException e) {
                //This should never happen as all the keys are not null
            }
            storeSnapshotForSequence(this.gameSequenceID, gameUpdate);
            broadcastToAllParties("game_update", gameUpdate);
            return true;
        } else {
            throw new UnauthorizedActionException(p);
        }
    }

    private void setCurrentSequence(int sequence){
        GameEngine.instance().getCacheService().storeString(this.matchIdentifier.toString() + CURRENT_SEQUENCE_CACHE_KEY, "" + sequence);
    }

    private void storeSnapshotForSequence(int sequenceNumber, JSONObject snapshot) {
        GameEngine.instance().getCacheService().storeString(this.matchIdentifier + GAME_SEQUENCE_KEY + sequenceNumber, snapshot.toString());
    }

    private boolean isPlayerTurn(Player p) {
        return ((p.equals(heroPlayer) && gameState == GameState.HERO_TURN)
                ||
                (p.equals(architectPlayer) && gameState == GameState.ARCHITECT_TURN));
    }

    public void broadcastToAllParties(String event, JSONObject obj) {
        BroadcastOperations roomBroadcast = GameEngine.instance().getBroadcastServiceForRoom(this.matchIdentifier.toString());
        roomBroadcast.sendEvent(event, obj);
    }

    public void addSpectator(Player spectator) {
        synchronized (this.spectators) {
            if (!this.spectators.contains(spectator)) {
                this.spectators.add(spectator);
                spectator.setCurrentMatch(Optional.of(this.matchIdentifier));
            }
        }
    }

    public void removeSpectator(Player spectator) {
        synchronized (this.spectators) {
            this.spectators.remove(spectator);
            spectator.setCurrentMatch(Optional.empty());
        }
    }

    public boolean isSpectatorOfMatch(Player spectator) {
        synchronized (this.spectators) {
            return this.spectators.contains(spectator);
        }
    }

    public void cleanup() {
        this.heroPlayer.setCurrentMatch(Optional.empty());
        this.architectPlayer.setCurrentMatch(Optional.empty());
        pingTask.cancel();
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject obj = new JSONObject();
        try {
            //Match ID
            obj.put(MATCH_ID_KEY, this.matchIdentifier.toString());

            //Players involved
            JSONObject playerObj = new JSONObject();
            playerObj.put(HERO_PLAYER_KEY, this.heroPlayer.getUsername());
            playerObj.put(ARCHITECT_PLAYER_KEY, this.architectPlayer.getUsername());
            obj.put(PLAYER_OBJ_KEY, playerObj);

            //Map
            obj.put(GAME_MAP_KEY, this.gameMap.getJSONRepresentation());

            //Board objects
            obj.put(BOARD_COLLECTION_KEY, this.boardObjects.getJSONRepresentation());

            //Latest sequence identifier
            obj.put(CURRENT_SEQUENCE_JSON_KEY, this.gameSequenceID);

            //Current game state
            obj.put(GAME_STATE_KEY, this.gameState.toString().toLowerCase());

        } catch (JSONException e) {
            //Never will be thrown as the keys are never null
        }
        return obj;
    }
}
