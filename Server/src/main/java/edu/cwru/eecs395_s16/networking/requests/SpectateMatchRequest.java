package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.networking.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/21/16.
 */
public class SpectateMatchRequest implements RequestData {

    String matchID;
    String playerID;

    public SpectateMatchRequest() {
        this.matchID = null;
    }

    public SpectateMatchRequest(String matchID) {
        this.matchID = matchID;
    }

    public String getMatchID() {
        return matchID;
    }

    public String getPlayerID() {
        return playerID;
    }

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.matchID = obj.optString("match_id",null);
        this.playerID = obj.optString("player",null);
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("match_id", matchID);
            repr.put("player",playerID);
        } catch (JSONException e) {
            //Should not happen b/c keys are not null.
        }
        return repr;
    }
}
