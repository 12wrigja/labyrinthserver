package edu.cwru.eecs395_s16.services.sessions;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.connections.GameClient;

import java.util.Map;
import java.util.Optional;
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
    public Optional<GameClient> findClient(Player player) {
        UUID clientID = connectionMap.get(player.getUsername());
        if(clientID == null){
            return Optional.empty();
        } else {
            InternalResponseObject<GameClient> client = GameEngine.instance().findClientFromUUID(clientID);
            if(client.isNormal()){
                return Optional.of(client.get());
            } else {
                return Optional.empty();
            }
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

}
