package edu.cwru.eecs395_s16.networking;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by james on 2/10/16.
 */
public interface RequestData {

    void fillFromJSON(JSONObject obj) throws InvalidDataException;

    JSONObject convertToJSON();

    static String getString(JSONObject obj, String key) throws InvalidDataException {
        try {
            return obj.getString(key);
        } catch (JSONException e) {
            throw new InvalidDataException(key);
        }
    }

    static int getInt(JSONObject obj, String key) throws InvalidDataException {
        try {
            return obj.getInt(key);
        } catch (JSONException e) {
            throw new InvalidDataException(key);
        }
    }

    static UUID getUUID(JSONObject obj, String key) throws InvalidDataException {
        String uuidStr = getString(obj,key);
        try{
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e){
            throw new InvalidDataException(key);
        }
    }

    static boolean getBoolean(JSONObject obj, String key) throws InvalidDataException {
        try{
            return obj.getBoolean(key);
        } catch (JSONException e){
            throw new InvalidDataException(key);
        }
    }
}
