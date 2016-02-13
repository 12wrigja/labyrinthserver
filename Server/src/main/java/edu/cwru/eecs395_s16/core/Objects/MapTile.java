package edu.cwru.eecs395_s16.core.Objects;

import edu.cwru.eecs395_s16.core.Interfaces.Jsonable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 2/12/16.
 */
public class MapTile extends BasicLocation implements Jsonable {

    private String tileType;
    private int rotation;

    public MapTile(int x, int y, String tileType, int rotation) {
        super(x, y);
        this.tileType = tileType;
        this.rotation = rotation;
    }

    @Override
    public Map<String, Object> getJsonableRepresentation() {
        Map<String,Object> json = new HashMap<>();
        int[] posArray = new int[2];
        posArray[0] = getX();
        posArray[1] = getY();
        json.put("position",posArray);
        json.put("terrain",tileType);
        json.put("rotation",rotation);
        return json;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public String getTileType() {
        return tileType;
    }

    public void setTileType(String tileType) {
        this.tileType = tileType;
    }
}