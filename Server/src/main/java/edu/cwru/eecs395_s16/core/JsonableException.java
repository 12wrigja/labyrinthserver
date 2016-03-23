package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.networking.Jsonable;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 1/21/16.
 */
public class JsonableException extends Exception implements Jsonable {

    private WebStatusCode errorCode;
    private String message;

    public JsonableException(WebStatusCode code, String message) {
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

    public WebStatusCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
