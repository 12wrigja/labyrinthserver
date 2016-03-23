package edu.cwru.eecs395_s16.services.sessions;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.services.players.PlayerRepository;
import edu.cwru.eecs395_s16.services.connections.GameClient;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/26/16.
 */
public class RedisSessionRepository implements SessionRepository {

    private static final String SESSION_KEY_PREFIX = "SESSION:";
    private static final String SESSION_CLIENT_ID_PREFIX = "CLIENTID:";
    private static final String SESSION_USERNAME_PREFIX = "USERNAME:";

    private final JedisPool pool;
    private final PlayerRepository backingRepo;

    public RedisSessionRepository(JedisPool pool, PlayerRepository backingRepo) {
        this.pool = pool;
        this.backingRepo = backingRepo;
    }

    @Override
    public InternalResponseObject<Player> findPlayer(UUID clientID) {
        try (Jedis j = pool.getResource()) {
            String username = j.get(SESSION_KEY_PREFIX + SESSION_CLIENT_ID_PREFIX + clientID.toString());
            InternalResponseObject<Player> p = backingRepo.findPlayer(username);
            if (p.isPresent()) {
                InternalResponseObject<GameClient> client = GameEngine.instance().findClientFromUUID(clientID);
                if (client.isNormal() && client.isPresent()) {
                    p.get().setClient(Optional.of(client.get()));
                }
            }
            return p;
        }
    }

    @Override
    public InternalResponseObject<Player> findPlayer(String username) {
        try (Jedis j = pool.getResource()) {
            String clientIDString = j.get(SESSION_KEY_PREFIX + SESSION_USERNAME_PREFIX + username);
            if(clientIDString == null){
                return new InternalResponseObject<>(InternalErrorCode.UNKNOWN_SESSION_IDENTIFIER);
            }
            UUID clientID;
            try {
                clientID = UUID.fromString(clientIDString);
            } catch (IllegalArgumentException e) {
                return new InternalResponseObject<>(WebStatusCode.SERVER_ERROR, InternalErrorCode.DATA_PARSE_ERROR);
            }
            InternalResponseObject<Player> p = backingRepo.findPlayer(username);
            if (p.isPresent()) {
                InternalResponseObject<GameClient> client = GameEngine.instance().findClientFromUUID(clientID);
                if (client.isNormal() && client.isPresent()) {
                    p.get().setClient(Optional.of(client.get()));
                }
            }
            return p;
        }
    }

    @Override
    public void storePlayer(UUID clientID, Player player) {
        try (Jedis j = pool.getResource()) {
            j.set(SESSION_KEY_PREFIX + SESSION_USERNAME_PREFIX + player.getUsername(), clientID.toString());
            j.set(SESSION_KEY_PREFIX + SESSION_CLIENT_ID_PREFIX + clientID.toString(), player.getUsername());
        }
    }

    @Override
    public void expirePlayerSession(UUID clientID) {
        InternalResponseObject<Player> p = findPlayer(clientID);
        if(p.isPresent()) {
            Player player = p.get();
            try (Jedis j = pool.getResource()) {
                j.del(SESSION_KEY_PREFIX + SESSION_USERNAME_PREFIX + player.getUsername());
                j.del(SESSION_KEY_PREFIX + SESSION_CLIENT_ID_PREFIX + clientID.toString());
            }
        }
    }

    @Override
    public void expirePlayerSession(String username) {
        InternalResponseObject<Player> p = findPlayer(username);
        if(p.isPresent()){
            Player player = p.get();
            try (Jedis j = pool.getResource()){
                j.del(SESSION_KEY_PREFIX + SESSION_USERNAME_PREFIX + player.getUsername());
                Optional<GameClient> c = p.get().getClient();
                if(c.isPresent()) {
                    j.del(SESSION_KEY_PREFIX + SESSION_CLIENT_ID_PREFIX + c.get().getSessionId());
                }
            }
        }
    }
}
