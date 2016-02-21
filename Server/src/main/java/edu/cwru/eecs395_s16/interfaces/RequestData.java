package edu.cwru.eecs395_s16.interfaces;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/10/16.
 */
public interface RequestData {

    void fillFromJSON(JSONObject obj) throws InvalidDataException;

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
}
