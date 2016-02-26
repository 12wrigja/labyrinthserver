package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;
import edu.cwru.eecs395_s16.core.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by james on 1/21/16.
 */
public class InMemorySessionRepository implements SessionRepository {

    private Map<UUID, Player> sessionMap = new ConcurrentHashMap<>();
    private Map<String,UUID> connectionMap = new ConcurrentHashMap<>();

    @Override
    public Optional<Player> findPlayer(UUID token) {
        return Optional.ofNullable(sessionMap.get(token));
    }

    @Override
    public Optional<Player> findPlayer(String username) throws UnknownUsernameException {
        UUID clientID = connectionMap.get(username);
        if(clientID == null){
            throw new UnknownUsernameException(username);
        } else {
            return findPlayer(clientID);
        }
    }

    @Override
    public void storePlayer(UUID token, Player player) {
        sessionMap.put(token, player);
        connectionMap.put(player.getUsername(), token);
    }
}
