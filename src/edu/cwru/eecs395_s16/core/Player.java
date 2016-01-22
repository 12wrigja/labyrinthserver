package edu.cwru.eecs395_s16.core;

/**
 * Created by james on 1/19/16.
 */
public class Player {

    private String username;

    private String password;

    public Player(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public String getUsername() {
        return username;
    }


}
