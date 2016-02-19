package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.networking.responses.StatusCode;

/**
 * Created by james on 2/19/16.
 */
public class InvalidGameStateException extends JsonableException {
    public InvalidGameStateException(String message) {
        super(StatusCode.UNPROCESSABLE_DATA, message);
    }
}
