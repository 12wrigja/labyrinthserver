package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/21/16.
 */
public class GameActionBaseRequest implements RequestData {

    public JSONObject getOriginalData() {
        return originalData;
    }

    public ACTION_TYPE getType() {
        return type;
    }

    public enum ACTION_TYPE {
        MOVE_ACTION,
        BASIC_ATTACK_ACTION,
        ABILITY_ACTION,
        PASS_ACTION;
    }

    private static final String ENUM_APPEND = "_ACTION";

    private JSONObject originalData;

    private ACTION_TYPE type;

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.originalData = obj;
        try {
            type = ACTION_TYPE.valueOf(obj.getString("type").toUpperCase()+ENUM_APPEND);
        } catch (JSONException e) {
            type = ACTION_TYPE.PASS_ACTION;
        }
    }
}
