package edu.cwru.eecs395_s16.auth.exceptions;

import edu.cwru.eecs395_s16.core.Interfaces.Jsonable;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;

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

    public Map<String, Object> getJsonableRepresentation() {
        Map<String, Object> mp = new HashMap<>();
        mp.put("status", this.errorCode.code);
        mp.put("message", this.message);
        return mp;
    }
}
