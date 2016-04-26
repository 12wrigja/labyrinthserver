package edu.cwru.eecs395_s16.services.heroitems;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.core.objects.creatures.Equipment;
import edu.cwru.eecs395_s16.core.objects.creatures.UsePattern;
import edu.cwru.eecs395_s16.core.objects.creatures.Weapon;
import edu.cwru.eecs395_s16.services.containers.DBRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by james on 3/17/16.
 */
public class PostgresHeroItemRepository extends DBRepository implements HeroItemRepository {

    public static final String HERO_ITEM_TABLE = "hero_items";
    public static final String RARITY_ITEM_TABLE = "rarities";
    public static final String GET_WEAPON_QUERY = "select * from " + HERO_ITEM_TABLE + " where type = 'weapon' and id" +
            " = ?";
    public static final String GET_EQUIPMENT_QUERY = "select * from " + HERO_ITEM_TABLE + " where type = 'equipment' " +
            "and id = ?";
    public static final String USE_PATTERNS = "use_patterns";
    public static final String GET_USE_PATTERN_QUERY = "select * from " + USE_PATTERNS + " where id = ?";
    public static final String USE_PATTERN_TILES = "use_pattern_tiles";
    public static final String GET_USE_PATTERN_TILES_QUERY = "select * from " + USE_PATTERN_TILES + " where " +
            "pattern_id = ?";


    private static Map<Integer, UsePattern> patternMap;

    public PostgresHeroItemRepository(Connection conn) {
        super(conn);
    }

    @Override
    public Optional<Weapon> getWeaponForId(int id) {
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_WEAPON_QUERY);
            stmt.setInt(1, id);
            ResultSet rslts = stmt.executeQuery();
            if (rslts.next()) {
                String name = rslts.getString("name");
                String image = rslts.getString("image");
                String description = rslts.getString("description");
                int attack_change = rslts.getInt("attack_change");
                int patternID = rslts.getInt("use_pattern_id");
                int range = rslts.getInt("use_range");

                stmt = conn.prepareStatement(GET_USE_PATTERN_QUERY);
                stmt.setInt(1, patternID);
                rslts = stmt.executeQuery();
                rslts.next();
                int patternInputs = rslts.getInt("inputs");
                boolean rotatable = rslts.getBoolean("rotatable");

                stmt = conn.prepareStatement(GET_USE_PATTERN_TILES_QUERY);
                stmt.setInt(1, patternID);
                Map<Location, Float> useEffectMap = new HashMap<>();
                rslts = stmt.executeQuery();
                while (rslts.next()) {
                    useEffectMap.put(new Location(rslts.getInt("x"), rslts.getInt("y")), 100f / rslts.getInt("effect"));
                }
                UsePattern pattern = new UsePattern(patternInputs, rotatable, useEffectMap);
                Weapon weapon = new Weapon(id, image, name, description, range, attack_change, pattern);
                return Optional.of(weapon);
            }
            return Optional.empty();
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return Optional.empty();
        }
    }

    @Override
    public Optional<Equipment> getEquipmentForId(int id) {
        return null;
    }

    @Override
    protected List<String> getTables() {
        return new ArrayList<String>() {
            {
                add(HERO_ITEM_TABLE);
                add(USE_PATTERNS);
                add(USE_PATTERN_TILES);
                add(RARITY_ITEM_TABLE);
            }
        };
    }
}
