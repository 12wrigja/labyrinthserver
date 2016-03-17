package edu.cwru.eecs395_s16.networking.requests.gameactions;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by james on 3/16/16.
 */
public class BasicAttackActionData {

    private UUID attacker;
    private UUID target;

    public BasicAttackActionData(UUID attacker, UUID target) {
        this.attacker = attacker;
        this.target = target;
    }

    public static InternalResponseObject<BasicAttackActionData> fillFromJSON(JSONObject obj) {
        UUID attackerID;
        try {
             attackerID = RequestData.getUUID(obj,"attacker_id");
        } catch (InvalidDataException e) {
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR,"The attacker id is invalid.");
        }
        UUID targetID;
        try {
            targetID = RequestData.getUUID(obj,"target_id");
        } catch (InvalidDataException e) {
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR, "The target id is invalid.");
        }

        return new InternalResponseObject<>(new BasicAttackActionData(attackerID,targetID));
    }

    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("attacker_id",attacker.toString());
            repr.put("target_id",target.toString());
        } catch (JSONException e){
            //This should not happen - the keys are nonnull
        }
        return repr;
    }

    public UUID getAttacker() {
        return attacker;
    }

    public UUID getTarget() {
        return target;
    }
}
