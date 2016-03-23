package edu.cwru.eecs395_s16.services.players;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.services.containers.Repository;

/**
 * Created by james on 1/21/16.
 */
public interface PlayerRepository extends Repository {

    InternalResponseObject<Player> registerPlayer(String username, String password, String passwordConfirm);

    InternalResponseObject<Player> loginPlayer(String username, String password);

    InternalResponseObject<Player> findPlayer(String username);

    boolean savePlayer(Player p);

    boolean deletePlayer(Player p);

}
