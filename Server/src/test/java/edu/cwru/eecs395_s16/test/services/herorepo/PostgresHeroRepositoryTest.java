package edu.cwru.eecs395_s16.test.services.herorepo;

import edu.cwru.eecs395_s16.services.containers.ServiceContainer;
import edu.cwru.eecs395_s16.services.matchmaking.BasicMatchmakingService;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by james on 3/16/16.
 */
public class PostgresHeroRepositoryTest extends HeroRepositoryBaseTest {

    private static Connection conn;
    private static JedisPool jedisPool;

    @BeforeClass
    public static void setupPersistenceConnections() throws SQLException {
        conn = DriverManager.getConnection("jdbc:postgresql:vagrant", "vagrant", "vagrant");
        jedisPool = new JedisPool("localhost");
    }

    @AfterClass
    public static void teardownPersistenceConnections() throws SQLException {
        if (conn != null) {
            conn.close();
        }
        jedisPool.destroy();
    }

    @Override
    public ServiceContainer buildContainer() {
        return ServiceContainer.buildPersistantContainer(CoreDataUtils.defaultCoreData(), conn, jedisPool, new
                BasicMatchmakingService());
    }
}
