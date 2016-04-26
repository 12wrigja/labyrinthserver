package edu.cwru.eecs395_s16.auth.exceptions;

import edu.cwru.eecs395_s16.core.JsonableException;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

/**
 * Created by james on 2/18/16.
 */
public class UnauthorizedActionException extends JsonableException {
    public UnauthorizedActionException(Player player) {
        super(WebStatusCode.UNAUTHORIZED, "The player " + player.getUsername() + " is not authorized to do this " +
                "action.");
    }
}
