package edu.cwru.eecs395_s16.services.MapRepository;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.MapTile;
import edu.cwru.eecs395_s16.core.objects.maps.FromDatabaseMap;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.repositories.MapRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 3/1/16.
 */
public class PostgresMapRepository implements MapRepository {

    private static final String GET_MAP_QUERY = "select * from maps inner join players on maps.creator_id = players.id where id = ?";
    private static final String Get_TILES_QUERY = "select * from tile_map where map_id = ?";

    private static final String GET_TILE_TYPES = "select * from tiles";

    final Connection conn;
    final Map<Integer,TileType> tileTypeMap;
    final Map<String,TileType> tileTypeStringMap;

    public PostgresMapRepository(Connection conn) {
        this.conn = conn;
        tileTypeMap = new HashMap<>();
        tileTypeStringMap = new HashMap<>();
        try {
            PreparedStatement tileTypeStmt = conn.prepareStatement(GET_TILE_TYPES);
            ResultSet rst = tileTypeStmt.executeQuery();
            while(rst.next()){
                int id = rst.getInt("id");
                String type = rst.getString("tile_type");
                boolean isObstruction = rst.getBoolean("is_obstruction");
                TileType t = new TileType(id,type,isObstruction);
                tileTypeMap.put(id,t);
                tileTypeStringMap.put(type,t);
            }
        } catch (SQLException e) {
            if(GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public GameMap getMapByID(int id) {
        try {
            PreparedStatement mapBase = conn.prepareStatement(GET_MAP_QUERY);
            mapBase.setInt(0,id);
            ResultSet r = mapBase.executeQuery();
            if(r.next()){
                int dbID = r.getInt("id");
                String name = r.getString("map_name");
                String creatorID = r.getString("username");
                int width = r.getInt("width");
                int height = r.getInt("depth");
                int heroCapacity = r.getInt("hero_capacity");
                FromDatabaseMap mp = new FromDatabaseMap(id,name,creatorID,width,height,heroCapacity);

                //Get tiles for map
                PreparedStatement tileStmt = conn.prepareStatement(GET_TILE_TYPES);
                tileStmt.setInt(0,id);
                r = tileStmt.executeQuery();
                int count = 0;
                while(r.next()){
                    count ++;
                    int x = r.getInt("x");
                    int y = r.getInt("y");
                    boolean isHeroSpawnTile = r.getBoolean("is_hero_spawn");
                    int rotation = r.getInt("rotation");
                    int tileID = r.getInt("tile_id");
                    TileType tileType = tileTypeMap.get(tileID);
                    MapTile tile = new MapTile(x,y,tileType,rotation, isHeroSpawnTile);
                    mp.setTile(x,y,tile);
                }
                if(!(count == (mp.getSizeX()*mp.getSizeY()))){
                    //Throw issue here - map is not fully defined.
                }
                return mp;
            } else {
                //Throw error - can't find that map.
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void storeNewMapInDatabase(String mapName, Player creator, GameMap map) {
        //TODO implement map storage
    }

    @Override
    public Map<String, TileType> getTileTypeMap() {
        return tileTypeStringMap;
    }
}
