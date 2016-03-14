package edu.cwru.eecs395_s16.services.sessionrepository;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by james on 1/21/16.
 */
public class InMemorySessionRepository implements SessionRepository {

    //Maps session ID's to players
    private Map<UUID, Player> sessionMap = new ConcurrentHashMap<>();
    //Maps usernames to player sessions
    private Map<String,UUID> connectionMap = new ConcurrentHashMap<>();

    @Override
    public InternalResponseObject<Player> findPlayer(UUID token) {
        if(sessionMap.containsKey(token)){
            return new InternalResponseObject<>(sessionMap.get(token),"player");
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.UNKNOWN_SESSION_IDENTIFIER, "Unable to find the player for the given session token.");
        }

    }

    @Override
    public InternalResponseObject<Player> findPlayer(String username) {
        UUID clientID = connectionMap.get(username);
        if(clientID == null){
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.UNKNOWN_USERNAME, "Could not find a session for the player with the given username.");
        } else {
            return findPlayer(clientID);
        }
    }

    @Override
    public void storePlayer(UUID token, Player player) {
        sessionMap.put(token, player);
        connectionMap.put(player.getUsername(), token);
    }

    @Override
    public void expirePlayerSession(UUID clientID) {
        if(sessionMap.containsKey(clientID)){
            Player p = sessionMap.get(clientID);
            sessionMap.remove(clientID);
            if(connectionMap.containsKey(p.getUsername())) {
                connectionMap.remove(p.getUsername());
            }
        }
    }

    @Override
    public void expirePlayerSession(String username) {
        if(connectionMap.containsKey(username)){
            UUID playerID = connectionMap.get(username);
            expirePlayerSession(playerID);
        }
    }
}
