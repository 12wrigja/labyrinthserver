package edu.cwru.eecs395_s16.test.persistance;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.fail;

/**
 * Created by james on 2/17/16.
 */
public class PostgresConfigTest {

    @Test
    public void testPostgresConfig() throws SQLException {
        try {
            Connection c = DriverManager.getConnection("jdbc:postgresql:vagrant", "vagrant", "vagrant");
            c.close();
        } catch (SQLException e) {
            fail("Unable to get db connection.");
        }
    }


}
