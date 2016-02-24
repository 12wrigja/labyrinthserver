package edu.cwru.eecs395_s16.interfaces;

import edu.cwru.eecs395_s16.core.JsonableException;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by james on 1/21/16.
 */
public class Response implements Jsonable {

    private JSONObject storage;

    public Response() {
        this.storage = new JSONObject();
        try {
            storage.put("status", StatusCode.OK.code);
            storage.put("message", StatusCode.OK.message);
        } catch (JSONException e) {
            //Not going to happen - all keys are not null;
        }
    }

    public Response(JsonableException e) {
        this();
        this.storage = e.getJSONRepresentation();
    }

    public Response(StatusCode code) {
        this();
        try {
            storage.put("status", code.code);
            storage.put("message", code.message);
        } catch (JSONException e) {
            //Not going to happen - both keys are not null;
        }
    }

    public void setKey(String key, Object value) {
        //We can't override the status key.
        if (key == null) {
            return;
        }
        if (!key.equals("status")) {
            try {
                storage.put(key, value);
            } catch (JSONException e) {
                //Not going to happen - key is null
            }
        }
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return this.storage;
    }
}
