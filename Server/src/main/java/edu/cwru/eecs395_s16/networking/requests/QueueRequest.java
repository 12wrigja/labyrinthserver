package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.networking.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/25/16.
 */
public class QueueRequest implements RequestData {

    private boolean queueWithPassBot = false;
    private int mapX = 10;
    private int mapY = 10;

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.queueWithPassBot = obj.optBoolean("queue_with_passbot",false);
        this.mapX = obj.optInt("map_x",10);
        this.mapY = obj.optInt("map_y",10);
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("queue_with_passbot", queueWithPassBot);
        } catch (JSONException e) {
            //Should not happen b/c keys are not null.
        }
        return repr;
    }

    public boolean shouldQueueWithPassBot() {
        return queueWithPassBot;
    }

    public int getMapX() {
        return mapX;
    }

    public int getMapY() {
        return mapY;
    }
}
