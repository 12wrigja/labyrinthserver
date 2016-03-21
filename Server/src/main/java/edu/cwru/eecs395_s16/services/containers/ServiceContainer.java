package edu.cwru.eecs395_s16.services.containers;

import edu.cwru.eecs395_s16.interfaces.repositories.*;
import edu.cwru.eecs395_s16.interfaces.services.CacheService;
import edu.cwru.eecs395_s16.interfaces.services.MatchmakingService;
import edu.cwru.eecs395_s16.services.bots.HeroRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.bots.PlayerRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.bots.SessionRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.cache.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.cache.RedisCacheService;
import edu.cwru.eecs395_s16.services.herorepository.InMemoryHeroRepository;
import edu.cwru.eecs395_s16.services.herorepository.PostgresHeroRepository;
import edu.cwru.eecs395_s16.services.maprepository.InMemoryMapRepository;
import edu.cwru.eecs395_s16.services.maprepository.PostgresMapRepository;
import edu.cwru.eecs395_s16.services.playerrepository.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.playerrepository.PostgresPlayerRepository;
import edu.cwru.eecs395_s16.services.sessionrepository.InMemorySessionRepository;
import edu.cwru.eecs395_s16.services.sessionrepository.RedisSessionRepository;
import edu.cwru.eecs395_s16.services.weaponrepository.InMemoryHeroItemRepository;
import edu.cwru.eecs395_s16.services.weaponrepository.PostgresHeroItemRepository;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.util.Map;
import java.util.stream.Collectors;

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
    public final HeroItemRepository heroItemRepository;

    protected ServiceContainer(Map<String,CoreDataUtils.CoreDataEntry> initialData, MapRepository mapRepository, HeroRepository heroRepository, CacheService cacheService, MatchmakingService matchService, SessionRepository sessionRepository, PlayerRepository playerRepository, HeroItemRepository heroItemRepository) {
        //Weapon Repository
        this.heroItemRepository = heroItemRepository;
        this.heroItemRepository.initialize(initialData);

        //Hero Repository
        HeroRepository hRepo = heroRepository;
        if(!(hRepo instanceof HeroRepositoryBotWrapper)){
            hRepo = new HeroRepositoryBotWrapper(hRepo);
        }
        this.heroRepository = hRepo;
        this.heroRepository.initialize(initialData);

        //Player Repository
        PlayerRepository pRepo = playerRepository;
        if(!(pRepo instanceof PlayerRepositoryBotWrapper)){
            pRepo = new PlayerRepositoryBotWrapper(pRepo);
        }
        this.playerRepository = pRepo;
        this.playerRepository.initialize(initialData);

        //Map Repository
        this.mapRepository = mapRepository;
        this.mapRepository.initialize(initialData);

        //Cache Service
        this.cacheService = cacheService;

        //Match Service
        this.matchService = matchService;

        //Autowrap various repositories
        //Session Repository
        SessionRepository sRepo = sessionRepository;
        if(!(sRepo instanceof SessionRepositoryBotWrapper)){
            sRepo = new SessionRepositoryBotWrapper(sRepo);
        }
        this.sessionRepository = sRepo;
    }

    public static ServiceContainer buildPersistantContainer(Map<String,CoreDataUtils.CoreDataEntry> initialData, Connection dbConnection, JedisPool jedisPool, MatchmakingService matchService){
        if(CoreDataUtils.runSQL(dbConnection,"create_schema.sql")){
            CoreDataUtils.insertIntoDB(dbConnection,initialData.values().stream().filter(entry -> entry.name.equals("rarities")).collect(Collectors.toList()));
            PlayerRepository repo = new PostgresPlayerRepository(dbConnection);
            return new ServiceContainer(initialData,new PostgresMapRepository(dbConnection),new PostgresHeroRepository(dbConnection),new RedisCacheService(jedisPool),matchService,new RedisSessionRepository(jedisPool,repo),repo,new PostgresHeroItemRepository(dbConnection));
        } else {
            throw new IllegalArgumentException("SQL in create_schema.sql is invalid.");
        }
    }

    public static ServiceContainer buildInMemoryContainer(Map<String,CoreDataUtils.CoreDataEntry> initialData, MatchmakingService matchService){
        return new ServiceContainer(initialData, new InMemoryMapRepository(), new InMemoryHeroRepository(), new InMemoryCacheService(), matchService, new InMemorySessionRepository(), new InMemoryPlayerRepository(), new InMemoryHeroItemRepository());
    }

}
