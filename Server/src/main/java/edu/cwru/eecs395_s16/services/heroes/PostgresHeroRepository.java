package edu.cwru.eecs395_s16.services.heroes;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.LevelReward;
import edu.cwru.eecs395_s16.services.containers.DBRepository;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.players.PostgresPlayerRepository;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by james on 2/18/16.
 */
public class PostgresHeroRepository extends DBRepository implements HeroRepository {

    private static final int NUM_NEW_HEROES = 6;
    public static final String HERO_PLAYER_TABLE = "hero_player";
    public static final String HEROES_TABLE = "heroes";
    public static final String LEVELS_TABLE = "levels";
    private static final String INSERT_DEFAULT_PLAYER_HEROES = "insert into " + HERO_PLAYER_TABLE + " (hero_id, player_id, experience, weapon_id, equipment_id) (select id as hero_id,? as player_id, 0 as experience, default_weapon as weapon_id , 4 from " + HEROES_TABLE + ")";
    private static final String DROP_ALL_PLAYER_HEROES = "delete from " + HERO_PLAYER_TABLE + " where player_id = ?";
    private static final String GET_HEROES_QUERY = "select * from " + HERO_PLAYER_TABLE + " inner join " + HEROES_TABLE + " on hero_player.hero_id = heroes.id where player_id = ?";
    private static final String GET_HERO_DEFINITION_QUERY = "select * from " + HEROES_TABLE + " where id = ?";
    private static final String GET_HERO_DEFINITION_BY_TYPE_QUERY = "select * from " + HEROES_TABLE + " where class = ?";
    private static final String GET_LEVEL_REWARD_QUERY = "select * from "+LEVELS_TABLE+" where class = ? and experience <= ? and experience > ?";
    private static final String UPDATE_PLAYER_HERO_QUERY = "update "+HERO_PLAYER_TABLE+" set experience=?, weapon_id = ? where player_id = ? and hero_id = ?";

    public PostgresHeroRepository(Connection conn) {
        this(conn, CoreDataUtils.defaultCoreData());
    }

    public PostgresHeroRepository(Connection conn, Map<String, CoreDataUtils.CoreDataEntry> baseData) {
        super(conn);
    }

    @Override
    public InternalResponseObject<List<Hero>> getPlayerHeroes(Player p) {
        try {
            List<Hero> heroes = new ArrayList<>();
            PreparedStatement stmt = conn.prepareStatement(GET_HEROES_QUERY);
            stmt.setInt(1, p.getDatabaseID());
            ResultSet rst = stmt.executeQuery();
            while (rst.next()) {
                Hero h = heroFromResultSet(p, rst);
                heroes.add(h);
            }
            return new InternalResponseObject<>(heroes, HEROES_TABLE);
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.INVALID_USERNAME, "Unable to retrieve " + HEROES_TABLE + " from Postgres for the given username.");
        }
    }

    @Override
    public InternalResponseObject<Boolean> saveHeroForPlayer(Player p, Hero h) {
        //Need to save: hero level, equipped weapon, and equipped equipment
        try{
            PreparedStatement stmt = conn.prepareStatement(UPDATE_PLAYER_HERO_QUERY);
            stmt.setLong(1,h.getExp());
            stmt.setInt(2,h.getWeapon().getDatabaseID());
            stmt.setInt(3,p.getDatabaseID());
            stmt.setInt(4,h.getHeroType().databaseIdentifier);
            int affected = stmt.executeUpdate();
            if(affected == 1){
                return new InternalResponseObject<>(true,"saved");
            } else {
                return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR);
            }
        } catch (SQLException e){
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    public InternalResponseObject<Boolean> createDefaultHeroesForPlayer(Player p) {
        if (p.getDatabaseID() < 0) {
            return new InternalResponseObject<>(InternalErrorCode.INVALID_DB_IDENTIFIER);
        }
        //Set up hero data links here
        try {
            //TODO determine what error checking should be here for update counts.
            PreparedStatement stmt = conn.prepareStatement(DROP_ALL_PLAYER_HEROES);
            stmt.setInt(1, p.getDatabaseID());
            stmt.executeUpdate();

            stmt = conn.prepareStatement(INSERT_DEFAULT_PLAYER_HEROES);
            stmt.setInt(1, p.getDatabaseID());
            int results = stmt.executeUpdate();
            if (!(results == NUM_NEW_HEROES)) {
                return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.INVALID_SQL);
            }
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.INVALID_SQL, "Unable to create default " + HEROES_TABLE + " for player.");
        }
        return new InternalResponseObject<>(true, "created");
    }

    @Override
    public List<LevelReward> getLevelRewards(HeroType type, long previousExperience, long newExperience) {
        List<LevelReward> rewards = new ArrayList<>();
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_LEVEL_REWARD_QUERY);
            stmt.setString(1,type.toString().toLowerCase());
            stmt.setLong(2, newExperience);
            stmt.setLong(3,previousExperience);
            ResultSet rslts= stmt.executeQuery();
            while(rslts.next()){
                long experienceThreshold = rslts.getLong("experience");
                int levelAwarded = rslts.getInt("level");
                String rewardStr = rslts.getString("upgrade");
                rewards.add(buildReward(type,levelAwarded,experienceThreshold,rewardStr));
            }
        } catch (SQLException e){
            if(GameEngine.instance().IS_DEBUG_MODE){
                e.printStackTrace();
            }
        }
        return rewards;
    }

    @Override
    public InternalResponseObject<HeroDefinition> getHeroDefinitionForId(int id) {
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_HERO_DEFINITION_QUERY);
            stmt.setInt(1, id);
            ResultSet set = stmt.executeQuery();
            set.next();
            int attack = set.getInt("attack");
            int defense = set.getInt("defense");
            int health = set.getInt("health");
            int movement = set.getInt("movement");
            int vision = set.getInt("vision");
            String className = set.getString("class").toUpperCase();
            int weaponID = set.getInt("default_weapon");
            HeroType type = HeroType.valueOf(className);
            return new InternalResponseObject<>(new HeroDefinition(id,type,attack,defense,health,movement,vision,weaponID), Hero.HERO_TYPE_KEY);
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(InternalErrorCode.INVALID_SQL);
        } catch (IllegalArgumentException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    public InternalResponseObject<HeroDefinition> getHeroDefinitionForType(HeroType type) {
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_HERO_DEFINITION_BY_TYPE_QUERY);
            stmt.setString(1, type.toString().toLowerCase());
            ResultSet set = stmt.executeQuery();
            set.next();
            int id = set.getInt("id");
            int attack = set.getInt("attack");
            int defense = set.getInt("defense");
            int health = set.getInt("health");
            int movement = set.getInt("movement");
            int vision = set.getInt("vision");
            String className = set.getString("class").toUpperCase();
            int weaponID = set.getInt("default_weapon");
            HeroType actType = HeroType.valueOf(className);
            return new InternalResponseObject<>(new HeroDefinition(id,actType,attack,defense,health,movement,vision,weaponID), Hero.HERO_TYPE_KEY);
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(InternalErrorCode.INVALID_SQL);
        } catch (IllegalArgumentException e) {
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
                add(HEROES_TABLE);
                add(HERO_PLAYER_TABLE);
                add(PostgresPlayerRepository.PLAYERS_TABLE);
                add(LEVELS_TABLE);
            }
        };
    }

    private Hero heroFromResultSet(Player p, ResultSet r) throws SQLException {
        int heroID = r.getInt("hero_id");
        int attack = r.getInt("attack");
        int defense = r.getInt("defense");
        int health = r.getInt("health");
        int movement = r.getInt("movement");
        int vision = r.getInt("vision");
        long exp = r.getLong("experience");
        String heroType = r.getString("class");
        HeroType type = HeroType.valueOf(heroType.toUpperCase());
        HeroBuilder hb = new HeroBuilder(p.getUsername(), type);
        hb.setDatabaseIdentifier(heroID)
                .setAttack(attack)
                .setDefense(defense)
                .setHealth(health)
                .setMovement(movement)
                .setVision(vision)
                .setExp(exp,true);
        return hb.createHero();
    }
}