package edu.cwru.eecs395_s16.interfaces.services;

import edu.cwru.eecs395_s16.core.InvalidGameStateException;
import edu.cwru.eecs395_s16.core.Player;

/**
 * Created by james on 2/15/16.
 */
public interface MatchmakingService {
    boolean queueAsHeroes(Player p) throws InvalidGameStateException;

    boolean queueAsArchitect(Player p) throws InvalidGameStateException;

    boolean removeFromQueue(Player p) throws InvalidGameStateException;

    void start();

    void stop();
}
