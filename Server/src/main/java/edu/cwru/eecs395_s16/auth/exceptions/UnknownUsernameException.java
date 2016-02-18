package edu.cwru.eecs395_s16.auth.exceptions;

import edu.cwru.eecs395_s16.core.JsonableException;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;

/**
 * Created by james on 1/20/16.
 */
public class UnknownUsernameException extends JsonableException {
    public UnknownUsernameException(String username) {
        super(StatusCode.UNPROCESSABLE_DATA, "The username '" + username + "' is invalid.");
    }
}
