package edu.cwru.eecs395_s16.core.Interfaces.Repositories;

import edu.cwru.eecs395_s16.core.Player;

import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public interface SessionRepository {
    Player findPlayer(UUID token);

    void storePlayer(UUID token, Player player);
}
