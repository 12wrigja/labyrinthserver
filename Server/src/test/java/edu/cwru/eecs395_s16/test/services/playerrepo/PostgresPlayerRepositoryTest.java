package edu.cwru.eecs395_s16.test.services.playerrepo;

import edu.cwru.eecs395_s16.services.players.PlayerRepository;
import edu.cwru.eecs395_s16.services.players.PostgresPlayerRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by james on 2/29/16.
 */
public class PostgresPlayerRepositoryTest extends PlayerRepositoryBaseTest {


    private static Connection conn;
    private static PostgresPlayerRepository repo;

    @BeforeClass
    public static void setupDBConnection() throws SQLException {
        conn = DriverManager.getConnection("jdbc:postgresql:vagrant", "vagrant", "vagrant");
        repo = new PostgresPlayerRepository(conn);
    }


    @Override
    public PlayerRepository getRepositoryImplementation() {
        return repo;
    }


    @AfterClass
    public static void teardownDBConnection() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }
}
