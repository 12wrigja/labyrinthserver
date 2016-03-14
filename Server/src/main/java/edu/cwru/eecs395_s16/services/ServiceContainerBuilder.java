package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.interfaces.repositories.*;
import edu.cwru.eecs395_s16.interfaces.services.MatchmakingService;
import edu.cwru.eecs395_s16.networking.matchmaking.BasicMatchmakingService;
import edu.cwru.eecs395_s16.services.maprepository.InMemoryMapRepository;
import edu.cwru.eecs395_s16.services.bots.PlayerRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.bots.SessionRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.cache.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.herorepository.InMemoryHeroRepository;
import edu.cwru.eecs395_s16.services.playerrepository.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.sessionrepository.InMemorySessionRepository;

import java.util.Timer;

public class ServiceContainerBuilder {
    private Timer gameTimer = new Timer();
    private MapRepository mapRepository = new InMemoryMapRepository();
    private HeroRepository heroRepository = new InMemoryHeroRepository();
    private CacheService cacheService = new InMemoryCacheService();
    private MatchmakingService matchService = new BasicMatchmakingService();
    private SessionRepository sessionRepository = new SessionRepositoryBotWrapper(new InMemorySessionRepository());
    private PlayerRepository playerRepository = new PlayerRepositoryBotWrapper(new InMemoryPlayerRepository());

    public ServiceContainerBuilder setGameTimer(Timer gameTimer) {
        this.gameTimer = gameTimer;
        return this;
    }

    public ServiceContainerBuilder setMapRepository(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
        return this;
    }

    public ServiceContainerBuilder setHeroRepository(HeroRepository heroRepository) {
        this.heroRepository = heroRepository;
        return this;
    }

    public ServiceContainerBuilder setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
        return this;
    }

    public ServiceContainerBuilder setMatchService(MatchmakingService matchService) {
        this.matchService = matchService;
        return this;
    }

    public ServiceContainerBuilder setSessionRepository(SessionRepository sessionRepository) {
        this.sessionRepository = new SessionRepositoryBotWrapper(sessionRepository);
        return this;
    }

    public ServiceContainerBuilder setPlayerRepository(PlayerRepository playerRepository) {
        this.playerRepository = new PlayerRepositoryBotWrapper(playerRepository);
        return this;
    }

    public ServiceContainer createServiceContainer() {
        return new ServiceContainer(gameTimer, mapRepository, heroRepository, cacheService, matchService, sessionRepository, playerRepository);
    }
}