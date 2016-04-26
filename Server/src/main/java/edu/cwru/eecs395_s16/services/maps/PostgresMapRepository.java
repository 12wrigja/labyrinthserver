package edu.cwru.eecs395_s16.services.maps;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.maps.FromDatabaseMap;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.maps.MapTile;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.containers.DBRepository;

import java.sql.*;
import java.util.*;

/**
 * Created by james on 3/1/16.
 */
public class PostgresMapRepository extends DBRepository implements MapRepository {

    public static final String MAPS_TABLE = "maps";
    public static final String TILE_MAP_TABLE = "tile_map";
    public static final String TILES_TABLE = "tiles";
    private static final String GET_MAP_QUERY = "select maps.*, players.username as username from " + MAPS_TABLE + " " +
            "inner join players on maps.creator_id = players.id where " + MAPS_TABLE + ".id = ?";
    private static final String GET_ALL_MAPS_QUERY = "select maps.*, players.username as username from " + MAPS_TABLE
            + " inner join players on maps.creator_id = players.id";
    private static final String GET_MAP_TILES_QUERY = "select * from " + TILE_MAP_TABLE + " where map_id = ?";
    private static final String GET_TILE_TYPES = "select * from " + TILES_TABLE;

    private static final String INSERT_MAP_QUERY = "insert into maps (id,map_name,creator_id,width,depth," +
            "hero_capacity) VALUES (default,?,?,?,?,?);";
    private static final String INSERT_MAP_TILE_QUERY = "insert into tile_map VALUES (?,?,?,?,?,?,?,?)";

    final Map<Integer, TileType> tileTypeMap;
    final Map<String, TileType> tileTypeStringMap;

    public PostgresMapRepository(Connection conn) {
        super(conn);
        tileTypeMap = new HashMap<>();
        tileTypeStringMap = new HashMap<>();
        getTileTypeMap();
    }

    @Override
    public InternalResponseObject<GameMap> getMapByID(int id) {
        try {
            PreparedStatement mapBase = conn.prepareStatement(GET_MAP_QUERY);
            mapBase.setInt(1, id);
            ResultSet r = mapBase.executeQuery();
            if (r.next()) {
                String name = r.getString("map_name");
                String creatorID = r.getString("username");
                int width = r.getInt("width");
                int height = r.getInt("depth");
                int heroCapacity = r.getInt("hero_capacity");
                FromDatabaseMap mp = new FromDatabaseMap(id, name, creatorID, width, height, heroCapacity);

                //Get tiles for map
                PreparedStatement tileStmt = conn.prepareStatement(GET_MAP_TILES_QUERY);
                tileStmt.setInt(1, id);
                r = tileStmt.executeQuery();
                int count = 0;
                while (r.next()) {
                    count++;
                    int x = r.getInt("x");
                    int y = r.getInt("y");
                    boolean isHeroSpawnTile = r.getBoolean("is_hero_spawn");
                    boolean isArchitectSpawnTile = r.getBoolean("is_architect_spawn");
                    boolean isObjectiveSpawnTile = r.getBoolean("is_objective_spawn");
                    int rotation = r.getInt("rotation");
                    int tileID = r.getInt("tile_id");
                    TileType tileType = tileTypeMap.get(tileID);
                    MapTile tile = new MapTile(x, y, tileType, rotation, isHeroSpawnTile, isArchitectSpawnTile,
                            isObjectiveSpawnTile);
                    mp.setTile(x, y, tile);
                }
                if (!(count == (mp.getSizeX() * mp.getSizeY()))) {
                    //Throw issue here - map is not fully defined.
                    return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode
                            .INVALID_MAP_DEFINITION);
                }
                return new InternalResponseObject<>(mp, "map");
            } else {
                //Throw error - can't find that map.
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode
                        .UNKNOWN_MAP_IDENTIFIER);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.INVALID_SQL);
        }
    }

    @Override
    public InternalResponseObject<Integer> storeNewMapInDatabase(String mapName, Player creator, GameMap map) {
        try {
            PreparedStatement stmt = conn.prepareStatement(INSERT_MAP_QUERY, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, mapName);
            stmt.setInt(2, creator.getDatabaseID());
            stmt.setInt(3, map.getSizeX());
            stmt.setInt(4, map.getSizeY());
            stmt.setInt(5, map.getHeroCapacity());
            int results = stmt.executeUpdate();
            if (results == 0) {
                return new InternalResponseObject<>(InternalErrorCode.INVALID_SQL, "Unable to store map metadata.");
            }
            int mapID;
            ResultSet genKeys = stmt.getGeneratedKeys();
            if (genKeys.next()) {
                mapID = (int) genKeys.getLong(1);
            } else {
                return new InternalResponseObject<>(InternalErrorCode.INVALID_SQL, "Unable to store map.");
            }

            stmt = conn.prepareStatement(INSERT_MAP_TILE_QUERY);
            for (int i = 0; i < map.getSizeX(); i++) {
                for (int j = 0; j < map.getSizeY(); j++) {
                    stmt.clearParameters();
                    stmt.setInt(1, mapID);
                    Optional<MapTile> tile = map.getTile(i, j);
                    if (tile.isPresent()) {
                        MapTile t = tile.get();
                        stmt.setInt(2, t.getTileType().getDatabaseID());
                        stmt.setInt(3, i);
                        stmt.setInt(4, j);
                        stmt.setBoolean(5, t.isHeroSpawn());
                        stmt.setBoolean(6, t.isArchitectSpawn());
                        stmt.setBoolean(7, t.isObjectiveSpawn());
                        stmt.setInt(8, t.getRotation());
                        results = stmt.executeUpdate();
                        if (results != 1) {
                            return new InternalResponseObject<>(InternalErrorCode.INVALID_SQL, "Unable to store map.");
                        }
                    } else {
                        return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR, "Unable to store map.");
                    }

                }
            }
            return new InternalResponseObject<>(mapID, "map_id");
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(InternalErrorCode.INVALID_SQL, "Unable to store map.");
        }
    }

    @Override
    public Map<String, TileType> getTileTypeMap() {
        if (tileTypeMap.size() == 0) {
            try {
                PreparedStatement tileTypeStmt = conn.prepareStatement(GET_TILE_TYPES);
                ResultSet rst = tileTypeStmt.executeQuery();
                while (rst.next()) {
                    int id = rst.getInt("id");
                    String type = rst.getString("tile_type");
                    boolean isObstruction = rst.getBoolean("is_obstruction");
                    boolean isVisionObstruction = rst.getBoolean("is_vision_obstruction");
                    TileType t = new TileType(id, type, isObstruction, isVisionObstruction);
                    tileTypeMap.put(id, t);
                    tileTypeStringMap.put(type, t);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tileTypeStringMap;
    }

    @Override
    public InternalResponseObject<List<MapMetadata>> getMapData() {
        try {
            PreparedStatement stmt = conn.prepareStatement(GET_ALL_MAPS_QUERY);
            ResultSet rslts = stmt.executeQuery();
            List<MapMetadata> metadata = new ArrayList<>();
            while (rslts.next()) {
                metadata.add(new MapMetadata(rslts.getInt("id"), rslts.getString("map_name"), rslts.getString
                        ("username"), rslts.getInt("width"), rslts.getInt("depth"), rslts.getInt("hero_capacity")));
            }
            return new InternalResponseObject<>(metadata, "maps");
        } catch (SQLException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(InternalErrorCode.INVALID_SQL, "Unable to retrieve map metadata.");
        }
    }

    @Override
    protected List<String> getTables() {
        return new ArrayList<String>() {
            {
                add(TILE_MAP_TABLE);
                add(TILES_TABLE);
                add(MAPS_TABLE);
            }
        };
    }
}
