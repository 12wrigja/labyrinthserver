package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 1/19/16.
 */
public class LoginUserRequest implements RequestData {

    private String username;
    private String password;

    public LoginUserRequest() {
    }

    public LoginUserRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void validate() throws InvalidDataException {
        List<String> params = new ArrayList<>();
        if(username == null){
            params.add("username");
        }
        if(password == null){
            params.add("password");
        }
        if(params.size() > 0){
            throw new InvalidDataException(params);
        }
    }
}
