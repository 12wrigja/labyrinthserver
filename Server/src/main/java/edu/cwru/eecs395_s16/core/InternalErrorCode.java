package edu.cwru.eecs395_s16.core;

/**
 * Created by james on 2/28/16.
 */
public enum InternalErrorCode {
    UNKNOWN("The internal error is unknown."),
    DUPLICATE_USERNAME("The given username already exists."),
    UNKNOWN_USERNAME("The given username could not be found."),
    INVALID_PASSWORD("The given password is invalid."),
    MISMATCHED_PASSWORD("The given passwords do not match."),
    RESTRICTED_USERNAME("The given username is restricted."),
    INVALID_USERNAME("The given username is not valid.");



















    public final String message;
    InternalErrorCode(String message){
        this.message = message;
    }
}
