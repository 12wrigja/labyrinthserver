package edu.cwru.eecs395_s16.services.maps;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.DatabaseObject;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.networking.Jsonable;
import edu.cwru.eecs395_s16.services.containers.Repository;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by james on 3/1/16.
 */
public interface MapRepository extends Repository {

    InternalResponseObject<GameMap> getMapByID(int id);

    InternalResponseObject<Integer> storeNewMapInDatabase(String mapName, Player creator, GameMap map);

    Map<String,TileType> getTileTypeMap();

    InternalResponseObject<List<MapMetadata>> getMapData();

    class TileType implements DatabaseObject {
        public final int id;
        public final String type;
        public final boolean isObstruction;

        public TileType(int id, String type, boolean isObstruction) {
            this.id = id;
            this.type = type;
            this.isObstruction = isObstruction;
        }

        @Override
        public int getDatabaseID() {
            return id;
        }
    }

    class MapMetadata implements Jsonable {
        public final int id;
        public final String name;
        public final String creator_id;
        public final int width;
        public final int depth;
        public final int heroCapacity;

        public MapMetadata(int id, String name, String creator_id, int width, int depth, int heroCapacity) {
            this.id = id;
            this.name = name;
            this.creator_id = creator_id;
            this.width = width;
            this.depth = depth;
            this.heroCapacity = heroCapacity;
        }

        @Override
        public JSONObject getJSONRepresentation() {
            JSONObject repr = new JSONObject();
            try {
                JSONObject sizeObj = new JSONObject();
                sizeObj.put(GameMap.MAP_X_KEY,width);
                sizeObj.put(GameMap.MAP_Y_KEY, depth);
                repr.put(GameMap.MAP_SIZE_KEY, sizeObj);
                repr.put(GameMap.MAP_NAME_KEY,name);
                repr.put(GameMap.MAP_CREATOR_ID_KEY, creator_id);
                repr.put(GameMap.MAP_HERO_CAPACITY_KEY, heroCapacity);
                repr.put(GameMap.MAP_ID_KEY,id);
            } catch (JSONException e){
                //Should not happen - non null keys
            }
            return repr;
        }
    }
}
