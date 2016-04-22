package edu.cwru.eecs395_s16.services.containers;

import edu.cwru.eecs395_s16.services.bots.HeroRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.bots.MonsterRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.bots.PlayerRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.bots.SessionRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.cache.CacheService;
import edu.cwru.eecs395_s16.services.cache.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.cache.RedisCacheService;
import edu.cwru.eecs395_s16.services.heroes.HeroRepository;
import edu.cwru.eecs395_s16.services.heroes.InMemoryHeroRepository;
import edu.cwru.eecs395_s16.services.heroes.PostgresHeroRepository;
import edu.cwru.eecs395_s16.services.heroitems.HeroItemRepository;
import edu.cwru.eecs395_s16.services.heroitems.InMemoryHeroItemRepository;
import edu.cwru.eecs395_s16.services.heroitems.PostgresHeroItemRepository;
import edu.cwru.eecs395_s16.services.maps.InMemoryMapRepository;
import edu.cwru.eecs395_s16.services.maps.MapRepository;
import edu.cwru.eecs395_s16.services.maps.PostgresMapRepository;
import edu.cwru.eecs395_s16.services.matchmaking.MatchmakingService;
import edu.cwru.eecs395_s16.services.monsters.InMemoryMonsterRepository;
import edu.cwru.eecs395_s16.services.monsters.MonsterRepository;
import edu.cwru.eecs395_s16.services.monsters.PostgresMonsterRepository;
import edu.cwru.eecs395_s16.services.players.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.players.PlayerRepository;
import edu.cwru.eecs395_s16.services.players.PostgresPlayerRepository;
import edu.cwru.eecs395_s16.services.sessions.InMemorySessionRepository;
import edu.cwru.eecs395_s16.services.sessions.RedisSessionRepository;
import edu.cwru.eecs395_s16.services.sessions.SessionRepository;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.util.Map;

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
    public final MonsterRepository monsterRepository;

    protected ServiceContainer(Map<String, CoreDataUtils.CoreDataEntry> initialData, MapRepository mapRepository, HeroRepository heroRepository, CacheService cacheService, MatchmakingService matchService, SessionRepository sessionRepository, PlayerRepository playerRepository, HeroItemRepository heroItemRepository, MonsterRepository monsterRepository) {
        //Autowrap various repositories

        //Weapon Repository
        this.heroItemRepository = heroItemRepository;
        this.heroItemRepository.initialize(initialData);

        //Hero Repository
        HeroRepository hRepo = heroRepository;
        if (!(hRepo instanceof HeroRepositoryBotWrapper)) {
            hRepo = new HeroRepositoryBotWrapper(hRepo);
        }
        this.heroRepository = hRepo;
        this.heroRepository.initialize(initialData);

        MonsterRepository mRepo = monsterRepository;
        if (!(mRepo instanceof MonsterRepositoryBotWrapper)) {
            mRepo = new MonsterRepositoryBotWrapper(mRepo);
        }
        this.monsterRepository = mRepo;
        this.monsterRepository.initialize(initialData);

        //Player Repository
        PlayerRepository pRepo = playerRepository;
        if (!(pRepo instanceof PlayerRepositoryBotWrapper)) {
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

        //Session Repository
        SessionRepository sRepo = sessionRepository;
        if (!(sRepo instanceof SessionRepositoryBotWrapper)) {
            sRepo = new SessionRepositoryBotWrapper(sRepo);
        }
        this.sessionRepository = sRepo;
    }

    public static ServiceContainer buildPersistantContainer(Map<String, CoreDataUtils.CoreDataEntry> initialData, Connection dbConnection, JedisPool jedisPool, MatchmakingService matchService) {
        CoreDataUtils.setCreateSchemaMap("create_schema.sql");
        PlayerRepository repo = new PostgresPlayerRepository(dbConnection);
        return new ServiceContainer(initialData, new PostgresMapRepository(dbConnection), new PostgresHeroRepository(dbConnection), new RedisCacheService(jedisPool), matchService, new RedisSessionRepository(jedisPool, repo), repo, new PostgresHeroItemRepository(dbConnection), new PostgresMonsterRepository(dbConnection));
    }

    public static ServiceContainer buildInMemoryContainer(Map<String, CoreDataUtils.CoreDataEntry> initialData, MatchmakingService matchService) {
        return new ServiceContainer(initialData, new InMemoryMapRepository(), new InMemoryHeroRepository(), new InMemoryCacheService(), matchService, new InMemorySessionRepository(), new InMemoryPlayerRepository(), new InMemoryHeroItemRepository(), new InMemoryMonsterRepository());
    }

    public void cleanAndInit(Map<String, CoreDataUtils.CoreDataEntry> initialData) {
        //Weapon Repository
        this.heroItemRepository.resetToDefaultData(initialData);

        //Hero Repository
        this.heroRepository.resetToDefaultData(initialData);

        //Monster Repository
        this.monsterRepository.resetToDefaultData(initialData);

        //Player Repository
        this.playerRepository.resetToDefaultData(initialData);

        //Map Repository
        this.mapRepository.resetToDefaultData(initialData);

        //Session Repository
        //TODO implement expiring all sessions (potentially).
    }

}
