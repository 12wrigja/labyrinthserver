package edu.cwru.eecs395_s16.networking;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.core.*;
import edu.cwru.eecs395_s16.core.actions.*;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterDefinition;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.objectives.CaptureObjectivesGameObjective;
import edu.cwru.eecs395_s16.core.objects.objectives.DeathmatchGameObjective;
import edu.cwru.eecs395_s16.core.objects.objectives.GameObjective;
import edu.cwru.eecs395_s16.networking.requests.*;
import edu.cwru.eecs395_s16.networking.requests.gameactions.BasicAttackActionData;
import edu.cwru.eecs395_s16.networking.requests.gameactions.CaptureObjectiveActionData;
import edu.cwru.eecs395_s16.networking.requests.gameactions.MoveGameActionData;
import edu.cwru.eecs395_s16.networking.requests.gameactions.PassGameActionData;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueArchitectRequest;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueHeroesRequest;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.bots.botimpls.PassBot;
import edu.cwru.eecs395_s16.services.connections.GameClient;
import edu.cwru.eecs395_s16.services.heroes.HeroRepository;
import edu.cwru.eecs395_s16.services.maps.MapRepository;
import edu.cwru.eecs395_s16.services.monsters.MonsterRepository;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/20/16.
 */
public class NetworkingInterface {

    @NetworkEvent(mustAuthenticate = false, description = "Used to log a player in. This must be called once to allow the user to the call all methods that are marked as needing authentication.")
    public InternalResponseObject<Player> login(LoginUserRequest data, GameClient client) {
        InternalResponseObject<Player> p = GameEngine.instance().services.playerRepository.loginPlayer(data.getUsername(), data.getPassword());
        if (p.isPresent()) {
            Optional<GameClient> playerClient = p.get().getClient();
            if (playerClient.isPresent()) {
                if (!playerClient.get().getSessionId().equals(client.getSessionId())) {
                    return new InternalResponseObject<>(WebStatusCode.UNAUTHORIZED, InternalErrorCode.ALREADY_SIGNED_IN);
                }
            }
            GameEngine.instance().services.sessionRepository.storePlayer(client.getSessionId(), p.get());
        }
        return p;
    }

    @NetworkEvent(mustAuthenticate = false, description = "Registers a user if the username does not already exist and the given passwords match.")
    public InternalResponseObject<Player> register(RegisterUserRequest data) {
        InternalResponseObject<Player> newPlayerResp = GameEngine.instance().services.playerRepository.registerPlayer(data.getUsername(), data.getPassword(), data.getPasswordConfirm());
        if (newPlayerResp.isNormal()) {
            InternalResponseObject<Boolean> heroesCreatedResp = GameEngine.instance().services.heroRepository.createDefaultHeroesForPlayer(newPlayerResp.get());
            if (!heroesCreatedResp.isNormal()) {
                GameEngine.instance().services.playerRepository.deletePlayer(newPlayerResp.get());
                return InternalResponseObject.cloneError(heroesCreatedResp);
            }
            InternalResponseObject<Boolean> monstersCreatedResp = GameEngine.instance().services.monsterRepository.createDefaultMonstersForPlayer(newPlayerResp.get());
            if (!monstersCreatedResp.isNormal()) {
                GameEngine.instance().services.playerRepository.deletePlayer(newPlayerResp.get());
                return InternalResponseObject.cloneError(monstersCreatedResp);
            }
        }
        return newPlayerResp;
    }

    @NetworkEvent(description = "Returns the full definition for the map with the given id.")
    public InternalResponseObject<GameMap> map(GetMapRequest obj, Player p) {
        MapRepository mRepo = GameEngine.instance().services.mapRepository;
        return mRepo.getMapByID(obj.getRequestedMapID());
    }

    @NetworkEvent(description = "Returns metadata on all maps.")
    public InternalResponseObject<List<MapRepository.MapMetadata>> maps(NoInputRequest obj, Player p) {
        MapRepository mRepo = GameEngine.instance().services.mapRepository;
        return mRepo.getMapData();
    }

    @NetworkEvent(description = "Stores a map.")
    public InternalResponseObject<Integer> storeMap(MapStoreRequest obj, Player p) {
        MapRepository mRepo = GameEngine.instance().services.mapRepository;
        return mRepo.storeNewMapInDatabase(obj.getMap().getName(), p, obj.getMap());
    }

    @NetworkEvent(description = "Queues up the player to play as the heroes")
    public InternalResponseObject<Boolean> queueUpHeroes(QueueHeroesRequest obj, Player p) {
        if (p.getCurrentMatchID().isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.PLAYER_BUSY, "You are in a match. Leave the match before queueing.");
        }
        InternalResponseObject<GameMap> mapResp = GameEngine.instance().services.mapRepository.getMapByID(obj.getMapID());
        GameMap map;
        if (mapResp.isNormal()) {
            map = mapResp.get();
        } else {
            map = new AlmostBlankMap(10, 10);
        }
        GameObjective objective = new DeathmatchGameObjective();
        if (obj.getGameObjectiveShortCode().equals("obj")) {
            objective = new CaptureObjectivesGameObjective(1);
        } else if (obj.getGameObjectiveShortCode().equals("dm")) {
            objective = new DeathmatchGameObjective();
        }
        if (obj.shouldQueueWithPassBot()) {
            //TODO update this to pick a random map?
            InternalResponseObject<Match> m = Match.InitNewMatch(p, new PassBot(), map, objective, obj.getSelectedHeroesIds(), null);
            if (m.isNormal()) {
                return new InternalResponseObject<>(true, "match_created");
            } else {
                return InternalResponseObject.cloneError(m);
            }
        } else {

            return GameEngine.instance().services.matchService.queueAsHeroes(p, obj, map, objective);
        }
    }

    @NetworkEvent(description = "Queues up the player to play as the heroes")
    public InternalResponseObject<Boolean> queueUpArchitect(QueueArchitectRequest obj, Player p) {
        if (p.getCurrentMatchID().isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.PLAYER_BUSY, "You are in a match. Leave the match before queueing.");
        }
        InternalResponseObject<GameMap> mapResp = GameEngine.instance().services.mapRepository.getMapByID(obj.getMapID());
        GameMap map;
        if (mapResp.isNormal()) {
            map = mapResp.get();
        } else {
            map = new AlmostBlankMap(10, 10);
        }
        GameObjective objective = new DeathmatchGameObjective();
        if (obj.getGameObjectiveShortCode().equals("obj")) {
            objective = new CaptureObjectivesGameObjective(1);
        } else if (obj.getGameObjectiveShortCode().equals("dm")) {
            objective = new DeathmatchGameObjective();
        }
        if (obj.shouldQueueWithPassBot()) {
            InternalResponseObject<Match> m = Match.InitNewMatch(new PassBot(), p, map, objective, null, null);
            if (m.isNormal()) {
                return new InternalResponseObject<>(true, "match_created");
            } else {
                //TODO update this so that the correct response is sent when you cannot make a match
                return InternalResponseObject.cloneError(m);
            }
        } else {
            return GameEngine.instance().services.matchService.queueAsArchitect(p, obj, map, objective);
        }
    }

    @NetworkEvent(description = "Removes the player from any matchmaking queue they are in.")
    public InternalResponseObject<Boolean> dequeue(NoInputRequest obj, Player p) {
        return GameEngine.instance().services.matchService.removeFromQueue(p);
    }

    @NetworkEvent(description = "Returns a list of all the player's current heroes. This is for use in the hero management and match initialization screens, not in-game.")
    public InternalResponseObject<List<Hero>> getHeroes(NoInputRequest obj, Player p) {
        HeroRepository heroRepo = GameEngine.instance().services.heroRepository;
        return heroRepo.getPlayerHeroes(p);
    }

    @NetworkEvent(description = "Returns a list of all the player's monsters. This is for use in the monster management and match initialization screens, not in-game.")
    public InternalResponseObject<List<MonsterDefinition>> getMonsters(NoInputRequest obj, Player p) {
        MonsterRepository heroRepo = GameEngine.instance().services.monsterRepository;
        return heroRepo.getPlayerMonsterTypes(p);
    }

    @NetworkEvent(description = "Allows a player to spectate a match between other players.")
    public InternalResponseObject<String> spectate(SpectateMatchRequest obj, Player p) {
        if (p.getCurrentMatchID().isPresent()) {
            return new InternalResponseObject<>(InternalErrorCode.PLAYER_BUSY, "You are in a match. Leave the match before spectating a match.");
        }
        UUID matchID = null;
        if(obj.getMatchID() == null && obj.getPlayerID() == null){
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR,"You didn't specify a player or match ID to spectate.");
        }
        if (obj.getPlayerID() != null){
                InternalResponseObject<Player> playerResp = GameEngine.instance().services.playerRepository.findPlayer(obj.getPlayerID());
                if(!playerResp.isNormal()){
                    return InternalResponseObject.cloneError(playerResp);
                }
                Player player = playerResp.get();
                Optional<UUID> pMatchID = player.getCurrentMatchID();
                if(pMatchID.isPresent()){
                    matchID = pMatchID.get();
                } else {
                    return new InternalResponseObject<>(InternalErrorCode.NOT_IN_MATCH,"The specified player is not in a match.");
                }
        } else if (obj.getMatchID() != null){
            try {
                matchID = UUID.fromString(obj.getMatchID());
            } catch (IllegalArgumentException e){
                return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR,"Invalid match id.");
            }
        }

        if(matchID == null){
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR,"Invalid match id.");
        }

        InternalResponseObject<Match> m = Match.fromCacheWithMatchIdentifier(matchID);
        if (m.isNormal()) {
            Match match = m.get();
            match.addSpectator(p);
            return new InternalResponseObject<>(match.getMatchIdentifier().toString(), "spectating");
        } else {
            return InternalResponseObject.cloneError(m);
        }
    }

    @NetworkEvent(description = "Allows a spectating player to stop spectating a match")
    public InternalResponseObject<String> stopSpectating(NoInputRequest obj, Player p) {
        Optional<UUID> matchIdentifier = p.getCurrentMatchID();
        if (matchIdentifier.isPresent()) {
            InternalResponseObject<Match> m = Match.fromCacheWithMatchIdentifier(matchIdentifier.get());
            if (m.isNormal()) {
                if (m.get().isSpectatorOfMatch(p)) {
                    m.get().removeSpectator(p);
                }
            } else {
                return InternalResponseObject.cloneError(m);
            }
        }
        return new InternalResponseObject<>("none", "spectating");
    }

    @NetworkEvent(description = "Allows a player playing a game to submit a game action")
    public InternalResponseObject<Boolean> gameAction(GameActionBaseRequest obj, Player p) {
        String validActionStr = "valid";
        Optional<UUID> matchID = p.getCurrentMatchID();
        if (matchID.isPresent()) {
            InternalResponseObject<Match> m = Match.fromCacheWithMatchIdentifier(matchID.get());
            if (!m.isNormal()) {
                return InternalResponseObject.cloneError(m);
            }
            GameAction action;
            switch (obj.getType()) {
                case MOVE_ACTION: {
                    InternalResponseObject<MoveGameActionData> dataResp = MoveGameActionData.fillFromJSON(obj.getOriginalData());
                    if (!dataResp.isNormal()) {
                        return InternalResponseObject.cloneError(dataResp);
                    }
                    action = new MoveGameAction(dataResp.get());
                    break;
                }
                case BASIC_ATTACK_ACTION: {
                    InternalResponseObject<BasicAttackActionData> dataResp = BasicAttackActionData.fillFromJSON(obj.getOriginalData());
                    if (!dataResp.isNormal()) {
                        return InternalResponseObject.cloneError(dataResp);
                    }
                    action = new BasicAttackGameAction(dataResp.get());
                    break;
                }
                case PASS_ACTION: {
                    InternalResponseObject<PassGameActionData> dataResp = PassGameActionData.fillFromJSON(obj.getOriginalData());
                    if (!dataResp.isNormal()) {
                        return InternalResponseObject.cloneError(dataResp);
                    }
                    action = new PassGameAction(dataResp.get());
                    break;
                }
                case CAPTURE_OBJECTIVE_ACTION: {
                    InternalResponseObject<CaptureObjectiveActionData> dataResp = CaptureObjectiveActionData.fillFromJSON(obj.getOriginalData());
                    if (!dataResp.isNormal()) {
                        return InternalResponseObject.cloneError(dataResp);
                    }
                    action = new CaptureObjectiveGameAction(dataResp.get());
                    break;
                }
//                case ABILITY_ACTION: {
//
//                }
                default: {
                    return new InternalResponseObject<>(false, validActionStr);
                }
            }
            return m.get().updateGameState(p, action);
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.NOT_IN_MATCH);
        }
    }

    @NetworkEvent(description = "Returns the latest state of a match if you are in one")
    public InternalResponseObject<Match> matchState(NoInputRequest obj, Player p) {
        Optional<UUID> matchID = p.getCurrentMatchID();
        if (matchID.isPresent()) {
            InternalResponseObject<Match> m = Match.fromCacheWithMatchIdentifier(matchID.get());
            return m;
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.NOT_IN_MATCH);
        }
    }

    @NetworkEvent(description = "Returns your current match id, if there is one.")
    public InternalResponseObject<String> currentMatch(NoInputRequest obj, Player p) {
        Optional<UUID> matchIDOpt = p.getCurrentMatchID();
        String matchID = "none";
        if (matchIDOpt.isPresent()) {
            matchID = matchIDOpt.get().toString();
        }
        return new InternalResponseObject<>(matchID, "match_id");
    }

    @NetworkEvent(description = "Leaves the current match.")
    public InternalResponseObject<Boolean> leaveMatch(NoInputRequest obj, Player p) {
        Optional<UUID> m = p.getCurrentMatchID();
        if (m.isPresent()) {
            InternalResponseObject<Match> match = Match.fromCacheWithMatchIdentifier(m.get());
            if (match.isNormal()) {
                JSONObject playerData = new JSONObject();
                try {
                    playerData.put("player:", p.getUsername());
                } catch (JSONException e) {
                    //Should never happen - nonnull key.
                }
                match.get().broadcastToAllParties("player_left", playerData);
                p.setCurrentMatch(Optional.empty());
                if(match.get().getGameState() != GameState.GAME_END) {
                    if (match.get().getHeroPlayer().getUsername().equals(p.getUsername())) {
                        match.get().end("player_forfeit", GameObjective.GAME_WINNER.ARCHITECT_WINNER);
                    } else if (match.get().getArchitectPlayer().getUsername().equals(p.getUsername())) {
                        match.get().end("player_forfeit", GameObjective.GAME_WINNER.HERO_WINNER);
                    }
                }
                return new InternalResponseObject<>(true, "left_match");
            } else {
                return InternalResponseObject.cloneError(match);
            }
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.NOT_IN_MATCH);
        }
    }

    @NetworkEvent(description = "Dev only. Allows you to modify parts of the state of your current match. Does no validation on the state, so if you fuck it up your on your own.")
    public InternalResponseObject<Boolean> adjustMatch(JSONObject stateChanges, Player p) {
        if (!p.isDev()) {
            return new InternalResponseObject<>(WebStatusCode.UNAUTHORIZED);
        }
        Optional<UUID> m = p.getCurrentMatchID();
        if (m.isPresent()) {
            InternalResponseObject<Match> match = Match.fromCacheWithMatchIdentifier(m.get());
            if (match.isNormal()) {
                match.get().modify(stateChanges);
                return new InternalResponseObject<>(true, "updated");
            } else {
                return InternalResponseObject.cloneError(match);
            }
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.NOT_IN_MATCH);
        }
    }
}