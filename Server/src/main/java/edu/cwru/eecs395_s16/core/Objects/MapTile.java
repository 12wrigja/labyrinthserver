package edu.cwru.eecs395_s16.core.objects;

import edu.cwru.eecs395_s16.interfaces.Jsonable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 2/12/16.
 */
public class MapTile extends Location implements Jsonable {

    private String tileType;
    private int rotation;

    public MapTile(int x, int y, String tileType, int rotation) {
        super(x, y);
        this.tileType = tileType;
        this.rotation = rotation;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject json = new JSONObject();
        try {
            json.put("x", getX());
            json.put("y",getY());
            json.put("terrain", tileType);
            json.put("rotation", rotation);
            json.put("is_obstacle",isObstructionTileType());
        } catch (JSONException e) {
            //Never will happen - all keys are not null
        }
        return json;
    }

    public boolean isObstructionTileType() {
        return this.tileType.equals("water") || this.tileType.equals("wall") || this.tileType.equals("empty");
    }
}
