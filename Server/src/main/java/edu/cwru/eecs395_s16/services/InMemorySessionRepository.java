package edu.cwru.eecs395_s16.services;

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

    private Map<UUID, Player> sessionMap = new ConcurrentHashMap<>();
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
}
