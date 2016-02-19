package edu.cwru.eecs395_s16.test.persistance;

import edu.cwru.eecs395_s16.auth.exceptions.DuplicateUsernameException;
import edu.cwru.eecs395_s16.auth.exceptions.MismatchedPasswordException;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;
import edu.cwru.eecs395_s16.interfaces.repositories.PlayerRepository;
import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;
import edu.cwru.eecs395_s16.services.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.InMemorySessionRepository;
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
        try {
            repo.registerPlayer(USERNAME,PASSWORD,PASSWORD);
        } catch (DuplicateUsernameException e) {
            fail("There was already a player in the in-memory store already with that username.");
        } catch (MismatchedPasswordException e) {
            fail("Somehow, the passwords arent the same. Fix this test!");
        }
        try {
            Optional<Player> p = repo.findPlayer(USERNAME);
            if(p.isPresent()){
                Player pl = p.get();
                assertEquals(USERNAME,pl.getUsername());
            }
        } catch (UnknownUsernameException e) {
            fail("We couldn't find that player in the repo.");
        }
    }

    @Test
    public void testInMemorySessionRepository(){
        SessionRepository repo = new InMemorySessionRepository();
        Player p = new Player(-1,USERNAME,PASSWORD);
        final UUID sessionID = UUID.randomUUID();
        repo.storePlayer(sessionID,p);

        Optional<Player> p1 = repo.findPlayer(sessionID);
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
