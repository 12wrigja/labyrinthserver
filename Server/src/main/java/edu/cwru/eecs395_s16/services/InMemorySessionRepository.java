package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;
import edu.cwru.eecs395_s16.core.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public class InMemorySessionRepository implements SessionRepository {

    private Map<UUID, Player> sessionMap = new HashMap<>();

    @Override
    public Optional<Player> findPlayer(UUID token) {
        return Optional.ofNullable(sessionMap.get(token));
    }

    @Override
    public void storePlayer(UUID token, Player player) {
        sessionMap.put(token, player);
    }
}
