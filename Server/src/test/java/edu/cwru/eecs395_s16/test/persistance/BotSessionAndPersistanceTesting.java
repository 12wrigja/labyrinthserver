package edu.cwru.eecs395_s16.test.persistance;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.bots.PassBot;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.test.EngineOnlyTest;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by james on 2/26/16.
 */
public class BotSessionAndPersistanceTesting extends EngineOnlyTest {

    @Test
    public void testRetrieveBotFromPersistance() {
        GameBot b = new PassBot();
        try {
            Optional<Player> retr = GameEngine.instance().getPlayerRepository().findPlayer(b.getUsername());
            if (retr.isPresent()) {
                Player retrieved = retr.get();
                if (retrieved instanceof GameBot) {
                    GameBot retrievedBot = (GameBot) retrieved;
                    assertEquals(b.getSessionId(), retrievedBot.getSessionId());
                    assertEquals(b.getUsername(), retrievedBot.getUsername());
                } else {
                    fail("Didnt find a bot - found somthing else.");
                }
            } else {
                fail("Didnt find the bot.");
            }
        } catch (UnknownUsernameException e) {
            fail("Unknown username");
        }
    }

    @Test
    public void testRetrieveBotFromSessionUsername() {
        GameBot b = new PassBot();
        try {
            Optional<Player> retr = GameEngine.instance().getSessionRepository().findPlayer(b.getUsername());
            if (retr.isPresent()) {
                Player retrieved = retr.get();
                if (retrieved instanceof GameBot) {
                    GameBot retrievedBot = (GameBot) retrieved;
                    assertEquals(b.getSessionId(), retrievedBot.getSessionId());
                    assertEquals(b.getUsername(), retrievedBot.getUsername());
                } else {
                    fail("Didn't find a bot - found something else.");
                }
            } else {
                fail("Didn't find the bot.");
            }
        } catch (UnknownUsernameException e) {
            fail("Unknown username");
        }
    }

    @Test
    public void testRetrieveBotFromSessionClientID() {
        GameBot b = new PassBot();
        Optional<Player> retr = GameEngine.instance().getSessionRepository().findPlayer(b.getSessionId());
        if (retr.isPresent()) {
            Player retrieved = retr.get();
            if (retrieved instanceof GameBot) {
                GameBot retrievedBot = (GameBot) retrieved;
                assertEquals(b.getSessionId(), retrievedBot.getSessionId());
                assertEquals(b.getUsername(), retrievedBot.getUsername());
            } else {
                fail("Didn't find a bot - found something else.");
            }
        } else {
            fail("Didn't find the bot.");
        }
    }

}
