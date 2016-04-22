package edu.cwru.eecs395_s16.networking.requests.queueing;

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
    private int mapID = -1;
    private String gameObjectiveShortCode = "dm";

    public QueueRequest() {

    }

    public QueueRequest(boolean queueWithPassBot, int mapX, int mapY, int mapID, String mapCode) {
        this.queueWithPassBot = queueWithPassBot;
        this.mapX = mapX;
        this.mapY = mapY;
        this.mapID = mapID;
        this.gameObjectiveShortCode = mapCode;
    }

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.queueWithPassBot = obj.optBoolean("queue_with_passbot", false);
        this.mapX = obj.optInt("map_x", 10);
        this.mapY = obj.optInt("map_y", 10);
        this.mapID = obj.optInt("map_id", -1);
        this.gameObjectiveShortCode = obj.optString("game_mode", "dm");
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("queue_with_passbot", queueWithPassBot);
            repr.put("map_x", mapX);
            repr.put("map_y", mapY);
            repr.put("map_id", mapID);
            repr.put("game_mode", gameObjectiveShortCode);
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

    public int getMapID() {
        return mapID;
    }

    public String getGameObjectiveShortCode() {
        return gameObjectiveShortCode;
    }
}
