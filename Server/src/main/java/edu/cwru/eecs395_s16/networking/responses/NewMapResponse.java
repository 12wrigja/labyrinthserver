package edu.cwru.eecs395_s16.networking.responses;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.networking.Response;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/28/16.
 */
public class NewMapResponse extends Response {

    public final GameMap map;

    public NewMapResponse(GameMap map) {
        this.map = map;
    }

    @Override
    public JSONObject getJSONRepresentation() {
        JSONObject repr = super.getJSONRepresentation();
        try {
            repr.put("map", map);
        } catch (JSONException e) {
            //Should not happen due to not null keys. XKXdwN5hppmSWRfb
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
        }
        return repr;
    }
}
