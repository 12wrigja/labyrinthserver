package edu.cwru.eecs395_s16.networking;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.auth.AuthenticationMiddlewareDataListener;
import edu.cwru.eecs395_s16.auth.exceptions.*;
import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.RandomlyGeneratedGameMap;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;
import edu.cwru.eecs395_s16.networking.requests.*;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/20/16.
 */
public class NetworkingInterface {

    public DataListener<JSONObject> createTypecastMiddleware(Method next, boolean needsAuth) {
        return new AuthenticationMiddlewareDataListener(this, GameEngine.instance().getSessionRepository(), next, needsAuth);
    }

    @NetworkEvent(mustAuthenticate = false, description = "Used to log a player in. This must be called once to allow the user to the call all methods that are marked as needing authentication.")
    public Response login(LoginUserRequest data, SocketIOClient client) throws UnknownUsernameException, InvalidPasswordException {
        Optional<Player> p = GameEngine.instance().getPlayerRepository().loginPlayer(data.getUsername(), data.getPassword());
        if (p.isPresent()) {
            GameEngine.instance().getSessionRepository().storePlayer(client.getSessionId(), p.get());
        }
        return new Response();
    }

    @NetworkEvent(mustAuthenticate = false, description = "Registers a user if the username does not already exist and the given passwords match.")
    public Response register(RegisterUserRequest data) throws DuplicateUsernameException, MismatchedPasswordException {
        GameEngine.instance().getPlayerRepository().registerPlayer(data.getUsername(), data.getPassword(), data.getPasswordConfirm());
        return new Response();
    }

    @NetworkEvent(mustAuthenticate = false, description = "DEV ONLY: Returns a random map generated using random walk.")
    public Response map(NewMapRequest obj) {
        Response r = new Response();
        r.setKey("map", new RandomlyGeneratedGameMap(obj.getX(), obj.getY()));
        return r;
    }

    @NetworkEvent(description = "Queues up the player to play as the heroes")
    public Response queueUpHeroes(NoInputRequest obj, Player p) throws InvalidGameStateException {
        boolean isQueued = GameEngine.instance().getMatchService().queueAsHeroes(p);
        Response r = new Response();
        r.setKey("queued", isQueued);
        return r;
    }

    @NetworkEvent(description = "Queues up the player to play as the heroes")
    public Response queueUpArchitect(NoInputRequest obj, Player p) throws InvalidGameStateException {
        boolean isQueued = GameEngine.instance().getMatchService().queueAsArchitect(p);
        Response r = new Response();
        r.setKey("queued", isQueued);
        return r;
    }

    @NetworkEvent(description = "Removes the player from any matchmaking queue they are in.")
    public Response dequeue(NoInputRequest obj, Player p) throws InvalidGameStateException {
        boolean isDequeued = GameEngine.instance().getMatchService().removeFromQueue(p);
        Response r = new Response();
        r.setKey("queued", !isDequeued);
        return r;
    }

    @NetworkEvent(description = "TESTING: returns a list of heroes.")
    public Response getHeroes(NoInputRequest obj, Player p) {
        HeroRepository heroRepo = GameEngine.instance().getHeroRepository();
        List<Hero> myHeroes = heroRepo.getPlayerHeroes(p);
        Response r = new Response();
        r.setKey("heroes", myHeroes);
        return r;
    }

    @NetworkEvent(description = "Allows a player to spectate a match between other players.")
    public Response spectate(SpectateMatchRequest obj, Player p) {
        Optional<Match> m = Match.fromCacheWithMatchIdentifier(obj.getMatchID());
        if(m.isPresent()){
            Match match = m.get();
            match.addSpectator(p);
            return new Response();
        } else {
            Response r = new Response(StatusCode.UNPROCESSABLE_DATA);
            r.setKey("message","Unable to spectate match with id: "+obj.getMatchID());
            return r;
        }
    }

    @NetworkEvent(description = "Allows a spectating player to stop spectating a match")
    public Response stopSpectating(NoInputRequest obj, Player p) {
        Optional<UUID> matchIdentifier = p.getCurrentMatchID();
        if(matchIdentifier.isPresent()){
            Optional<Match> m = Match.fromCacheWithMatchIdentifier(matchIdentifier.get());
            if(m.isPresent()){
                if(m.get().isSpectatorOfMatch(p)){
                    m.get().removeSpectator(p);
                    return new Response();
                } else {
                    Response r = new Response(StatusCode.UNPROCESSABLE_DATA);
                    r.setKey("message","You are currently not spectating a match right now");
                    return r;
                }
            } else {
                return new Response(StatusCode.SERVER_ERROR);
            }
        } else {
            Response r = new Response(StatusCode.UNPROCESSABLE_DATA);
            r.setKey("message","You are currently not spectating a match right now");
            return r;
        }
    }

//    @NetworkEvent(description = "Allows a player playing a game to submit a game action")
//    public Response gameAction(GameActionBaseRequest obj, Player p) throws InvalidDataException, UnauthorizedActionException {
//        Optional<UUID> matchID = p.getCurrentMatchID();
//        if(matchID.isPresent()) {
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
//                    return new Response(StatusCode.SERVER_ERROR);
//                }
//                case ABILITY_ACTION: {
//                    return new Response(StatusCode.SERVER_ERROR);
//                }
//            }
//            if (action.isPresent()) {
//                Optional<Match> m = Match.fromCacheWithMatchIdentifier(matchID.get());
//                if(m.isPresent()){
//                    m.get().updateGameState(p,action.get());
//                }
//            } else {
//                return new Response(StatusCode.UNPROCESSABLE_DATA);
//            }
//        }
//        Response r = new Response(StatusCode.SERVER_ERROR);
//        r.setKey("message","Unable to find match.");
//        return r;
//    }

    @NetworkEvent(description = "TESTS JSON PARSING", mustAuthenticate = false)
    public Response testJson(JSONObject obj) {
        System.out.println(obj.toString());
        Response r = new Response();
        r.setKey("input",obj);
        return r;
    }

}