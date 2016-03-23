package edu.cwru.eecs395_s16.networking.requests.gameactions;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.networking.RequestData;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by james on 4/15/16.
 */
public class PassGameActionData {

    private UUID character_id;

    public PassGameActionData(UUID character_id) {
        this.character_id = character_id;
    }

    public static InternalResponseObject<PassGameActionData> fillFromJSON(JSONObject obj) {
        UUID characterUUID;
        try {
            characterUUID = RequestData.getUUID(obj,"character_id");
        } catch (InvalidDataException e) {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA,InternalErrorCode.DATA_PARSE_ERROR,"The character_id is invalid.");
        }
        return new InternalResponseObject<>(new PassGameActionData(characterUUID));
    }

    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("character_id", character_id.toString());
            repr.put("type","pass");
        } catch (JSONException e) {
            //Should not happen b/c keys are not null.
        }
        return repr;
    }

    public UUID getCharacterID() {
        return character_id;
    }

    public void setCharacter(UUID character_id) {
        this.character_id = character_id;
    }

}
