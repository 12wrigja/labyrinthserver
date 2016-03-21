package edu.cwru.eecs395_s16.services.playerrepository;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.interfaces.repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 1/19/16.
 */
public class InMemoryPlayerRepository implements PlayerRepository {

    private Map<String, Player> playerMap = new HashMap<>();

    @Override
    public InternalResponseObject<Player> registerPlayer(String username, String password, String passwordConfirm) {
        if(username == null || !username.matches("[a-zA-Z0-9]+")){
            return new InternalResponseObject<Player>(InternalErrorCode.INVALID_USERNAME);
        }
        if(password == null || passwordConfirm == null || !password.equals(passwordConfirm)){
            return new InternalResponseObject<>(InternalErrorCode.MISMATCHED_PASSWORD);
        }
        if (playerMap.containsKey(username)) {
            return new InternalResponseObject<>(InternalErrorCode.DUPLICATE_USERNAME);
        } else {
            //TODO change this back. Probably.
            Player p = new Player(-1,username, password, true);
            playerMap.put(username, p);
            return new InternalResponseObject<>(p);
        }
    }

    public InternalResponseObject<Player> loginPlayer(String username, String password) {
        if (!playerMap.containsKey(username)) {
            return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_USERNAME);
        } else {
            Player p = playerMap.get(username);
            if (p.checkPassword(password)) {
                return new InternalResponseObject<>(p);
            } else {
                return new InternalResponseObject<>(InternalErrorCode.INVALID_PASSWORD);
            }
        }
    }

    @Override
    public InternalResponseObject<Player> findPlayer(String username) {
        return new InternalResponseObject<>(playerMap.get(username));
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
        }
        return true;
    }

    public void initialize(List<List<String>> players) {
        for(List<String> playerData : players){
            Player p = new Player(-1,playerData.get(1),playerData.get(2),Boolean.parseBoolean(playerData.get(4)));
            playerMap.put(p.getUsername(),p);
        }
    }
}
