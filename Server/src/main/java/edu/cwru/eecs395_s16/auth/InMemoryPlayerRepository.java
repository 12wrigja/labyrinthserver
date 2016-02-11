package edu.cwru.eecs395_s16.auth;

import edu.cwru.eecs395_s16.auth.exceptions.DuplicateUsernameException;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.MismatchedPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 1/19/16.
 */
public class InMemoryPlayerRepository implements PlayerRepository {

    private Map<String, Player> playerMap = new HashMap<>();

    @Override
    public Player registerPlayer(String username, String password, String passwordConfirm) throws DuplicateUsernameException, MismatchedPasswordException {
        if(!password.equals(passwordConfirm)){
            throw new MismatchedPasswordException();
        }
        if (playerMap.containsKey(username)) {
            throw new DuplicateUsernameException(username);
        } else {
            Player p = new Player(username, password);
            playerMap.put(username, p);
            return p;
        }
    }

    public Player loginPlayer(String username, String password) throws UnknownUsernameException, InvalidPasswordException {
        if (!playerMap.containsKey(username)) {
            throw new UnknownUsernameException(username);
        } else {
            Player p = playerMap.get(username);
            if (p.checkPassword(password)) {
                return p;
            } else {
                throw new InvalidPasswordException();
            }
        }
    }

    @Override
    public Player findPlayer(String username) throws UnknownUsernameException {
        return playerMap.get(username);
    }

    @Override
    public boolean savePlayer(Player p) {
        //Do nothing as a player is already saved in memory. This method is for persisting to some sort of permanent storage
        return true;
    }

    @Override
    public boolean deletePlayer(Player p) {
        if(playerMap.containsKey(p.getUsername())) {
            playerMap.remove(p.getUsername());
            return true;
        } else {
            return false;
        }

    }

}
