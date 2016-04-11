package edu.cwru.eecs395_s16.services.sessions;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.services.connections.GameClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public interface SessionRepository {
    InternalResponseObject<Player> findPlayer(UUID clientID);
    Optional<GameClient> findClient(Player player);
    void storePlayer(UUID clientID, Player player);
    void expirePlayerSession(UUID clientID);
}
