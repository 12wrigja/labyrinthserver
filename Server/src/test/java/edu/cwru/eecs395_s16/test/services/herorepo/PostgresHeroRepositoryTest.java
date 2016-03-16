package edu.cwru.eecs395_s16.test.services.herorepo;

import edu.cwru.eecs395_s16.services.ServiceContainer;
import edu.cwru.eecs395_s16.services.ServiceContainerBuilder;
import edu.cwru.eecs395_s16.services.herorepository.PostgresHeroRepository;
import edu.cwru.eecs395_s16.services.playerrepository.PostgresPlayerRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by james on 3/16/16.
 */
public class PostgresHeroRepositoryTest extends HeroRepositoryBaseTest {

    private static Connection conn;
    private static PostgresHeroRepository hRepo;
    private static PostgresPlayerRepository pRepo;

    @BeforeClass
    public static void setupDBConnection() throws SQLException {
        conn = DriverManager.getConnection("jdbc:postgresql:vagrant", "vagrant", "vagrant");
        hRepo = new PostgresHeroRepository(conn);
        pRepo = new PostgresPlayerRepository(conn);
    }

    @Override
    public ServiceContainer buildContainer() {
        ServiceContainerBuilder scb = new ServiceContainerBuilder();
        scb.setHeroRepository(hRepo);
        scb.setPlayerRepository(pRepo);
        return scb.createServiceContainer();
    }

    @AfterClass
    public static void teardownDBConnection() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }
}
