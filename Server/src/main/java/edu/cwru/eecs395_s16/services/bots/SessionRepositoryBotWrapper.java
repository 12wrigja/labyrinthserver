package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
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
    public InternalResponseObject<Player> findPlayer(UUID clientID) {
        Optional<GameBot> bot = GameEngine.instance().getBotService().botForSessionID(clientID);
        if(bot.isPresent()){
            return new InternalResponseObject<>(bot.get(),"bot");
        } else {
            return actualRepo.findPlayer(clientID);
        }
    }

    @Override
    public InternalResponseObject<Player> findPlayer(String username) {
        Optional<GameBot> bot = GameEngine.instance().getBotService().botForUsername(username);
        if(bot.isPresent()){
            return new InternalResponseObject<>(bot.get(),"bot");
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

    @Override
    public void expirePlayerSession(UUID clientID) {
        //Directly forward this to the wrapped session repo.
        //The bot service will take care of expiring the session id automatically.
        actualRepo.expirePlayerSession(clientID);
    }

    @Override
    public void expirePlayerSession(String username) {
        //Directly forward this to the wrapped session repo.
        //The bot service will take care of expiring the session id automatically.
        actualRepo.expirePlayerSession(username);
    }

}
