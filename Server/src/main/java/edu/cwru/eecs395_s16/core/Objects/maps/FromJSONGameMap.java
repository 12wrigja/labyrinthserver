package edu.cwru.eecs395_s16.core.objects.maps;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.MapTile;
import edu.cwru.eecs395_s16.interfaces.objects.GameMap;
import edu.cwru.eecs395_s16.interfaces.repositories.MapRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Created by james on 2/24/16.
 */
public class FromJSONGameMap implements GameMap {

    private int x;
    private int y;
    private MapTile[][] tiles;
    private String creator;
    private String mapName;
    private int heroCapacity;

    public FromJSONGameMap (JSONObject obj) throws JSONException {
        JSONObject size = obj.getJSONObject("size");
        this.x = size.getInt("x");
        this.y = size.getInt("y");
        this.tiles = new MapTile[x][y];
        JSONArray tiles = obj.getJSONArray("tiles");
        for(int i=0; i<tiles.length(); i++){
            JSONObject tile = tiles.getJSONObject(i);
            String terrain = tile.getString("terrain");
            MapRepository.TileType tileType = GameEngine.instance().getMapRepository().getTileTypeMap().get(terrain);
            int tileX = tile.getInt("x");
            int tileY = tile.getInt("y");
            int rotation = tile.getInt("rotation");
            MapTile t = new MapTile(tileX,tileY,tileType,rotation, false);
            this.tiles[tileX][tileY] = t;
        }
    }

    @Override
    public Optional<MapTile> getTile(int x, int y) {
        if(x >= 0 && x < this.x && y >= 0 && y < this.y){
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
}
