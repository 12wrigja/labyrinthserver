package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.networking.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 2/12/16.
 */
public class NewMapRequest implements RequestData {

    private int x;
    private int y;

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public NewMapRequest(){
        this.x = -1;
        this.y = -1;
    };

    public NewMapRequest(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.x = RequestData.getInt(obj,"x");
        this.y = RequestData.getInt(obj,"y");
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("x",x);
            repr.put("y",y);
        } catch (JSONException e) {
            //Should not happen b/c keys are not null.
        }
        return repr;
    }
}
