package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 1/20/16.
 */
public class RegisterUserRequest implements RequestData {

    private String username;
    private String password;
    private String password_confirm;

    public String getPasswordConfirm() {
        return password_confirm;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public RegisterUserRequest(String username, String password, String password_confirm) {
        this.username = username;
        this.password = password;
        this.password_confirm = password_confirm;
    }
    
    public RegisterUserRequest(){}

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.username = RequestData.getString(obj,"username");
        if(!this.username.matches("[a-zA-Z0-9]+")){
            throw new InvalidDataException("username");
        }
        this.password = RequestData.getString(obj,"password");
        this.password_confirm = RequestData.getString(obj,"password_confirm");
    }

    @Override
    public JSONObject convertToJSON() {
        JSONObject repr = new JSONObject();
        try {
            repr.put("username",username);
            repr.put("password",password);
            repr.put("password_confirm",password_confirm);
        } catch (JSONException e) {
            //Should not happen b/c keys are not null.
        }
        return repr;
    }
}
