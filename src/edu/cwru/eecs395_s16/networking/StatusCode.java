package edu.cwru.eecs395_s16.networking;

/**
 * Created by james on 1/20/16.
 */
public enum StatusCode {

    OK(200, null),
    UNPROCESSABLE_DATA(422, "Unable to process request. Invalid Entity."),
    UNAUTHENTICATED(401, "You must be authenticated to perform this action."),
    UNAUTHORIZED(403, "You are forbidden from performing this action.");

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public final String message;
    public final Object code;
}
