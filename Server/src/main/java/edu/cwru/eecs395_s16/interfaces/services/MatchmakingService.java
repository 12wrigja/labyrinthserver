package edu.cwru.eecs395_s16.interfaces.services;

import edu.cwru.eecs395_s16.core.Player;

/**
 * Created by james on 2/15/16.
 */
public interface MatchmakingService {
    boolean queueAsHeroes(Player p);

    boolean queueAsArchitect(Player p);

    boolean removeFromQueue(Player p);

    void start();

    void stop();
}
