package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.core.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public interface SessionRepository {
    Optional<Player> findPlayer(UUID clientID);
    Optional<Player> findPlayer(String username) throws UnknownUsernameException;
    void storePlayer(UUID clientID, Player player);
}
