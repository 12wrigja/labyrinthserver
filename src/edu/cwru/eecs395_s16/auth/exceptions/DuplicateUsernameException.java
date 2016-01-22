package edu.cwru.eecs395_s16.auth.exceptions;

/**
 * Created by james on 1/20/16.
 */
public class DuplicateUsernameException extends Exception {
    private final String username;

    public DuplicateUsernameException(String username) {
        super();
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
