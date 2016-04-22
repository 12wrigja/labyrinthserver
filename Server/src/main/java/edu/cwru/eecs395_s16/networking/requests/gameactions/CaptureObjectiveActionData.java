package edu.cwru.eecs395_s16.networking.requests.gameactions;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by james on 4/8/16.
 */
public class CaptureObjectiveActionData {

    private UUID objectiveID;
    private UUID characterID;

    public CaptureObjectiveActionData(UUID characterID, UUID objectiveID) {
        this.characterID = characterID;
        this.objectiveID = objectiveID;
    }

    public static InternalResponseObject<CaptureObjectiveActionData> fillFromJSON(JSONObject obj) {

        UUID characterID;
        try {
            String characterIDs = obj.getString("character_id");
            try {
                characterID = UUID.fromString(characterIDs);
            } catch (IllegalArgumentException e) {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.DATA_PARSE_ERROR, "The character_id is invalid.");
            }
        } catch (JSONException e) {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.DATA_PARSE_ERROR, "The character_id is invalid.");
        }

        UUID objectiveID;
        try {
            String objectiveIDs = obj.getString("objective_id");
            try {
                objectiveID = UUID.fromString(objectiveIDs);
            } catch (IllegalArgumentException e) {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.DATA_PARSE_ERROR, "The objective_id is invalid.");
            }
        } catch (JSONException e) {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.DATA_PARSE_ERROR, "The objective_id is invalid.");
        }

        return new InternalResponseObject<>(new CaptureObjectiveActionData(characterID, objectiveID));
    }

    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("character_id", characterID.toString());
            repr.put("objective_id", objectiveID.toString());
            repr.put("type", "capture_objective");
        } catch (JSONException e) {
            //Should not happen b/c keys are not null.
        }
        return repr;
    }

    public UUID getObjectiveID() {
        return objectiveID;
    }

    public UUID getCharacterID() {
        return characterID;
    }
}
