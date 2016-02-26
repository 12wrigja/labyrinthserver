package edu.cwru.eecs395_s16.networking;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.auth.exceptions.*;
import edu.cwru.eecs395_s16.bots.PassBot;
import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.actions.MoveGameAction;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.objects.GameAction;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;
import edu.cwru.eecs395_s16.interfaces.services.GameClient;
import edu.cwru.eecs395_s16.networking.requests.*;
import edu.cwru.eecs395_s16.networking.requests.gameactions.MoveGameActionData;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/20/16.
 */
public class NetworkingInterface {

    @NetworkEvent(mustAuthenticate = false, description = "Used to log a player in. This must be called once to allow the user to the call all methods that are marked as needing authentication.")
    public Response login(LoginUserRequest data, GameClient client) throws UnknownUsernameException, InvalidPasswordException {
        Optional<Player> p = GameEngine.instance().getPlayerRepository().loginPlayer(data.getUsername(), data.getPassword());
        if (p.isPresent()) {
            GameEngine.instance().getSessionRepository().storePlayer(client.getSessionId(), p.get());
            return new Response();
        } else {
            //This will be triggered if you try and log in as a bot
            return new Response(StatusCode.SERVER_ERROR);
        }
    }

    @NetworkEvent(mustAuthenticate = false, description = "Registers a user if the username does not already exist and the given passwords match.")
    public Response register(RegisterUserRequest data) throws DuplicateUsernameException, MismatchedPasswordException {
        Optional<Player> p = GameEngine.instance().getPlayerRepository().registerPlayer(data.getUsername(), data.getPassword(), data.getPasswordConfirm());
        if(p.isPresent()) {
            return new Response();
        } else {
            //This will be triggered if you try and register using a bot username
            return new Response(StatusCode.SERVER_ERROR);
        }
    }

    @NetworkEvent(mustAuthenticate = false, description = "DEV ONLY: Returns a random map generated using random walk.")
    public Response map(NewMapRequest obj) {
        Response r = new Response();
        r.setKey("map", new AlmostBlankMap(obj.getX(), obj.getY()));
        return r;
    }

    @NetworkEvent(description = "Queues up the player to play as the heroes")
    public Response queueUpHeroes(QueueRequest obj, Player p) throws InvalidGameStateException {
        if(obj.shouldQueueWithPassBot()){
            //TODO update this to pick a random map?
            Optional<Match> m = Match.InitNewMatch(p,new PassBot(), new AlmostBlankMap(10,10));
            if(m.isPresent()){
                return new Response();
            } else {
                //TODO update this so that the correct response is sent when you cannot make a match
                return new Response(StatusCode.UNPROCESSABLE_DATA);
            }
        } else {
            boolean isQueued = GameEngine.instance().getMatchService().queueAsHeroes(p);
            Response r = new Response();
            r.setKey("queued", isQueued);
            return r;
        }
    }

    @NetworkEvent(description = "Queues up the player to play as the heroes")
    public Response queueUpArchitect(QueueRequest obj, Player p) throws InvalidGameStateException {
        if(obj.shouldQueueWithPassBot()){
            Optional<Match> m = Match.InitNewMatch(new PassBot(),p, new AlmostBlankMap(10,10));
            if(m.isPresent()){
                return new Response();
            } else {
                //TODO update this so that the correct response is sent when you cannot make a match
                return new Response(StatusCode.UNPROCESSABLE_DATA);
            }
        } else {
            boolean isQueued = GameEngine.instance().getMatchService().queueAsArchitect(p);
            Response r = new Response();
            r.setKey("queued", isQueued);
            return r;
        }
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

    @NetworkEvent(description = "Allows a player playing a game to submit a game action")
    public Response gameAction(GameActionBaseRequest obj, Player p) throws InvalidDataException, UnauthorizedActionException, InvalidGameStateException {
        Optional<UUID> matchID = p.getCurrentMatchID();
        if(matchID.isPresent()) {
            Optional<GameAction> action = Optional.empty();
            switch (obj.getType()) {
                case MOVE_ACTION: {
                    MoveGameActionData moveData = new MoveGameActionData();
                    moveData.fillFromJSON(obj.getOriginalData());
                    action = Optional.of(new MoveGameAction(moveData));
                    break;
                }
                case BASIC_ATTACK_ACTION: {
//                RequestData action = new MoveGameAction();
//                action.fillFromJSON(obj.getOriginalData());
//                actualAction = Optional.of(action);
                    break;
                }
                case PASS_ACTION: {
                    return new Response(StatusCode.SERVER_ERROR);
                }
                case ABILITY_ACTION: {
                    return new Response(StatusCode.SERVER_ERROR);
                }
            }
            if (action.isPresent()) {
                Optional<Match> m = Match.fromCacheWithMatchIdentifier(matchID.get());
                if(m.isPresent()){
                    if(m.get().updateGameState(p,action.get())) {
                        return new Response();
                    }
                }
            }
            return new Response(StatusCode.UNPROCESSABLE_DATA);
        }
        Response r = new Response(StatusCode.SERVER_ERROR);
        r.setKey("message","Unable to find match.");
        return r;
    }

    @NetworkEvent(description = "TESTS JSON PARSING", mustAuthenticate = false)
    public Response testJson(JSONObject obj) {
        System.out.println(obj.toString());
        Response r = new Response();
        r.setKey("input",obj);
        return r;
    }

    @NetworkEvent(description = "Returns the latest state of a match if you are in one")
    public Response matchState(NoInputRequest obj, Player p){
        Optional<UUID> matchID = p.getCurrentMatchID();
        if(matchID.isPresent()){
            Optional<Match> m = Match.fromCacheWithMatchIdentifier(matchID.get());
            if(m.isPresent()){
                Response r = new Response();
                r.setKey("game_state",m.get().getJSONRepresentation());
                return r;
            } else {
                return new Response(StatusCode.SERVER_ERROR);
            }
        } else {
            Response r = new Response(StatusCode.UNPROCESSABLE_DATA);
            r.setKey("message","You currently aren't in a match!");
            return r;
        }
    }

    @NetworkEvent(description = "Returns your current match id, if there is one.")
    public Response currentMatch(NoInputRequest obj, Player p) {
        Response r = new Response();
        Optional<UUID> matchID = p.getCurrentMatchID();
        r.setKey("match_id",matchID.isPresent()?matchID.get().toString():"none");
        return r;
    }

    @NetworkEvent(description = "Leaves the current match. Will terminate the match for other players as well, and end the match for all spectators.")
    public Response leaveMatch(NoInputRequest obj, Player p) {
        Optional<UUID> m = p.getCurrentMatchID();
        if(m.isPresent()){
            Optional<Match> match = Match.fromCacheWithMatchIdentifier(m.get());
            if(match.isPresent()) {
                match.get().end("Player " + p.getUsername() + " left the match.");
                return new Response();
            } else {
                return new Response(StatusCode.SERVER_ERROR);
            }
        } else {
            Response r = new Response(StatusCode.UNPROCESSABLE_DATA);
            r.setKey("message","You are not currently in a match.");
            return r;
        }
    }
}