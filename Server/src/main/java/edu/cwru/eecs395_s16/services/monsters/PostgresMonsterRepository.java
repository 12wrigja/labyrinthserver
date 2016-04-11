package edu.cwru.eecs395_s16.services.monsters;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.core.objects.creatures.Creature;
import edu.cwru.eecs395_s16.core.objects.creatures.CreatureBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.LevelReward;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterBuilder;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.containers.DBRepository;
import edu.cwru.eecs395_s16.services.players.PostgresPlayerRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/18/16.
 */
public class PostgresMonsterRepository extends DBRepository implements MonsterRepository {

    public static final String MONSTER_PLAYER_TABLE = "monster_player";
    public static final String MONSTERS_TABLE = "monsters";
    private static final String INSERT_DEFAULT_PLAYER_HEROES = "insert into " + MONSTER_PLAYER_TABLE + " (monster_id, player_id, quantity) select id as monster_id, ? as player_id, 0 as quantity from monsters";
    private static final String UPDATE_MONSTER_QUANTITY_QUERY = "update "+MONSTER_PLAYER_TABLE +" set quantity = quantity + ? where player_id = ? and monster_id = ?";
    private static final String REMOVE_ALL_PLAYER_MONSTERS = "delete from " + MONSTER_PLAYER_TABLE + " where player_id = ?";
    private static final String GET_MONSTERS_QUERY = "select * from " + MONSTER_PLAYER_TABLE + " inner join " + MONSTERS_TABLE + " on monster_player.monster_id = monsters.id where player_id = ?";
    private static final String GET_MONSTER_DEFINITION_QUERY = "select * from " + MONSTERS_TABLE + " where id = ?";

    public PostgresMonsterRepository(Connection conn) {
        super(conn);
    }

    @Override
    public InternalResponseObject<List<MonsterDefinition>> getPlayerMonsterTypes(Player p) {
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_MONSTERS_QUERY);
            stmt.setInt(1, p.getDatabaseID());
            ResultSet rslts = stmt.executeQuery();
            List<MonsterDefinition> definitions = new ArrayList<>();
            while (rslts.next()) {
                definitions.add(monsterDefinitionFromResultSetWithCounts(rslts));
            }
            if (definitions.size() == 0) {
                return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_USERNAME);
            } else {
                return new InternalResponseObject<>(definitions, "monsters");
            }
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_USERNAME);
        }
    }

    @Override
    public InternalResponseObject<Boolean> addMonsterForPlayer(Player p, MonsterDefinition monsterDefinition, int quantity) {
        try {
            PreparedStatement stmt = conn.prepareStatement(UPDATE_MONSTER_QUANTITY_QUERY);
            stmt.setInt(1,quantity);
            stmt.setInt(2,p.getDatabaseID());
            stmt.setInt(3,monsterDefinition.id);
            int results = stmt.executeUpdate();
            if (results > 0) {
                return new InternalResponseObject<>(true, "added");
            } else {
                return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.INVALID_SQL);
            }
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.INVALID_SQL);
        }
    }

    @Override
    public InternalResponseObject<Boolean> createDefaultMonstersForPlayer(Player p) {
        try {
            PreparedStatement stmt = conn.prepareStatement(REMOVE_ALL_PLAYER_MONSTERS);
            stmt.setInt(1,p.getDatabaseID());
            stmt.executeUpdate();
            stmt = conn.prepareStatement(INSERT_DEFAULT_PLAYER_HEROES);
            stmt.setInt(1,p.getDatabaseID());
            int results = stmt.executeUpdate();
            if (results > 0) {
                InternalResponseObject<MonsterDefinition> goblinDef = getMonsterDefinitionForId(1);
                if(goblinDef.isNormal()) {
                    InternalResponseObject<Boolean> goblinCreateResp = addMonsterForPlayer(p, goblinDef.get(), 10);
                    if(goblinCreateResp.isNormal()) {
                        return new InternalResponseObject<>(true, "created");
                    } else {
                        return InternalResponseObject.cloneError(goblinCreateResp,"Unable to give new player goblins.");
                    }
                } else {
                    return InternalResponseObject.cloneError(goblinDef,"Unable to find the goblin definition.");
                }
            } else {
                return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.INVALID_SQL);
            }
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.INVALID_SQL);
        }
    }

    @Override
    public InternalResponseObject<MonsterDefinition> getMonsterDefinitionForId(int id) {
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_MONSTER_DEFINITION_QUERY);
            stmt.setInt(1, id);
            ResultSet rslts = stmt.executeQuery();
            MonsterDefinition def;
            if (rslts.next()) {
                def = monsterDefinitionFromResultSet(rslts);
                return new InternalResponseObject<>(def, "monster_definition");
            } else {
                return new InternalResponseObject<>(InternalErrorCode.INVALID_DB_IDENTIFIER, "There is no monster definition for this id.");
            }
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR);
        }
    }


    @Override
    protected List<String> getTables() {
        return new ArrayList<String>() {
            {
                add(MONSTERS_TABLE);
                add(MONSTER_PLAYER_TABLE);
                add(PostgresPlayerRepository.PLAYERS_TABLE);
            }
        };
    }

    private MonsterDefinition monsterDefinitionFromResultSetWithCounts(ResultSet r) throws SQLException {
        int monsterDefId = r.getInt("monster_id");
        String name = r.getString("name");
        int attack = r.getInt("attack");
        int defense = r.getInt("defense");
        int health = r.getInt("health");
        int movement = r.getInt("movement");
        int vision = r.getInt("vision");
        int quantity = r.getInt("quantity");
        int weapon_id = r.getInt("weapon_id");
        return new MonsterDefinition(monsterDefId, name, attack, defense, health, movement, vision, weapon_id, quantity);
    }

    private MonsterDefinition monsterDefinitionFromResultSet(ResultSet r) throws SQLException {
        int monsterDefId = r.getInt("id");
        String name = r.getString("name");
        int attack = r.getInt("attack");
        int defense = r.getInt("defense");
        int health = r.getInt("health");
        int movement = r.getInt("movement");
        int vision = r.getInt("vision");
        int quantity = 0;
        int weapon_id = r.getInt("weapon_id");
        return new MonsterDefinition(monsterDefId, name, attack, defense, health, movement, vision, weapon_id, quantity);
    }
}
