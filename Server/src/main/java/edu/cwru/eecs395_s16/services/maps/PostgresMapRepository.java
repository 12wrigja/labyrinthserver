package edu.cwru.eecs395_s16.services.maps;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.maps.MapTile;
import edu.cwru.eecs395_s16.core.objects.maps.FromDatabaseMap;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.services.containers.DBRepository;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 3/1/16.
 */
public class PostgresMapRepository extends DBRepository implements MapRepository {

    public static final String MAPS_TABLE = "maps";
    private static final String GET_MAP_QUERY = "select maps.*, players.username as username from " + MAPS_TABLE + " inner join players on maps.creator_id = players.id where "+MAPS_TABLE+".id = ?";
    public static final String TILE_MAP_TABLE = "tile_map";
    private static final String GET_MAP_TILES_QUERY = "select * from " + TILE_MAP_TABLE + " where map_id = ?";

    public static final String TILES_TABLE = "tiles";
    private static final String GET_TILE_TYPES = "select * from " + TILES_TABLE;

    final Map<Integer, TileType> tileTypeMap;
    final Map<String, TileType> tileTypeStringMap;

    public PostgresMapRepository(Connection conn) {
        super(conn);
        tileTypeMap = new HashMap<>();
        tileTypeStringMap = new HashMap<>();
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
                    MapTile tile = new MapTile(x, y, tileType, rotation, isHeroSpawnTile, isArchitectSpawnTile, isObjectiveSpawnTile);
                    mp.setTile(x, y, tile);
                }
                if (!(count == (mp.getSizeX() * mp.getSizeY()))) {
                    //Throw issue here - map is not fully defined.
                    return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.INVALID_MAP_DEFINITION);
                }
                return new InternalResponseObject<>(mp, "map");
            } else {
                //Throw error - can't find that map.
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.UNKNOWN_MAP_IDENTIFIER);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.INVALID_SQL);
        }
    }

    @Override
    public void storeNewMapInDatabase(String mapName, Player creator, GameMap map) {
        //TODO implement map storage
    }

    @Override
    public Map<String, TileType> getTileTypeMap() {
        if(tileTypeMap.size() == 0) {
            try {
                PreparedStatement tileTypeStmt = conn.prepareStatement(GET_TILE_TYPES);
                ResultSet rst = tileTypeStmt.executeQuery();
                while (rst.next()) {
                    int id = rst.getInt("id");
                    String type = rst.getString("tile_type");
                    boolean isObstruction = rst.getBoolean("is_obstruction");
                    TileType t = new TileType(id, type, isObstruction);
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
    protected List<String> getTables() {
        return new ArrayList<String>(){
            {
                add(TILE_MAP_TABLE);
                add(TILES_TABLE);
                add(MAPS_TABLE);
            }
        };
    }
}
