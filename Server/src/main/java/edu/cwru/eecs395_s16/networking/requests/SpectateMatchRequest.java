package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import org.json.JSONObject;

/**
 * Created by james on 2/21/16.
 */
public class SpectateMatchRequest implements RequestData {

    String matchID;

    public String getMatchID() {
        return matchID;
    }

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.matchID = RequestData.getString(obj,"match_id");
    }
}