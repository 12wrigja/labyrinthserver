package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.DuplicateUsernameException;
import edu.cwru.eecs395_s16.auth.exceptions.InvalidPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.MismatchedPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.bots.GameBot;
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
    public Optional<Player> registerPlayer(String username, String password, String passwordConfirm) throws DuplicateUsernameException, MismatchedPasswordException {
        Optional<GameBot> bot = GameEngine.instance().getBotService().botForUsername(username);
        if(bot.isPresent()){
            throw new DuplicateUsernameException(username);
        } else {
            return actualRepo.registerPlayer(username,password,passwordConfirm);
        }
    }

    @Override
    public Optional<Player> loginPlayer(String username, String password) throws UnknownUsernameException, InvalidPasswordException {
        Optional<GameBot> bot = GameEngine.instance().getBotService().botForUsername(username);
        if(bot.isPresent()){
            throw new InvalidPasswordException();
        } else {
            return actualRepo.loginPlayer(username,password);
        }
    }

    @Override
    public Optional<Player> findPlayer(String username) throws UnknownUsernameException {
        Optional<GameBot> bot = GameEngine.instance().getBotService().botForUsername(username);
        if(bot.isPresent()){
            return Optional.of(bot.get());
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
