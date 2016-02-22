package edu.cwru.eecs395_s16.core;

import com.corundumstudio.socketio.BroadcastOperations;
import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.UnauthorizedActionException;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.core.objects.RandomlyGeneratedGameMap;
import edu.cwru.eecs395_s16.interfaces.Jsonable;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.objects.*;
import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by james on 1/19/16.
 */
public class Match implements Jsonable {

    private final TimerTask pingTask;

    private static final String HERO_PLAYER_KEY = ":HeroPlayer";
    private final Player heroPlayer;
    private static final String ARCHITECT_PLAYER_KEY = ":ArchitectPlayer";
    private final Player architectPlayer;

    private final Set<Player> spectators;

    private final UUID matchIdentifier;

    private final GameMap gameMap;

    private GameState gameState;

    private final List<GameObject> boardObjects;

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

        //Retrieve players
        Optional<String> heroPlayerIdentifier = cache.getString(id.toString() + HERO_PLAYER_KEY);
        Optional<String> architectPlayerIdentifier = cache.getString(id.toString() + ARCHITECT_PLAYER_KEY);

        Player heroPlayer;
        Player architectPlayer;

        if (heroPlayerIdentifier.isPresent()) {
            Optional<Player> hOpt;
            try {
                hOpt = GameEngine.instance().getPlayerRepository().findPlayer(heroPlayerIdentifier.get());
            } catch (UnknownUsernameException e) {
                if(GameEngine.instance().IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
                return Optional.empty();
            }
            if (hOpt.isPresent()) {
                heroPlayer = hOpt.get();
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }

        if (architectPlayerIdentifier.isPresent()) {
            Optional<Player> aOpt;
            try {
                aOpt = GameEngine.instance().getPlayerRepository().findPlayer(architectPlayerIdentifier.get());
            } catch (UnknownUsernameException e) {
                if(GameEngine.instance().IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
                return Optional.empty();
            }
            if (aOpt.isPresent()) {
                architectPlayer = aOpt.get();
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }

        //Retrieve Map

        //Retrieve Game State

        //TODO properly retrieve and setup game state here.
        Match m = new Match(heroPlayer, architectPlayer, id, new RandomlyGeneratedGameMap(5, 5));
        return Optional.of(m);
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
        this.boardObjects = new ArrayList<>();

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
        this.gameState = GameState.GAME_START;

        //TODO update to add all the base game objects to the board.
        this.boardObjects.addAll(GameEngine.instance().getHeroRepository().getPlayerHeroes(this.heroPlayer));
        this.boardObjects.addAll(GameEngine.instance().getHeroRepository().getPlayerHeroes(this.architectPlayer));

        Response r = new Response();
        r.setKey("match-id",this.matchIdentifier.toString());
        r.setDeepKey(this.heroPlayer.getUsername(),"players","heroes");
        r.setDeepKey(this.architectPlayer.getUsername(),"players","architect");
        r.setKey("map",this.gameMap.getJSONRepresentation());
        r.setKey("board_objects",this.boardObjects);
        broadcastToAllParties("match_found",r);
    }

    //TODO figure out what inputs go here
    //Maybe some sort of action class?
    public synchronized <T extends RequestData> boolean  updateGameState(Player p, GameAction action) throws UnauthorizedActionException {
        //First check and see if it is your turn
        if (isPlayerTurn(p)){
            //Assemble the required lists of stuff.
            if(action.canDoAction(this.gameMap, this.boardObjects)){
                action.doGameAction(this.gameMap, this.boardObjects);
                //TODO persist new game state here
                //TODO Send results out to all players and spectators
                return true;
            } else {
                return false;
            }
        } else {
            throw new UnauthorizedActionException(p);
        }

    }

    private boolean isPlayerTurn(Player p) {
        return ((p.equals(heroPlayer) && gameState == GameState.HERO_TURN)
                ||
                (p.equals(architectPlayer) && gameState == GameState.ARCHITECT_TURN));
    }

    public void broadcastToAllParties(String event, Jsonable object) {
        BroadcastOperations roomBroadcast = GameEngine.instance().getBroadcastServiceForRoom(this.matchIdentifier.toString());
        roomBroadcast.sendEvent(event, object.getJSONRepresentation());
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

    public boolean isSpectatorOfMatch(Player spectator){
        synchronized (this.spectators){
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
        try{
            //Match ID
            obj.put("match-id",this.matchIdentifier.toString());

            //Players involved
            JSONObject playerObj = new JSONObject();
            playerObj.put("heroes",this.heroPlayer.getUsername());
            playerObj.put("architect",this.architectPlayer.getUsername());
            obj.put("players",playerObj);

            //Map
            obj.put("map",this.gameMap.getJSONRepresentation());

            //Board objects
            JSONArray boardObjectArray = new JSONArray();
            for(GameObject gObj : this.boardObjects){
                boardObjectArray.put(gObj.getJSONRepresentation());
            }
            obj.put("board_objects",boardObjectArray);

            //Current game state
            obj.put("game_state",this.gameState.toString());

        }catch (JSONException e){
            //Never will be thrown as the keys are never null
        }
        return obj;
    }
}
