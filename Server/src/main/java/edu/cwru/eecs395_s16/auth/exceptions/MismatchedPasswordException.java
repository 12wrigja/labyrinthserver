package edu.cwru.eecs395_s16.auth.exceptions;

import edu.cwru.eecs395_s16.networking.responses.StatusCode;

/**
 * Created by james on 1/20/16.
 */
public class MismatchedPasswordException extends JsonableException {
    public MismatchedPasswordException() {
        super(StatusCode.UNPROCESSABLE_DATA, "Passwords do not match.");
    }
}
