package edu.cwru.eecs395_s16.networking.requests;

import edu.cwru.eecs395_s16.auth.exceptions.InvalidDataException;

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

    public void setPassword_confirm(String password_confirm) {
        this.password_confirm = password_confirm;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void validate() throws InvalidDataException {
        List<String> invalidParams = new ArrayList<>();
        if(getPassword() == null){
            invalidParams.add("password");
        }
        if(getPasswordConfirm() == null){
            invalidParams.add("password_confirm");
        }
        if(getUsername() == null){
            invalidParams.add("username");
        }
        if(invalidParams.size() > 0){
            throw new InvalidDataException(invalidParams);
        }
    }
}
