package edu.cwru.eecs395_s16.interfaces;

import edu.cwru.eecs395_s16.core.JsonableException;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;

import java.util.HashMap;
import java.util.List;
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
        //We can't override the status key.
        if(!key.equals("status")){
            storage.put(key, value);
        }
    }

    public void setDeepKey(Object value, String... keypath){
        //Split up the key and store the value
        Map<String,Object> currentMap = storage;
        for(int i = 0; i<keypath.length; i++){
            //Get whatever is there in the current object or array
            String part = keypath[i];
            Object currentObj = currentMap.get(part);
            //Check to see if there is already something there
            if(currentObj != null){
                if( i == keypath.length - 1){
                    //There is something in the space we designated.
                    break;
                } else {
                    //Theres already something there...
                    if (currentObj instanceof Map<?, ?>) {
                        currentMap = (Map<String, Object>) currentObj;
                    } else if (currentObj instanceof List<?>) {
                        //TODO add in support for lists
                    } else {
                        break;
                    }
                }
            } else {
                if(i == keypath.length - 1){
                    currentMap.put(part,value);
                } else {
                    //This is not the last part of the
                    Map<String, Object> newMap = new HashMap<>();
                    currentMap.put(part,newMap);
                    currentMap = newMap;
                }
            }
        }
    }

    @Override
    public Map<String, Object> getJsonableRepresentation() {
        return this.storage;
    }
}
