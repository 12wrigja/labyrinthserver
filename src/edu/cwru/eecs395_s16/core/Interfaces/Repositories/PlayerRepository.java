package edu.cwru.eecs395_s16.core.Interfaces.Repositories;

import edu.cwru.eecs395_s16.auth.exceptions.DuplicateUsernameException;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.core.Player;

/**
 * Created by james on 1/21/16.
 */
public interface PlayerRepository {

    Player registerPlayer(String username, String password) throws DuplicateUsernameException;

    Player loginPlayer(String username, String password) throws UnknownUsernameException, InvalidPasswordException;

    Player findPlayer(String username) throws UnknownUsernameException;

    void savePlayer(Player p);

}
