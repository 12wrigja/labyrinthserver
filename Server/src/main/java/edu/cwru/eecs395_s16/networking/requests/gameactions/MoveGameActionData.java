package edu.cwru.eecs395_s16.networking.requests.gameactions;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by james on 2/21/16.
 */
public class MoveGameActionData {

    private UUID characterID;
    private List<Location> path;

    public MoveGameActionData(UUID characterID, List<Location> path) {
        this.characterID = characterID;
        this.path = path;
    }

    public static InternalResponseObject<MoveGameActionData> fillFromJSON(JSONObject obj) {

        UUID characterID;
        try {
            String characterIDs = obj.getString("character_id");
            try {
                characterID = UUID.fromString(characterIDs);
            } catch (IllegalArgumentException e) {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode
                        .DATA_PARSE_ERROR, "The character_id is invalid.");
            }
        } catch (JSONException e) {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.DATA_PARSE_ERROR,
                    "The character_id is invalid.");
        }

        List<Location> path = new ArrayList<>();
        try {
            JSONArray pathArr = obj.getJSONArray("path");
            for (int i = 0; i < pathArr.length(); i++) {
                JSONObject location = pathArr.getJSONObject(i);
                int x = location.getInt("x");
                int y = location.getInt("y");
                Location l = new Location(x, y);
                path.add(l);
            }
        } catch (JSONException e) {
            if (GameEngine.instance().IS_DEBUG_MODE) {
                e.printStackTrace();
            }
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.DATA_PARSE_ERROR,
                    "The path is invalid.");
        }

        return new InternalResponseObject<>(new MoveGameActionData(characterID, path));
    }

    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("character_id", characterID);
            repr.put("path", path);
            repr.put("type", "move");
        } catch (JSONException e) {
            //Should not happen b/c keys are not null.
        }
        return repr;
    }

    public UUID getCharacterID() {
        return characterID;
    }

    public List<Location> getPath() {
        return path;
    }

}
