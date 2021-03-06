package edu.cwru.eecs395_s16.networking.requests.gameactions;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.objects.Location;
import edu.cwru.eecs395_s16.networking.RequestData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by james on 3/16/16.
 */
public class BasicAttackActionData {

    private UUID attacker;
    private List<Location> targets;

    public BasicAttackActionData(UUID attacker, List<Location> targets) {
        this.attacker = attacker;
        this.targets = targets;
    }

    public static InternalResponseObject<BasicAttackActionData> fillFromJSON(JSONObject obj) {
        UUID attackerID;
        List<Location> targets = new ArrayList<>();
        try {
            attackerID = RequestData.getUUID(obj, "attacker_id");
        } catch (InvalidDataException e) {
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR, "The attacker id is invalid.");
        }

        try {
            JSONArray arr = obj.getJSONArray("targets");
            try {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject target = arr.getJSONObject(i);
                    int x = target.getInt(Location.X_KEY);
                    int y = target.getInt(Location.Y_KEY);
                    targets.add(new Location(x, y));
                }
            } catch (IllegalArgumentException e) {
                return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR, "One of the target " +
                        "identifiers is invalid.");
            }
        } catch (JSONException e) {
            return new InternalResponseObject<>(InternalErrorCode.DATA_PARSE_ERROR, "One of the targets is invalid.");
        }

        return new InternalResponseObject<>(new BasicAttackActionData(attackerID, targets));
    }

    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("attacker_id", attacker.toString());
            repr.put("targets", targets);
            repr.put("type", "basic_attack");
        } catch (JSONException e) {
            //This should not happen - the keys are nonnull
        }
        return repr;
    }

    public UUID getAttacker() {
        return attacker;
    }

    public List<Location> getTargets() {
        return targets;
    }
}
