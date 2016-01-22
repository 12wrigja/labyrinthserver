package edu.cwru.eecs395_s16.networking;

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
    }

    @Override
    public Map<String, Object> getJsonableRepresentation() {
        return this.storage;
    }
}
