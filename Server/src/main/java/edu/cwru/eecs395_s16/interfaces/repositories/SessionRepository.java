package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;

import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public interface SessionRepository {
    InternalResponseObject<Player> findPlayer(UUID clientID);
    InternalResponseObject<Player> findPlayer(String username);
    void storePlayer(UUID clientID, Player player);
    void expirePlayerSession(UUID clientID);
    void expirePlayerSession(String username);
}
