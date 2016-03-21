package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;

import java.util.Map;

/**
 * Created by james on 3/1/16.
 */
public interface MapRepository extends Repository {

    InternalResponseObject<GameMap> getMapByID(int id);

    void storeNewMapInDatabase(String mapName, Player creator, GameMap map);

    Map<String,TileType> getTileTypeMap();

    class TileType {
        public final int id;
        public final String type;
        public final boolean isObstruction;

        public TileType(int id, String type, boolean isObstruction) {
            this.id = id;
            this.type = type;
            this.isObstruction = isObstruction;
        }
    }
}
