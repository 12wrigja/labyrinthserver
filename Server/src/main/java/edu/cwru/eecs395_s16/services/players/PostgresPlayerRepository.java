package edu.cwru.eecs395_s16.services.players;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.services.containers.DBRepository;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 2/15/16.
 */
public class PostgresPlayerRepository extends DBRepository implements PlayerRepository {

    public static final String PLAYERS_TABLE = "players";
    private static final String GET_PLAYER_QUERY = "select * from " + PLAYERS_TABLE + " where username = ?";
    private static final String INSERT_PLAYER_QUERY = "insert into " + PLAYERS_TABLE + " (username, password, currency, is_dev) VALUES (?,?,?,false)";
    private static final String PLAYER_EXISTS_QUERY = "select count(*) as total from " + PLAYERS_TABLE + " where username = ?";
    private static final String VALID_LOGIN_QUERY = "select * from " + PLAYERS_TABLE + " where username = ? AND password = ?";
    private static final String DELETE_PLAYER_QUERY = "delete from " + PLAYERS_TABLE + " where id = ?";

    public PostgresPlayerRepository(Connection dbConnection) {
        super(dbConnection);
    }

    @Override
    public InternalResponseObject<Player> registerPlayer(String username, String password, String passwordConfirm) {
        if (!username.matches("[a-zA-Z0-9]+")) {
            return new InternalResponseObject<>(InternalErrorCode.INVALID_USERNAME);
        }
        if (password == null || passwordConfirm == null || !password.equals(passwordConfirm)) {
            return new InternalResponseObject<>(InternalErrorCode.MISMATCHED_PASSWORD);
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
                return new InternalResponseObject<>(InternalErrorCode.DUPLICATE_USERNAME);
            } else {
                stmt = conn.prepareStatement(INSERT_PLAYER_QUERY, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setInt(3, 0);
                stmt.executeUpdate();
                ResultSet newKeys = stmt.getGeneratedKeys();
                int playerDBID = -1;
                while (newKeys.next()) {
                    playerDBID = newKeys.getInt(1);
                }
                Player p = new Player(playerDBID, username, password, false);
                return new InternalResponseObject<>(p,"player");
            }
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR);
        }
    }

    @Override
    public InternalResponseObject<Player> loginPlayer(String username, String password) {
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
                    Player p = new Player(id, username, password, false);
                    return new InternalResponseObject<>(p,"player");
                } else {
                    return new InternalResponseObject<>(InternalErrorCode.INVALID_PASSWORD);
                }
            } else {
                return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_USERNAME);
            }
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR);
        }
    }

    @Override
    public InternalResponseObject<Player> findPlayer(String username) {
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
                Player p = new Player(id, username, password, false);
                return new InternalResponseObject<>(p,"player");
            } else {
                return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_USERNAME);
            }
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR);
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
        if (p.getDatabaseID() >= 0) {
            return deletePlayerViaID(p.getDatabaseID());
        } else {
            //Check and see if we can delete by username
            InternalResponseObject<Player> p1 = findPlayer(p.getUsername());
            if (p1.isNormal()) {
                p = p1.get();
                return deletePlayerViaID(p.getDatabaseID());
            } else {
                return false;
            }
        }
    }

    private boolean deletePlayerViaID(int id) {
        if (id >= 0) {
            try {
                PreparedStatement stmt = conn.prepareStatement(DELETE_PLAYER_QUERY);
                stmt.setInt(1, id);
                int numChanged = stmt.executeUpdate();
                return numChanged == 1;
            } catch (SQLException e) {
                if (GameEngine.instance() == null || GameEngine.instance().IS_DEBUG_MODE) {
                    e.printStackTrace();
                }
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected List<String> getTables() {
        return new ArrayList<String>(){
            {
                add(PLAYERS_TABLE);
            }
        };
    }
}
