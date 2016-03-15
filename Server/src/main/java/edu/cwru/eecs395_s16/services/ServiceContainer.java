package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.interfaces.repositories.*;
import edu.cwru.eecs395_s16.interfaces.services.MatchmakingService;

import java.util.Timer;

/**
 * Created by james on 3/13/16.
 */
public class ServiceContainer {

    public final PlayerRepository playerRepository;
    public final SessionRepository sessionRepository;
    public final MatchmakingService matchService;
    public final CacheService cacheService;
    public final HeroRepository heroRepository;
    public final MapRepository mapRepository;

    public ServiceContainer(MapRepository mapRepository, HeroRepository heroRepository, CacheService cacheService, MatchmakingService matchService, SessionRepository sessionRepository, PlayerRepository playerRepository) {
        this.mapRepository = mapRepository;
        this.heroRepository = heroRepository;
        this.cacheService = cacheService;
        this.matchService = matchService;
        this.sessionRepository = sessionRepository;
        this.playerRepository = playerRepository;
    }


}
