package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/25/16.
 */
public class SessionRepositoryBotWrapper implements SessionRepository {

    private final SessionRepository actualRepo;

    public SessionRepositoryBotWrapper(SessionRepository actualRepo) {
        this.actualRepo = actualRepo;
    }

    @Override
    public Optional<Player> findPlayer(UUID clientID) {
        Optional<GameBot> bot = GameEngine.instance().getBotService().botForSessionID(clientID);
        if(bot.isPresent()){
            return Optional.of(bot.get());
        } else {
            return actualRepo.findPlayer(clientID);
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
    public void storePlayer(UUID clientID, Player player) {
        Optional<GameBot> bot = GameEngine.instance().getBotService().botForSessionID(clientID);
        if(!bot.isPresent()){
            actualRepo.storePlayer(clientID,player);
        }
    }

}
