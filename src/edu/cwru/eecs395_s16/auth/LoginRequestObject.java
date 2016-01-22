package edu.cwru.eecs395_s16.auth;

/**
 * Created by james on 1/19/16.
 */
public class LoginRequestObject {

    private String username;
    private String password;

    public LoginRequestObject() {
    }

    public LoginRequestObject(String username, String password) {
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
}
