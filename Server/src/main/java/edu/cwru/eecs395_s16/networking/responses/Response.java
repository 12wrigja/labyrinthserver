package edu.cwru.eecs395_s16.networking.responses;

import edu.cwru.eecs395_s16.auth.exceptions.JsonableException;
import edu.cwru.eecs395_s16.core.Interfaces.Jsonable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 1/21/16.
 */
public class Response implements Jsonable {

    private Map<String, Object> storage;

    public Response() {
        this.storage = new HashMap<>();
        storage.put("status", StatusCode.OK.code);
        storage.put("message",StatusCode.OK.message);
    }

    public Response(JsonableException e) {
        this();
        this.storage = e.getJsonableRepresentation();
    }

    public Response(StatusCode code) {
        this();
        storage.put("status", code.code);
        storage.put("message",code.message);
    }

    public void setKey(String key, Object value){
        if(!key.equals("status")){
            storage.put(key,value);
        }
    }

    @Override
    public Map<String, Object> getJsonableRepresentation() {
        return this.storage;
    }
}
