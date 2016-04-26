package edu.cwru.eecs395_s16.services.matchmaking;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.objectives.GameObjective;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueArchitectRequest;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueHeroesRequest;

/**
 * Created by james on 2/15/16.
 */
public interface MatchmakingService {
    InternalResponseObject<Boolean> queueAsHeroes(Player p, QueueHeroesRequest request, GameMap map, GameObjective
            objective);

    InternalResponseObject<Boolean> queueAsArchitect(Player p, QueueArchitectRequest request, GameMap map,
                                                     GameObjective objective);

    InternalResponseObject<Boolean> removeFromQueue(Player p);

    void start();

    void stop();

    String getQueueInformation();
}
