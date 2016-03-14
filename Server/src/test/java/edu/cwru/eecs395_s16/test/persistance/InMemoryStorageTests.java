package edu.cwru.eecs395_s16.test.persistance;

import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;
import edu.cwru.eecs395_s16.interfaces.repositories.PlayerRepository;
import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;
import edu.cwru.eecs395_s16.services.cache.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.playerrepository.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.sessionrepository.InMemorySessionRepository;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by james on 2/17/16.
 */
public class InMemoryStorageTests {

    final String USERNAME = "James";
    final String PASSWORD = "test";

    @Test
    public void testInMemoryPlayerRepository(){
        PlayerRepository repo = new InMemoryPlayerRepository();
        InternalResponseObject<Player> playerResponse = repo.registerPlayer(USERNAME,PASSWORD,PASSWORD);

        if(playerResponse.isNormal()) {
            Player pl = playerResponse.get();
            assertEquals(USERNAME,pl.getUsername());
        } else {
            fail(playerResponse.getMessage());
        }
    }

    @Test
    public void testInMemorySessionRepository(){
        SessionRepository repo = new InMemorySessionRepository();
        Player p = new Player(-1,USERNAME,PASSWORD);
        final UUID sessionID = UUID.randomUUID();
        repo.storePlayer(sessionID,p);

        InternalResponseObject<Player> p1 = repo.findPlayer(sessionID);
        if(p1.isPresent()){
            assertEquals(p.getUsername(),p1.get().getUsername());
        } else {
            fail("Could not retrieve a player from the repository.");
        }
    }

    @Test
    public void testInMemoryCache(){
        CacheService cache = new InMemoryCacheService();
        cache.storeString("username",USERNAME);
        Optional<String> fromCache = cache.getString("username");
        if(fromCache.isPresent()) {
            assertEquals(USERNAME, fromCache.get());
        } else {
            fail("Unable to retrieve string from cache.");
        }
    }

}
