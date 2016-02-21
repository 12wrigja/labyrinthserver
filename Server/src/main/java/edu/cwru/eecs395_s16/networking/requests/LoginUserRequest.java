package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.interfaces.RequestData;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
}
