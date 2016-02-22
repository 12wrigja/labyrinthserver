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

    public void setDeepKey(Object value, String... keypath) {
        //Split up the key and store the value
        JSONObject currentMap = storage;
        for (int i = 0; i < keypath.length; i++) {
            //Get whatever is there in the current object or array
            String part = keypath[i];
            if (!part.isEmpty()) {
                Object currentObj = null;
                try {
                    currentObj = currentMap.get(part);
                } catch (JSONException e) {
                    //Do nothing - this will trigger the overwrite case
                }
                //Check to see if there is already something there
                if (currentObj != null) {
                    if (i == keypath.length - 1) {
                        //There is something in the space we designated.
                        break;
                    } else {
                        //Theres already something there...
                        if (currentObj instanceof JSONObject) {
                            currentMap = (JSONObject) currentObj;
                        } else if (currentObj instanceof List<?>) {
                            //TODO add in support for lists
                        } else {
                            break;
                        }
                    }
                } else {
                    if (i == keypath.length - 1) {
                        try {
                            currentMap.put(part, value);
                        } catch (JSONException e) {
                            //Never will occur - if the key section was empty we would have exited by now.
                        }
                    } else {
                        //This is not the last part of the
                        JSONObject newMap = new JSONObject();
                        try {
                            currentMap.put(part, newMap);
                        } catch (JSONException e) {
                            //Never will occur - if the key section was empty we would have exited by now.
                        }
                        currentMap = newMap;
                    }
                }
            } else {
                //There is an empty part in the key - ignore this command
                return;
            }
        }
    }

    @Override
    public JSONObject getJSONRepresentation() {
        return this.storage;
    }
}
