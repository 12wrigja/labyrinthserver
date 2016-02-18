package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.auth.exceptions.DuplicateUsernameException;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.MismatchedPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.interfaces.repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Player;

import java.sql.*;
import java.util.Optional;

/**
 * Created by james on 2/15/16.
 */
public class PostgresPlayerRepository implements PlayerRepository {

    private static final String GET_PLAYER_QUERY = "select * from players where username = ?";
    private static final String INSERT_PLAYER_QUERY = "insert into players VALUES (?,?,?,false)";
    private static final String PLAYER_EXISTS_QUERY = "select count(*) as total from players where username = ?";
    private static final String VALID_LOGIN_QUERY = "select count(*) as total from players where username = ? AND password = ?";
    private static final String DELETE_PLAYER_QUERY = "delete from players where user_id = ?";

    Connection conn;

    public PostgresPlayerRepository(Connection dbConnection) {
        this.conn = dbConnection;
    }

    @Override
    public Optional<Player> registerPlayer(String username, String password, String passwordConfirm) throws DuplicateUsernameException, MismatchedPasswordException {
        if (!password.equals(passwordConfirm)) {
            throw new MismatchedPasswordException();
        }
        try {
            PreparedStatement stmt = conn.prepareStatement(PLAYER_EXISTS_QUERY);
            stmt.setString(1, username);
            ResultSet rst = stmt.executeQuery();
            int count = 0;
            while (rst.next()) {
                count = rst.getInt("total");
            }
            if (count >= 1) {
                throw new DuplicateUsernameException(username);
            } else {
                stmt = conn.prepareStatement(INSERT_PLAYER_QUERY);
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setInt(3, 0);
                stmt.executeUpdate();
                Player p = new Player(username, password);
                return Optional.of(p);
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Player> loginPlayer(String username, String password) throws UnknownUsernameException, InvalidPasswordException {
        try {
            PreparedStatement stmt = conn.prepareStatement(PLAYER_EXISTS_QUERY);
            stmt.setString(1, username);
            ResultSet rst = stmt.executeQuery();
            int count = 0;
            while (rst.next()) {
                count = rst.getInt("total");
            }
            if (count >= 1) {
                stmt = conn.prepareStatement(VALID_LOGIN_QUERY);
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rst2 = stmt.executeQuery();
                int validCount = 0;
                while (rst2.next()) {
                    validCount = rst2.getInt("total");
                }
                if (validCount >= 1) {
                    Player p = new Player(username, password);
                    return Optional.of(p);
                } else {
                    throw new InvalidPasswordException();
                }
            } else {
                throw new UnknownUsernameException(username);
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Player> findPlayer(String username) throws UnknownUsernameException {
        try {
            PreparedStatement stmt = conn.prepareStatement(PLAYER_EXISTS_QUERY);
            stmt.setString(1, username);
            ResultSet rst = stmt.executeQuery();
            int count = 0;
            while (rst.next()) {
                count = rst.getInt("total");
            }
            if (count >= 1) {
                stmt = conn.prepareStatement(GET_PLAYER_QUERY);
                stmt.setString(1, username);
                ResultSet rst2 = stmt.executeQuery();
                //TODO convert player row to player object
                rst2.next();
                String password = rst2.getString("password");
                Player p = new Player(username, password);
                return Optional.of(p);
            } else {
                throw new UnknownUsernameException(username);
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean savePlayer(Player p) {
        //This updates the player info that might have changed over time in the app.
        //This is probably going to be limited to currency for this application, although it might use other repositories to save off store info, etc.
        return false;
    }

    @Override
    public boolean deletePlayer(Player p) {
        return false;
    }
}
