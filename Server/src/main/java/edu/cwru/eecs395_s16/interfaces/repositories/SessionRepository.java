package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.core.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public interface SessionRepository {
    Optional<Player> findPlayer(UUID token);

    void storePlayer(UUID token, Player player);
}
