package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.core.objects.maps.FromDatabaseMap;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.maps.MapTile;
import edu.cwru.eecs395_s16.networking.RequestData;
import edu.cwru.eecs395_s16.services.maps.MapRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 4/16/16.
 */
public class MapStoreRequest implements RequestData {

    private GameMap map;

    public MapStoreRequest() {
        map = null;
    }

    public MapStoreRequest(GameMap map) {
        this.map = map;
    }

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        String mapName;
        int sizeX;
        int sizeY;
        try {
            mapName = RequestData.getString(obj, GameMap.MAP_NAME_KEY);
            JSONObject sizeObj = obj.getJSONObject(GameMap.MAP_SIZE_KEY);
            sizeX = RequestData.getInt(sizeObj, GameMap.MAP_X_KEY);
            sizeY = RequestData.getInt(sizeObj, GameMap.MAP_Y_KEY);
        } catch (JSONException e) {
            throw new InvalidDataException(GameMap.MAP_SIZE_KEY);
        }
        List<MapTile> actTileList = new ArrayList<>();
        int[][] posMap = new int[sizeX][sizeY];
        try {
            Map<String, MapRepository.TileType> tileTypeMap = GameEngine.instance().services.mapRepository
                    .getTileTypeMap();
            JSONArray tileArr = obj.getJSONArray(GameMap.MAP_TILES_KEY);

            for (int i = 0; i < tileArr.length(); i++) {
                JSONObject tile = tileArr.getJSONObject(i);
                int tileX = RequestData.getInt(tile, MapTile.X_KEY);
                int tileY = RequestData.getInt(tile, MapTile.Y_KEY);
                if (posMap[tileX][tileY] != 0) {
                    throw new InvalidDataException(GameMap.MAP_TILES_KEY, "Tile at position " + tileX + "," + tileY +
                            "is already specified");
                } else {
                    posMap[tileX][tileY] = 1;
                }
                String terrainType = RequestData.getString(tile, MapTile.TERRAIN_KEY);
                if (!tileTypeMap.containsKey(terrainType)) {
                    throw new InvalidDataException(GameMap.MAP_TILES_KEY + "." + i + "." + MapTile.TERRAIN_KEY);
                }
                MapRepository.TileType actTerrainType = tileTypeMap.get(terrainType);
                int rotation = RequestData.getInt(tile, MapTile.ROTATION_KEY);
                boolean heroSpawn = tile.optBoolean(MapTile.HERO_SPAWN_KEY, false);
                boolean architectSpawn = tile.optBoolean(MapTile.ARCHITECT_SPAWN_KEY, false);
                boolean objectiveSpawn = tile.optBoolean(MapTile.OBJECTIVE_SPAWN_KEY, false);
                actTileList.add(new MapTile(tileX, tileY, actTerrainType, rotation, heroSpawn, architectSpawn,
                        objectiveSpawn));
            }
        } catch (JSONException e) {
            throw new InvalidDataException(GameMap.MAP_TILES_KEY, "Invalid JSON: " + GameMap.MAP_TILES_KEY + " needs " +
                    "to be an array");
        }
        if (sizeX * sizeY != actTileList.size()) {
            throw new InvalidDataException(GameMap.MAP_TILES_KEY, "Incorrect number of tiles specified. Expected: " +
                    sizeX * sizeY + ", Actual:" + actTileList.size());
        }
        int heroCapacity = (int) actTileList.stream().filter(MapTile::isHeroSpawn).count();
        FromDatabaseMap map = new FromDatabaseMap(-1, mapName, "", sizeX, sizeY, heroCapacity);
        actTileList.forEach(tile -> map.setTile(tile.getX(), tile.getY(), tile));
        this.map = map;
    }

    @Override
    public JSONObject convertToJSON() {
        return map.getJSONRepresentation();
    }

    public GameMap getMap() {
        return map;
    }
}
