package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import org.json.JSONObject;

/**
 * Created by james on 2/10/16.
 */
public class NoInputRequest implements RequestData {

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {

    }

    @Override
    public JSONObject convertToJSON() {
        return new JSONObject();
    }
}
