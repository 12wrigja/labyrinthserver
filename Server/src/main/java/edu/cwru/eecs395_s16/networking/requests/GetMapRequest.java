package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.networking.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 4/17/16.
 */
public class GetMapRequest implements RequestData {

    int requestedMapID;

    public GetMapRequest() {
        requestedMapID = -1;
    }

    public GetMapRequest(int requestedMapID) {
        this.requestedMapID = requestedMapID;
    }

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        requestedMapID = obj.optInt("map_id", -1);
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("map_id", requestedMapID);
        } catch (JSONException e) {
            //Should not happen - non-null keys
        }
        return repr;
    }

    public int getRequestedMapID() {
        return requestedMapID;
    }
}
