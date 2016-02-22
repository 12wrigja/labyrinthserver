package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.interfaces.Jsonable;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 1/21/16.
 */
public class JsonableException extends Exception implements Jsonable {

    private StatusCode errorCode;
    private String message;

    public JsonableException(StatusCode code, String message) {
        this.errorCode = code;
        this.message = message;
    }

    public JSONObject getJSONRepresentation() {
        JSONObject mp = new JSONObject();
        try {
            mp.put("status", this.errorCode.code);
            mp.put("message", this.message);
        }catch(JSONException e){
            //Not going to happen - both keys are not null;
        }
        return mp;
    }
}
