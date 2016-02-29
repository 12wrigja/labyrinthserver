package edu.cwru.eecs395_s16.interfaces.repositories;

import edu.cwru.eecs395_s16.auth.exceptions.DuplicateUsernameException;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.MismatchedPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;

import java.util.Optional;

/**
 * Created by james on 1/21/16.
 */
public interface PlayerRepository {

    InternalResponseObject<Player> registerPlayer(String username, String password, String passwordConfirm);

    InternalResponseObject<Player> loginPlayer(String username, String password);

    InternalResponseObject<Player> findPlayer(String username);

    boolean savePlayer(Player p);

    boolean deletePlayer(Player p);

}
