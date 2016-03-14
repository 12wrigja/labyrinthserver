package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.repositories.PlayerRepository;

import java.util.Optional;

/**
 * Created by james on 2/25/16.
 */
public class PlayerRepositoryBotWrapper implements PlayerRepository {

    private final PlayerRepository actualRepo;

    public PlayerRepositoryBotWrapper(PlayerRepository actual){
        this.actualRepo = actual;
    }

    @Override
    public InternalResponseObject<Player> registerPlayer(String username, String password, String passwordConfirm) {
        Optional<GameBot> bot = GameEngine.instance().botService.botForUsername(username);
        if(bot.isPresent()){
            return new InternalResponseObject<Player>(InternalErrorCode.RESTRICTED_USERNAME);
        } else {
            return actualRepo.registerPlayer(username,password,passwordConfirm);
        }
    }

    @Override
    public InternalResponseObject<Player> loginPlayer(String username, String password) {
        Optional<GameBot> bot = GameEngine.instance().botService.botForUsername(username);
        if(bot.isPresent()){
            return new InternalResponseObject<Player>(InternalErrorCode.RESTRICTED_USERNAME);
        } else {
            return actualRepo.loginPlayer(username,password);
        }
    }

    @Override
    public InternalResponseObject<Player> findPlayer(String username) {
        Optional<GameBot> bot = GameEngine.instance().botService.botForUsername(username);
        if(bot.isPresent()){
            return new InternalResponseObject<>(bot.get());
        } else {
            return actualRepo.findPlayer(username);
        }
    }

    @Override
    public boolean savePlayer(Player p) {
        return p instanceof GameBot || actualRepo.savePlayer(p);
    }

    @Override
    public boolean deletePlayer(Player p) {
        return !(p instanceof GameBot) && actualRepo.deletePlayer(p);
    }
}
