package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.UnauthorizedActionException;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.maps.FromJSONGameMap;
import edu.cwru.eecs395_s16.interfaces.Jsonable;
import edu.cwru.eecs395_s16.interfaces.objects.GameAction;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
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

    public static InternalResponseObject<Match> InitNewMatch(Player heroPlayer, Player dmPlayer, GameMap gameMap) {
        if (heroPlayer.getCurrentMatchID().isPresent() || dmPlayer.getCurrentMatchID().isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.PLAYER_BUSY);
        } else {
            UUID randMatchID = UUID.randomUUID();
            Match m = new Match(heroPlayer, dmPlayer, randMatchID, gameMap);
            InternalResponseObject<?> resp = m.startInitialGameTasks();
            if(!resp.isNormal()){
                return InternalResponseObject.cloneError(resp);
            } else {
                return new InternalResponseObject<>(m, "match");
            }
        }
    }

    public static InternalResponseObject<Match> fromCacheWithMatchIdentifier(UUID id) {
        //This method is used to retrieve the match from a cache
        CacheService cache = GameEngine.instance().services.cacheService;
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
                InternalResponseObject<Player> heroRetrievalResponse = GameEngine.instance().services.sessionRepository.findPlayer(players.getString(HERO_PLAYER_KEY));
                if(!heroRetrievalResponse.isNormal()){
                    return InternalResponseObject.cloneError(heroRetrievalResponse,"Unable to find the hero player for the match.");
                }
                InternalResponseObject<Player> architectRetrievalResponse = GameEngine.instance().services.sessionRepository.findPlayer(players.getString(ARCHITECT_PLAYER_KEY));
                if(!architectRetrievalResponse.isNormal()){
                    return InternalResponseObject.cloneError(architectRetrievalResponse, "Unable to find the architect player for the match");
                }

                //Retrieve Map
                GameMap mp = new FromJSONGameMap((JSONObject) matchData.get("map"));

                //Build match as we have all the basics we need
                Match m;
                if (heroRetrievalResponse.isPresent() && architectRetrievalResponse.isPresent()) {
                    m = new Match(heroRetrievalResponse.get(), architectRetrievalResponse.get(), id, mp);
                } else {
                    return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR,InternalErrorCode.MISSING_PLAYER, "One of the two players in the match are missing.");
                }

                //Retrieve Game State
                m.gameState = GameState.valueOf(matchData.getString(GAME_STATE_KEY).toUpperCase());
                m.gameSequenceID = latestSequenceNumber;

                //Retrieve Game Board Objects
                JSONObject gameObjectCollectionData = matchData.getJSONObject(BOARD_COLLECTION_KEY);
                m.boardObjects.fillFromJSONData(gameObjectCollectionData);

                return new InternalResponseObject<>(m,"match");
            } catch (Exception e) {
                if (GameEngine.instance().IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
                return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR);
            }
        } else {
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR,InternalErrorCode.MATCH_RETRIEVAL_ERROR);
        }
    }

    public static InternalResponseObject<Match> fromCacheWithMatchIdentifier(String id) {
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
                GameEngine.instance().broadcastEventForRoom(matchIdentifier.toString(), "room_ping", "You are in room " + matchIdentifier.toString());
            }
        };

        spectators = new HashSet<>(5);
    }

    private InternalResponseObject<Match> startInitialGameTasks() {
        //Schedule the ping task once every second and have the players join the room for the match
        GameEngine.instance().services.gameTimer.scheduleAtFixedRate(pingTask, 0, 1000);
        this.heroPlayer.getClient().get().joinRoom(this.matchIdentifier.toString());
        this.architectPlayer.getClient().get().joinRoom(this.matchIdentifier.toString());

        //Set the player's current match identifiers
        this.heroPlayer.setCurrentMatch(Optional.of(this.matchIdentifier));
        this.architectPlayer.setCurrentMatch(Optional.of(this.matchIdentifier));

        //Set initial turn data
        this.gameState = GameState.HERO_TURN;
        this.gameSequenceID = INITIAL_SEQUENCE_NUMBER;

        //Retrieve heroes for the hero player and place them randomly in the spawn positions
        InternalResponseObject<List<Hero>> heroHeroResponse = GameEngine.instance().services.heroRepository.getPlayerHeroes(this.heroPlayer);
        if(!heroHeroResponse.isNormal()){
            return InternalResponseObject.cloneError(heroHeroResponse);
        }
        List<Hero> heroHeroes = heroHeroResponse.get();
        int numHeroSpaces = this.gameMap.getHeroCapacity();
        int numHeroes = heroHeroes.size();
        List<Location> heroSpawnLocations = this.gameMap.getHeroSpawnLocations();
        Collections.shuffle(heroHeroes);
        int numIterations = Math.min(numHeroes, numHeroSpaces);
        for(int i=0; i<numIterations; i++){
            GameObject hero = heroHeroes.get(i);
            Location newLoc = heroSpawnLocations.get(i);
            hero.setLocation(newLoc);
            this.boardObjects.add(hero);
        }

        //Add all the architect's monsters and traps to the board
        //TODO update to add all the architect's monsters and traps to the board instead of their heroes
        InternalResponseObject<List<Hero>> architectHeroResponse = GameEngine.instance().services.heroRepository.getPlayerHeroes(this.architectPlayer);
        if(!architectHeroResponse.isNormal()){
            return InternalResponseObject.cloneError(architectHeroResponse);
        }
        List<Hero> architectHeroes = architectHeroResponse.get();
        this.boardObjects.addAll(architectHeroes);

        //Take initial snapshots and store them.
        setCurrentSequence(this.gameSequenceID);
        JSONObject matchBaseline = this.getJSONRepresentation();
        storeSnapshotForSequence(this.gameSequenceID, matchBaseline);

        //Broadcast that a match has been found to all interested parties
        broadcastToAllParties("match_found", matchBaseline);
        return new InternalResponseObject<>();
    }

    public synchronized InternalResponseObject<Boolean> updateGameState(Player p, GameAction action) {
        //First check and see if it is your turn
        if (isPlayerTurn(p)) {
            //Assemble the required lists of stuff.
            InternalResponseObject<Boolean> resp = action.checkCanDoAction(this.gameMap, this.boardObjects, p);
            if(!resp.isNormal()){
                return resp;
            }
            JSONObject matchState = this.getJSONRepresentation();
            action.doGameAction(this.gameMap, this.boardObjects);
            JSONObject gameUpdate = takeSnapshotForAction(matchState, action);
            storeSnapshotForSequence(this.gameSequenceID, gameUpdate);
            broadcastToAllParties("game_update", gameUpdate);
            return resp;
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA,InternalErrorCode.NOT_YOUR_TURN);
        }
    }

    public JSONObject takeSnapshot(JSONObject originalState, Object actionReason){
        this.gameSequenceID++;
        setCurrentSequence(this.gameSequenceID);
        JSONObject newMatchState = this.getJSONRepresentation();
        JSONObject matchDifferences = JSONUtils.getDiff(originalState, newMatchState).asJSONObject();
        JSONObject gameUpdate = new JSONObject();
        try {
            gameUpdate.put("action", actionReason);
            gameUpdate.put("new_state", matchDifferences);

        } catch (JSONException e) {
            //This should never happen as all the keys are not null
        }
        return gameUpdate;
    }

    public void takeAndCommitSnapshot(JSONObject originalState, Object actionReason){
        JSONObject diff = takeSnapshot(originalState,actionReason);
        storeSnapshotForSequence(this.gameSequenceID,diff);
    }

    private JSONObject takeSnapshotForAction(JSONObject originaState, GameAction action){
        return takeSnapshot(originaState, action.getJSONRepresentation());
    }

    private void setCurrentSequence(int sequence) {
        GameEngine.instance().services.cacheService.storeString(this.matchIdentifier.toString() + CURRENT_SEQUENCE_CACHE_KEY, "" + sequence);
    }

    private void storeSnapshotForSequence(int sequenceNumber, JSONObject snapshot) {
        GameEngine.instance().services.cacheService.storeString(this.matchIdentifier + GAME_SEQUENCE_KEY + sequenceNumber, snapshot.toString());
    }

    private boolean isPlayerTurn(Player p) {
        return ((p.equals(heroPlayer) && gameState == GameState.HERO_TURN)
                ||
                (p.equals(architectPlayer) && gameState == GameState.ARCHITECT_TURN));
    }

    public void broadcastToAllParties(String event, JSONObject obj) {
        GameEngine.instance().broadcastEventForRoom(this.matchIdentifier.toString(), event, obj);
    }

    public void addSpectator(Player spectator) {
        synchronized (this.spectators) {
            if (!this.spectators.contains(spectator)) {
                this.spectators.add(spectator);
                spectator.getClient().get().joinRoom(this.matchIdentifier.toString());
                spectator.setCurrentMatch(Optional.of(this.matchIdentifier));
            }
        }
    }

    public void removeSpectator(Player spectator) {
        synchronized (this.spectators) {
            this.spectators.remove(spectator);
            spectator.getClient().get().leaveRoom(this.matchIdentifier.toString());
            spectator.setCurrentMatch(Optional.empty());
        }
    }

    public boolean isSpectatorOfMatch(Player spectator) {
        synchronized (this.spectators) {
            return this.spectators.contains(spectator);
        }
    }

    public void end(String reason) {
        //TODO commit match data, update xp, currency, etc
        JSONObject reasonObj = new JSONObject();
        try {
            reasonObj = new JSONObject("{\"reason\":" + reason + "}");
        } catch (JSONException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
        }
        broadcastToAllParties("match_end", reasonObj);
        this.heroPlayer.setCurrentMatch(Optional.empty());
        this.architectPlayer.setCurrentMatch(Optional.empty());
        spectators.forEach(this::removeSpectator);
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

    public UUID getMatchIdentifier() {
        return matchIdentifier;
    }

    public GameObjectCollection getBoardObjects() {
        return boardObjects;
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameMap getGameMap() {
        return gameMap;
    }
}
