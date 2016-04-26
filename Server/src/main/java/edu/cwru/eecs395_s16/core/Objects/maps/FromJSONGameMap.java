package edu.cwru.eecs395_s16.core.objects.maps;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.services.maps.MapRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

/**
 * Created by james on 2/24/16.
 */
public class FromJSONGameMap implements GameMap {

    private int x;
    private int y;
    private MapTile[][] tiles;
    private String creator = "";
    private String mapName = "";
    private int heroCapacity = 0;

    public FromJSONGameMap(JSONObject obj) throws JSONException {
        JSONObject size = obj.getJSONObject(GameMap.MAP_SIZE_KEY);
        this.x = size.getInt(GameMap.MAP_X_KEY);
        this.y = size.getInt(GameMap.MAP_Y_KEY);
        this.tiles = new MapTile[x][y];
        this.creator = obj.optString(GameMap.MAP_CREATOR_ID_KEY, "");
        this.mapName = obj.optString(GameMap.MAP_NAME_KEY, "");
        this.heroCapacity = obj.optInt(GameMap.MAP_HERO_CAPACITY_KEY, 0);
        JSONArray tiles = obj.getJSONArray(GameMap.MAP_TILES_KEY);
        for (int i = 0; i < tiles.length(); i++) {
            JSONObject tile = tiles.getJSONObject(i);
            String terrain = tile.getString(MapTile.TERRAIN_KEY);
            MapRepository.TileType tileType = GameEngine.instance().services.mapRepository.getTileTypeMap().get
                    (terrain);
            int tileX = tile.getInt(MapTile.X_KEY);
            int tileY = tile.getInt(MapTile.Y_KEY);
            int rotation = tile.getInt(MapTile.ROTATION_KEY);
            MapTile t = new MapTile(tileX, tileY, tileType, rotation, false, false, false);
            this.tiles[tileX][tileY] = t;
        }
    }

    @Override
    public Optional<MapTile> getTile(int x, int y) {
        if (x >= 0 && x < this.x && y >= 0 && y < this.y) {
            return Optional.of(tiles[x][y]);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int getSizeX() {
        return x;
    }

    @Override
    public int getSizeY() {
        return y;
    }

    @Override
    public String getCreatorUsername() {
        return creator;
    }

    @Override
    public String getName() {
        return mapName;
    }

    @Override
    public int getHeroCapacity() {
        return heroCapacity;
    }

    @Override
    public List<Location> getHeroSpawnLocations() {
        return null;
    }

    @Override
    public List<Location> getArchitectCreatureSpawnLocations() {
        return null;
    }

    @Override
    public List<Location> getObjectiveSpawnLocations() {
        return null;
    }

    @Override
    public int getDatabaseID() {
        return -1;
    }
}
