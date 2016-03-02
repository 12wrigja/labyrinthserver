package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.GameEngine;
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
    private static final String INSERT_PLAYER_QUERY = "insert into players (username, password, currency, is_dev) VALUES (?,?,?,false)";
    private static final String PLAYER_EXISTS_QUERY = "select count(*) as total from players where username = ?";
    private static final String VALID_LOGIN_QUERY = "select * from players where username = ? AND password = ?";
    private static final String DELETE_PLAYER_QUERY = "delete from players where user_id = ?";
    private static final String INSERT_DEFAULT_PLAYER_HEROES = "insert into hero_player (hero_id, player_id, level) (select id as hero_id,? as player_id, 1 as level from heroes)";

    final Connection conn;

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
                stmt = conn.prepareStatement(INSERT_PLAYER_QUERY, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setInt(3, 0);
                stmt.executeUpdate();
                ResultSet newKeys = stmt.getGeneratedKeys();
                int playerDBID = -1;
                while(newKeys.next()){
                    playerDBID = newKeys.getInt(1);
                }
                Player p = new Player(playerDBID,username, password);

                //Set up hero data links here
                stmt = conn.prepareStatement(INSERT_DEFAULT_PLAYER_HEROES);
                stmt.setInt(1,playerDBID);
                stmt.executeUpdate();
                return Optional.of(p);
            }
        } catch (SQLException e) {
            if(GameEngine.instance().IS_DEBUG_MODE){
                e.printStackTrace();
            }
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
                int id = -1;
                while (rst2.next()) {
                    id = rst2.getInt("id");
                }
                if (id >= 0) {
                    Player p = new Player(id,username, password);
                    return Optional.of(p);
                } else {
                    throw new InvalidPasswordException();
                }
            } else {
                throw new UnknownUsernameException(username);
            }
        } catch (SQLException e) {
            if(GameEngine.instance().IS_DEBUG_MODE){
                e.printStackTrace();
            }
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
                int id = rst2.getInt("id");
                String password = rst2.getString("password");
                Player p = new Player(id,username, password);
                return Optional.of(p);
            } else {
                throw new UnknownUsernameException(username);
            }
        } catch (SQLException e) {
            if(GameEngine.instance().IS_DEBUG_MODE){
                e.printStackTrace();
            }
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
