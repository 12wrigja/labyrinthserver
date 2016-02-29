package edu.cwru.eecs395_s16.networking;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.interfaces.services.GameClient;
import edu.cwru.eecs395_s16.networking.requests.LoginUserRequest;
import edu.cwru.eecs395_s16.networking.requests.NewMapRequest;
import edu.cwru.eecs395_s16.networking.requests.RegisterUserRequest;
import edu.cwru.eecs395_s16.networking.responses.NewMapResponse;

/**
 * Created by james on 1/20/16.
 */
public class NetworkingInterface {

    @NetworkEvent(mustAuthenticate = false, description = "Used to log a player in. This must be called once to allow the user to the call all methods that are marked as needing authentication.")
    public InternalResponseObject<Player> login(LoginUserRequest data, GameClient client) {
        InternalResponseObject<Player> p = GameEngine.instance().getPlayerRepository().loginPlayer(data.getUsername(), data.getPassword());
        if (p.isPresent()) {
            GameEngine.instance().getSessionRepository().storePlayer(client.getSessionId(), p.get());
        }
        return p;
    }

    @NetworkEvent(mustAuthenticate = false, description = "Registers a user if the username does not already exist and the given passwords match.")
    public InternalResponseObject<Player> register(RegisterUserRequest data) {
        return GameEngine.instance().getPlayerRepository().registerPlayer(data.getUsername(), data.getPassword(), data.getPasswordConfirm());
    }

    @NetworkEvent(mustAuthenticate = false, description = "DEV ONLY: Returns a random map generated using random walk.")
    public NewMapResponse map(NewMapRequest obj) {
        NewMapResponse r = new NewMapResponse(new AlmostBlankMap(obj.getX(), obj.getY()));
        return r;
    }
//
//    @NetworkEvent(description = "Queues up the player to play as the heroes")
//    public QueueStatusResponse queueUpHeroes(QueueRequest obj, Player p) {
//        if (obj.shouldQueueWithPassBot()) {
//            //TODO update this to pick a random map?
//            Optional<Match> m = Match.InitNewMatch(p, new PassBot(), new AlmostBlankMap(10, 10));
//            if (m.isPresent()) {
//                return new QueueStatusResponse(true);
//            } else {
//                /*TODO figure out when this is called and update this so that the correct
//                 * response is sent when you cannot make a match
//                 */
//                return new QueueStatusResponse(WebStatusCode.UNPROCESSABLE_DATA, "Unable to queue into a match", false);
//            }
//        } else {
//            boolean isQueued = GameEngine.instance().getMatchService().queueAsHeroes(p);
//            return new QueueStatusResponse(isQueued);
//        }
//    }
//
//    @NetworkEvent(description = "Queues up the player to play as the heroes")
//    public Response queueUpArchitect(QueueRequest obj, Player p) {
//        if (obj.shouldQueueWithPassBot()) {
//            Optional<Match> m = Match.InitNewMatch(new PassBot(), p, new AlmostBlankMap(10, 10));
//            if (m.isPresent()) {
//                return new Response();
//            } else {
//                //TODO update this so that the correct response is sent when you cannot make a match
//                return new Response(WebStatusCode.UNPROCESSABLE_DATA);
//            }
//        } else {
//            boolean isQueued = GameEngine.instance().getMatchService().queueAsArchitect(p);
//            Response r = new Response();
//            r.setKey("queued", isQueued);
//            return r;
//        }
//    }
//
//    @NetworkEvent(description = "Removes the player from any matchmaking queue they are in.")
//    public Response dequeue(NoInputRequest obj, Player p) {
//        boolean isDequeued = GameEngine.instance().getMatchService().removeFromQueue(p);
//        Response r = new Response();
//        r.setKey("queued", !isDequeued);
//        return r;
//    }
//
//    @NetworkEvent(description = "TESTING: returns a list of heroes.")
//    public Response getHeroes(NoInputRequest obj, Player p) {
//        HeroRepository heroRepo = GameEngine.instance().getHeroRepository();
//        List<Hero> myHeroes = heroRepo.getPlayerHeroes(p);
//        Response r = new Response();
//        r.setKey("heroes", myHeroes);
//        return r;
//    }
//
//    @NetworkEvent(description = "Allows a player to spectate a match between other players.")
//    public Response spectate(SpectateMatchRequest obj, Player p) {
//        Optional<Match> m = Match.fromCacheWithMatchIdentifier(obj.getMatchID());
//        if (m.isPresent()) {
//            Match match = m.get();
//            match.addSpectator(p);
//            return new Response();
//        } else {
//            Response r = new Response(WebStatusCode.UNPROCESSABLE_DATA);
//            r.setKey("message", "Unable to spectate match with id: " + obj.getMatchID());
//            return r;
//        }
//    }
//
//    @NetworkEvent(description = "Allows a spectating player to stop spectating a match")
//    public Response stopSpectating(NoInputRequest obj, Player p) {
//        Optional<UUID> matchIdentifier = p.getCurrentMatchID();
//        if (matchIdentifier.isPresent()) {
//            Optional<Match> m = Match.fromCacheWithMatchIdentifier(matchIdentifier.get());
//            if (m.isPresent()) {
//                if (m.get().isSpectatorOfMatch(p)) {
//                    m.get().removeSpectator(p);
//                    return new Response();
//                } else {
//                    Response r = new Response(WebStatusCode.UNPROCESSABLE_DATA);
//                    r.setKey("message", "You are currently not spectating a match right now");
//                    return r;
//                }
//            } else {
//                return new Response(WebStatusCode.SERVER_ERROR);
//            }
//        } else {
//            Response r = new Response(WebStatusCode.UNPROCESSABLE_DATA);
//            r.setKey("message", "You are currently not spectating a match right now");
//            return r;
//        }
//    }
//
//    @NetworkEvent(description = "Allows a player playing a game to submit a game action")
//    public Response gameAction(GameActionBaseRequest obj, Player p) {
//        Optional<UUID> matchID = p.getCurrentMatchID();
//        if (matchID.isPresent()) {
//            Optional<GameAction> action = Optional.empty();
//            switch (obj.getType()) {
//                case MOVE_ACTION: {
//                    MoveGameActionData moveData = new MoveGameActionData();
//                    moveData.fillFromJSON(obj.getOriginalData());
//                    action = Optional.of(new MoveGameAction(moveData));
//                    break;
//                }
//                case BASIC_ATTACK_ACTION: {
////                RequestData action = new MoveGameAction();
////                action.fillFromJSON(obj.getOriginalData());
////                actualAction = Optional.of(action);
//                    break;
//                }
//                case PASS_ACTION: {
//                    return new Response(WebStatusCode.SERVER_ERROR);
//                }
//                case ABILITY_ACTION: {
//                    return new Response(WebStatusCode.SERVER_ERROR);
//                }
//            }
//            if (action.isPresent()) {
//                Optional<Match> m = Match.fromCacheWithMatchIdentifier(matchID.get());
//                if (m.isPresent()) {
//                    if (m.get().updateGameState(p, action.get())) {
//                        return new Response();
//                    }
//                }
//            }
//            return new Response(WebStatusCode.UNPROCESSABLE_DATA);
//        }
//        Response r = new Response(WebStatusCode.SERVER_ERROR);
//        r.setKey("message", "Unable to find match.");
//        return r;
//    }
//
//    @NetworkEvent(description = "TESTS JSON PARSING", mustAuthenticate = false)
//    public Response testJson(JSONObject obj) {
//        System.out.println(obj.toString());
//        Response r = new Response();
//        r.setKey("input", obj);
//        return r;
//    }
//
//    @NetworkEvent(description = "Returns the latest state of a match if you are in one")
//    public Response matchState(NoInputRequest obj, Player p) {
//        Optional<UUID> matchID = p.getCurrentMatchID();
//        if (matchID.isPresent()) {
//            Optional<Match> m = Match.fromCacheWithMatchIdentifier(matchID.get());
//            if (m.isPresent()) {
//                Response r = new Response();
//                //JSON Representation Change
//                r.setKey("game_state", m.get());
//                return r;
//            } else {
//                return new Response(WebStatusCode.SERVER_ERROR);
//            }
//        } else {
//            Response r = new Response(WebStatusCode.UNPROCESSABLE_DATA);
//            r.setKey("message", "You currently aren't in a match!");
//            return r;
//        }
//    }
//
//    @NetworkEvent(description = "Returns your current match id, if there is one.")
//    public Response currentMatch(NoInputRequest obj, Player p) {
//        Response r = new Response();
//        Optional<UUID> matchID = p.getCurrentMatchID();
//        r.setKey("match_id", matchID.isPresent() ? matchID.get().toString() : "none");
//        return r;
//    }
//
//    @NetworkEvent(description = "Leaves the current match. Will terminate the match for other players as well, and end the match for all spectators.")
//    public Response leaveMatch(NoInputRequest obj, Player p) {
//        Optional<UUID> m = p.getCurrentMatchID();
//        if (m.isPresent()) {
//            Optional<Match> match = Match.fromCacheWithMatchIdentifier(m.get());
//            if (match.isPresent()) {
//                match.get().end("Player " + p.getUsername() + " left the match.");
//                return new Response();
//            } else {
//                return new Response(WebStatusCode.SERVER_ERROR);
//            }
//        } else {
//            Response r = new Response(WebStatusCode.UNPROCESSABLE_DATA);
//            r.setKey("message", "You are not currently in a match.");
//            return r;
//        }
//    }
}