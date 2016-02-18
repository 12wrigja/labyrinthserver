package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.auth.exceptions.DuplicateUsernameException;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.MismatchedPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.interfaces.repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by james on 1/19/16.
 */
public class InMemoryPlayerRepository implements PlayerRepository {

    private Map<String, Player> playerMap = new HashMap<>();

    @Override
    public Optional<Player> registerPlayer(String username, String password, String passwordConfirm) throws DuplicateUsernameException, MismatchedPasswordException {
        if(!password.equals(passwordConfirm)){
            throw new MismatchedPasswordException();
        }
        if (playerMap.containsKey(username)) {
            throw new DuplicateUsernameException(username);
        } else {
            Player p = new Player(username, password);
            playerMap.put(username, p);
            return Optional.of(p);
        }
    }

    public Optional<Player> loginPlayer(String username, String password) throws UnknownUsernameException, InvalidPasswordException {
        if (!playerMap.containsKey(username)) {
            throw new UnknownUsernameException(username);
        } else {
            Player p = playerMap.get(username);
            if (p.checkPassword(password)) {
                return Optional.of(p);
            } else {
                throw new InvalidPasswordException();
            }
        }
    }

    @Override
    public Optional<Player> findPlayer(String username) throws UnknownUsernameException {
        return Optional.ofNullable(playerMap.get(username));
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
