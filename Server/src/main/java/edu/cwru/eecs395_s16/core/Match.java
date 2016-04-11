package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.GameObjectCollection;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.maps.FromJSONGameMap;
import edu.cwru.eecs395_s16.core.objects.objectives.GameObjective;
import edu.cwru.eecs395_s16.networking.Jsonable;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.actions.GameAction;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.services.cache.CacheService;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.utils.JSONDiff;
import edu.cwru.eecs395_s16.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by james on 1/19/16.
 */
public class Match implements Jsonable {

    public static final String MATCH_END_KEY = "match_end";
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
    private static final String CURRENT_SNAPSHOT_CACHE_KEY = ":CURRENTSNAPSHOT";
    private static final String CURRENT_SEQUENCE_JSON_KEY = "current_sequence";
    private static final int INITIAL_SEQUENCE_NUMBER = 0;
    private int gameSequenceID = INITIAL_SEQUENCE_NUMBER;

    //TODO Turn numbers
    //Turn numbers
    private static final String TURN_NUMBER_KEY = "turn_number";
    private int turnNumber = 0;

    private static final String MATCH_OBJECTIVE_KEY = "objective";
    private final GameObjective objective;

    //Event Keys
    public static final String GAME_UPDATE_KEY = "game_update";

    public static InternalResponseObject<Match> InitNewMatch(Player heroPlayer, Player dmPlayer, GameMap gameMap, GameObjective objective, Set<UUID> useHeroes, Map<GameObject, Location> initialArchitectLocations) {
        if (heroPlayer.getCurrentMatchID().isPresent() || dmPlayer.getCurrentMatchID().isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.PLAYER_BUSY);
        } else {
            UUID randMatchID = UUID.randomUUID();
            Match m = new Match(heroPlayer, dmPlayer, randMatchID, gameMap, objective);
            InternalResponseObject<?> resp = m.startInitialGameTasks(useHeroes, initialArchitectLocations);
            if (!resp.isNormal()) {
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

        JSONObject matchData;
        if (temp.isPresent()) {
            int latestSequenceNumber = Integer.parseInt(temp.get());
            Optional<String> snapshot = cache.getString(id.toString() + CURRENT_SNAPSHOT_CACHE_KEY);
            if(snapshot.isPresent()){
                try {
                    matchData = new JSONObject(snapshot.get());
                } catch (JSONException e) {
                    if (GameEngine.instance().IS_DEBUG_MODE) {
                        e.printStackTrace();
                    }
                    return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR);
                }
            } else {
                Optional<String> base = cache.getString(id.toString() + GAME_SEQUENCE_KEY + INITIAL_SEQUENCE_NUMBER);
                if (base.isPresent()) {
                    try {
                        matchData = new JSONObject(base.get());
                        for (int i = INITIAL_SEQUENCE_NUMBER + 1; i <= latestSequenceNumber; i++) {
                            Optional<String> change = cache.getString(id.toString() + GAME_SEQUENCE_KEY + i);
                            JSONObject jSnapshot = new JSONObject(change.get());
                            JSONObject stateChanges = (JSONObject) jSnapshot.get("new_state");
                            JSONDiff jDiff = new JSONDiff((JSONObject) stateChanges.get("removed"), (JSONObject) stateChanges.get("added"), (JSONObject) stateChanges.get("changed"));
                            matchData = JSONUtils.patch(matchData, jDiff);
                        }
                    } catch (Exception e) {
                        if (GameEngine.instance().IS_DEBUG_MODE) {
                            e.printStackTrace();
                        }
                        return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR);
                    }
                } else {
                    return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR);
                }
            }

            try {
                //Retrieve players
                JSONObject players = (JSONObject) matchData.get(PLAYER_OBJ_KEY);
                InternalResponseObject<Player> heroRetrievalResponse = GameEngine.instance().services.playerRepository.findPlayer(players.getString(HERO_PLAYER_KEY));
                if (!heroRetrievalResponse.isNormal()) {
                    return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, heroRetrievalResponse.getInternalErrorCode(), "Unable to find the hero player for the match.");
                }
                InternalResponseObject<Player> architectRetrievalResponse = GameEngine.instance().services.playerRepository.findPlayer(players.getString(ARCHITECT_PLAYER_KEY));
                if (!architectRetrievalResponse.isNormal()) {
                    return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, architectRetrievalResponse.getInternalErrorCode(), "Unable to find the architect player for the match");
                }

                //Retrieve Map
                GameMap mp = new FromJSONGameMap((JSONObject) matchData.get("map"));

                //Retrieve Game Objective
                GameObjective objective = GameObjective.objectiveForJSON(matchData.getJSONObject(MATCH_OBJECTIVE_KEY));

                //Build match as we have all the basics we need
                Match m;
                if (heroRetrievalResponse.isPresent() && architectRetrievalResponse.isPresent()) {
                    m = new Match(heroRetrievalResponse.get(), architectRetrievalResponse.get(), id, mp, objective);
                } else {
                    return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.MISSING_PLAYER, "One of the two players in the match are missing.");
                }

                //Retrieve Game State
                m.gameState = GameState.valueOf(matchData.getString(GAME_STATE_KEY).toUpperCase());
                m.gameSequenceID = latestSequenceNumber;
                m.turnNumber = matchData.getInt(TURN_NUMBER_KEY);

                //Retrieve Game Board Objects
                JSONObject gameObjectCollectionData = matchData.getJSONObject(BOARD_COLLECTION_KEY);
                m.boardObjects.fillFromJSONData(gameObjectCollectionData);

                return new InternalResponseObject<>(m, "match");
            } catch (JSONException e){
                if (GameEngine.instance().IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
                return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR);
            }
        } else {
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR);
        }

    }

    public static InternalResponseObject<Match> fromCacheWithMatchIdentifier(String id) {
        UUID uuidID = UUID.fromString(id);
        return fromCacheWithMatchIdentifier(uuidID);
    }

    private Match(Player heroPlayer, Player architectPlayer, UUID matchIdentifier, GameMap gameMap, GameObjective objective) {
        this.heroPlayer = heroPlayer;
        this.architectPlayer = architectPlayer;
        this.matchIdentifier = matchIdentifier;
        this.gameMap = gameMap;
        this.objective = objective;
        this.boardObjects = new GameObjectCollection();

        pingTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    GameEngine.instance().broadcastEventForRoom(matchIdentifier.toString(), "room_ping", "You are in room " + matchIdentifier.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        spectators = new HashSet<>(5);
    }

    private InternalResponseObject<Match> startInitialGameTasks(Set<UUID> pickedHeroes, Map<GameObject, Location> initialArchitectLocations) {
        //Schedule the ping task once every second and have the players join the room for the match
        this.heroPlayer.getClient().get().joinRoom(this.matchIdentifier.toString());
        this.architectPlayer.getClient().get().joinRoom(this.matchIdentifier.toString());
        GameEngine.instance().gameTimer.scheduleAtFixedRate(pingTask, 0, 1000);

        //Set the player's current match identifiers
        this.heroPlayer.setCurrentMatch(Optional.of(this.matchIdentifier));
        this.architectPlayer.setCurrentMatch(Optional.of(this.matchIdentifier));

        //Set initial turn data
        this.gameState = GameState.HERO_TURN;
        this.turnNumber = 1;
        this.gameSequenceID = INITIAL_SEQUENCE_NUMBER;

        //Retrieve heroes for the hero player and place them randomly in the spawn positions
        int numHeroSpaces = this.gameMap.getHeroCapacity();
        if(pickedHeroes != null && pickedHeroes.size() != numHeroSpaces){
            return new InternalResponseObject<>(InternalErrorCode.INCORRECT_INITIAL_HERO_SETUP,"Not enough heroes were selected to create a match on this map.");
        }

        InternalResponseObject<List<Hero>> heroHeroResponse = GameEngine.instance().services.heroRepository.getPlayerHeroes(this.heroPlayer);
        if (!heroHeroResponse.isNormal()) {
            return InternalResponseObject.cloneError(heroHeroResponse);
        }
        List<Hero> heroHeroes;
        if(pickedHeroes != null) {
            heroHeroes = heroHeroResponse.get().stream().filter(h -> pickedHeroes.contains(h.getGameObjectID())).collect(Collectors.toList());
        } else {
            heroHeroes = heroHeroResponse.get();
        }

        int numHeroes = heroHeroes.size();
        List<Location> heroSpawnLocations = this.gameMap.getHeroSpawnLocations();
        Collections.shuffle(heroHeroes);
        int numIterations = Math.min(numHeroes, numHeroSpaces);
        for (int i = 0; i < numIterations; i++) {
            GameObject hero = heroHeroes.get(i);
            Location newLoc = heroSpawnLocations.get(i);
            hero.setLocation(newLoc);
            this.boardObjects.add(hero);
        }

        //Add all the architect's monsters and traps to the board
        //TODO update to add all the architect's monsters and traps to the board instead of their heroes
        InternalResponseObject<List<Hero>> architectHeroResponse = GameEngine.instance().services.heroRepository.getPlayerHeroes(this.architectPlayer);
        if (!architectHeroResponse.isNormal()) {
            return InternalResponseObject.cloneError(architectHeroResponse);
        }
        List<Hero> architectHeroes = architectHeroResponse.get();
        int numArchitectHeroes = architectHeroes.size();
        List<Location> architectSpawnLocations = gameMap.getArchitectCreatureSpawnLocations();
        int numArchitectObjectLocations = architectSpawnLocations.size();
        numIterations = Math.min(numArchitectHeroes, numArchitectObjectLocations);
        Collections.shuffle(architectHeroes);
        for (int i = 0; i < numIterations; i++) {
            Creature obj = architectHeroes.get(i);
            Location spawnLoc = architectSpawnLocations.get(i);
            obj.setLocation(spawnLoc);
            this.boardObjects.add(obj);
        }

        //Add in an objective if the map calls for one
        objective.setup(this);

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
            InternalResponseObject<Boolean> resp = action.checkCanDoAction(this, this.gameMap, this.boardObjects, p);
            if (!resp.isNormal()) {
                return resp;
            }
            doAndSnapshot(action.getJSONRepresentation(), () ->
                    action.doGameAction(this.gameMap, this.boardObjects), true);
            GameObjective.GAME_WINNER winner = objective.checkForGameEnd(this);
            if(winner != GameObjective.GAME_WINNER.NO_WINNER){
                end(winner.toString().toLowerCase());
            } else {
                //Check the current player's creatures and see if they are all exhausted - if so the turn swaps
                long numNotExhausted = boardObjects.getForPlayerOwner(p).stream().filter(obj -> obj instanceof Creature && ((Creature) obj).getActionPoints() > 0).count();
                if (numNotExhausted == 0) {
                    JSONObject turnEndObj = new JSONObject();
                    try {
                        turnEndObj.put("type","turn_end");
                    } catch (JSONException e){
                        //
                    }
                    doAndSnapshot(turnEndObj, this::swapSides, true);
                }
            }
            return resp;
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.NOT_YOUR_TURN);
        }
    }

    public void doAndSnapshot(Object actionReason, Runnable method, boolean broadcast) {
        JSONObject matchState = this.getJSONRepresentation();
        method.run();
        JSONObject gameUpdate = takeSnapshot(matchState, actionReason);
        storeSnapshotForSequence(this.gameSequenceID, gameUpdate);
        if (broadcast) {
            broadcastToAllParties(GAME_UPDATE_KEY, gameUpdate);
        }
    }

    public void modify(JSONObject input) {
        String reason = "manual_modification";
        JSONObject currentState = this.getJSONRepresentation();
        JSONObject modifiedState = this.getJSONRepresentation();
        //Manually modify the JSON
        String key;
        Iterator keyIterator = input.keys();
        while (keyIterator.hasNext()) {
            key = (String) keyIterator.next();
            UUID keyID;
            try {
                keyID = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }
            Optional<GameObject> obj = boardObjects.getByID(keyID);
            if (obj.isPresent()) {
                try {
                    JSONObject objSnapshot = obj.get().getJSONRepresentation();
                    JSONDiff diff = new JSONDiff(new JSONObject(), new JSONObject(), input.getJSONObject(key));
                    JSONObject newRepresentation = JSONUtils.patch(objSnapshot, diff);
                    modifiedState.getJSONObject(BOARD_COLLECTION_KEY).put(key, newRepresentation);
                } catch (JSONException e) {
                }
            }
        }
        this.gameSequenceID++;
        setCurrentSequence(this.gameSequenceID);
        //Compute diffs and store as a snapshot
        JSONObject matchDiff = JSONUtils.getDiff(currentState, modifiedState).asJSONObject();
        JSONObject gameUpdate = new JSONObject();
        try {
            gameUpdate.put("action", reason);
            gameUpdate.put("new_state", matchDiff);
        } catch (JSONException e) {
            //should never happen - keys are non-null
        }
        storeSnapshotForSequence(this.gameSequenceID, gameUpdate);
        broadcastToAllParties(GAME_UPDATE_KEY,gameUpdate);
    }

    private void swapSides() {
        if (gameState == GameState.HERO_TURN) {
            gameState = GameState.ARCHITECT_TURN;
        } else if (gameState == GameState.ARCHITECT_TURN) {
            gameState = GameState.HERO_TURN;
        }
        boardObjects.stream().filter(obj -> obj instanceof Creature).forEach(obj -> {
            Creature c = (Creature) obj;
            c.resetActionPoints();
            c.triggerPassive(gameMap, boardObjects);
        });
        this.turnNumber++;
    }

    public Player getHeroPlayer() {
        return heroPlayer;
    }

    public Player getArchitectPlayer() {
        return architectPlayer;
    }

    /**
     * Creates a diff snapshot of the match given a previous state, creating a diff between the current state
     * and that previous state. Turns that diff into a Game Update object, with the difference and an action /
     * reason for the update. Automatically updates the game's sequence identifier.
     *
     * @param originalState The state to compare against. Typically this is the state before an action is done.
     * @param actionReason  The reason for the game update. Usually this describes the action that occurred.
     * @return Returns a Game Update - a JSONObject that cotains an "action" (the reason for the update) and a "new_state"
     * which is the difference between the old and new states.
     */
    public JSONObject takeSnapshot(JSONObject originalState, Object actionReason) {
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

    private void setCurrentSequence(int sequence) {
        GameEngine.instance().services.cacheService.storeString(this.matchIdentifier.toString() + CURRENT_SEQUENCE_CACHE_KEY, "" + sequence);
        GameEngine.instance().services.cacheService.storeString(this.matchIdentifier.toString() + CURRENT_SNAPSHOT_CACHE_KEY, this.getJSONRepresentation().toString());
    }

    private void storeSnapshotForSequence(int sequenceNumber, JSONObject snapshot) {
        GameEngine.instance().services.cacheService.storeString(this.matchIdentifier + GAME_SEQUENCE_KEY + sequenceNumber, snapshot.toString());
    }

    public boolean isPlayerTurn(Player p) {
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
        doAndSnapshot("match_end: "+reason,()->{
            gameState = GameState.GAME_END;
        },false);
        //TODO commit match data, update xp, currency, etc
        JSONObject reasonObj = new JSONObject();
        try {
            reasonObj = new JSONObject("{\"reason\":" + reason + "}");
        } catch (JSONException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
        }
        broadcastToAllParties(MATCH_END_KEY, reasonObj);
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

            //Current turn number
            obj.put(TURN_NUMBER_KEY, this.turnNumber);

            obj.put(MATCH_OBJECTIVE_KEY, objective.getJSONRepresentation());

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

    public int getTurnNumber() {
        return turnNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Match match = (Match) o;

        return getMatchIdentifier().equals(match.getMatchIdentifier());

    }

    @Override
    public int hashCode() {
        return getMatchIdentifier().hashCode();
    }

    public int getGameSequenceID() {
        return gameSequenceID;
    }

    public GameObjective getObjective() {
        return objective;
    }
}
