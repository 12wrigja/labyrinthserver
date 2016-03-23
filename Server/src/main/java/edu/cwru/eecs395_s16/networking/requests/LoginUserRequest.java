package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.networking.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by james on 1/19/16.
 */
public class LoginUserRequest implements RequestData {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.username = RequestData.getString(obj,"username");
        this.password = RequestData.getString(obj,"password");
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("username",username);
            repr.put("password",password);
        } catch (JSONException e) {
            //Should never happen b/c keys are not null
        }
        return repr;
    }
}
