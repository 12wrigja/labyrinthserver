package edu.cwru.eecs395_s16.core;

/**
 * Created by james on 2/28/16.
 */
public enum InternalErrorCode {
    //Generic errors
    UNKNOWN("The internal error is unknown."),
    DATA_PARSE_ERROR("Unable to parse data."),

    //Player Auth
    DUPLICATE_USERNAME("The given username already exists."),
    UNKNOWN_USERNAME("The given username could not be found."),
    INVALID_PASSWORD("The given password is invalid."),
    MISMATCHED_PASSWORD("The given passwords do not match."),
    RESTRICTED_USERNAME("The given username is restricted."),
    INVALID_USERNAME("The given username is not valid."),

    //Session Auth
    UNKNOWN_SESSION_IDENTIFIER("The given session identifier could not be resolved."),

    //Map Stuff
    UNKNOWN_MAP_IDENTIFIER("The given map identifier could not be resolved."),
    INVALID_MAP_DEFINITION("The map with the given identifier is not properly defined."),

    //Database errors
    INVALID_SQL("Some SQL was invalid."),

    //Match creation and info retrieval for matches
    MISSING_PLAYER("Unable to retrieve a player from given information."),
    PLAYER_BUSY("A player is currently busy."),
    MATCH_RETRIEVAL_ERROR("Unable to retrieve a match from storage."),

    //Queuing
    ALREADY_IN_QUEUE("You are already in a queue."),
    NOT_IN_QUEUE("You are not in a queue."),
    NOT_IN_MATCH("You are not in a match."),

    //Game Errors
    INVALID_GAME_ACTION("The game action is not valid."),
    NOT_YOUR_TURN("It is not currently your turn."),
    INVALID_OBJECT("The given game object is not valid for this action."),
    UNKNOWN_OBJECT("The given identifier is not a valid game object."),

    //Character Action Errors
    NO_ACTION_POINTS("The given creature is exhausted."),

    //Movement errors
    PATH_TOO_LONG("The path is longer than the movement value."),
    PATH_OBSTRUCTED("The path is obstructed."),
    PATH_SKIP("The path has gaps in it.");




















    public final String message;
    InternalErrorCode(String message){
        this.message = message;
    }
}
