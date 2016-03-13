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
    INVALID_USERNAME("The given username is not valid."),
    UNKNOWN_SESSION_IDENTIFIER("The given session identifier could not be resolved."),
    UNKNOWN_MAP_IDENTIFIER("The given map identifier could not be resolved."),
    INVALID_MAP_DEFINITION("The map with the given identifier is not properly defined."),
    INVALID_SQL("Some SQL was invalid."),
    MISSING_PLAYER("Unable to retrieve a player from given information."),
    PLAYER_BUSY("A player is currently busy."),
    MATCH_RETRIEVAL_ERROR("Unable to retrieve a match from storage."),
    ALREADY_IN_QUEUE("You are already in a queue."),
    NOT_IN_QUEUE("You are not in a queue."),
    NOT_IN_MATCH("You are not in a match."),
    INVALID_GAME_ACTION("The game action is not valid."),
    NOT_YOUR_TURN("It is not currently your turn."),
    DATA_PARSE_ERROR("Unable to parse data.");



















    public final String message;
    InternalErrorCode(String message){
        this.message = message;
    }
}
