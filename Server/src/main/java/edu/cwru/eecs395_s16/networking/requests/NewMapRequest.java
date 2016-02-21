package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.x = RequestData.getInt(obj,"x");
        this.y = RequestData.getInt(obj,"y");
    }
}
