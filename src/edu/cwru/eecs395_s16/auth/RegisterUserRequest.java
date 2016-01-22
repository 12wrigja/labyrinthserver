package edu.cwru.eecs395_s16.auth;

/**
 * Created by james on 1/20/16.
 */
public class RegisterUserRequest {

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

}
