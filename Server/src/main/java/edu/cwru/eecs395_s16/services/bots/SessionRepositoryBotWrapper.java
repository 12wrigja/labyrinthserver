package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.services.connections.GameClient;
import edu.cwru.eecs395_s16.services.sessions.SessionRepository;

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
        Optional<GameBot> bot = GameEngine.instance().botService.botForSessionID(clientID);
        if(bot.isPresent()){
            return new InternalResponseObject<>(bot.get(),"bot");
        } else {
            return actualRepo.findPlayer(clientID);
        }
    }

    @Override
    public Optional<GameClient> findClient(Player player) {
        if(player instanceof GameBot){
            return Optional.of((GameBot)player);
        } else {
            return actualRepo.findClient(player);
        }
    }

    @Override
    public void storePlayer(UUID clientID, Player player) {
        Optional<GameBot> bot = GameEngine.instance().botService.botForSessionID(clientID);
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

}
