package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;
import edu.cwru.eecs395_s16.interfaces.RequestData;
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

    @Override
    public void fillFromJSON(JSONObject obj) throws InvalidDataException {
        this.username = RequestData.getString(obj,"username");
        this.password = RequestData.getString(obj,"password");
        this.password_confirm = RequestData.getString(obj,"password_confirm");
    }
}
