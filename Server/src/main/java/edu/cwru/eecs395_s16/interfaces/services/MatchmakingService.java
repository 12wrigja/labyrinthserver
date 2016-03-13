package edu.cwru.eecs395_s16.interfaces.services;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.Player;

/**
 * Created by james on 2/15/16.
 */
public interface MatchmakingService {
    InternalResponseObject<Boolean> queueAsHeroes(Player p);

    InternalResponseObject<Boolean> queueAsArchitect(Player p);

    InternalResponseObject<Boolean> removeFromQueue(Player p);

    void start();

    void stop();
}
