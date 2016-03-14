package edu.cwru.eecs395_s16.test.persistance;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.exceptions.UnknownUsernameException;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.bots.PassBot;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
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

    private static final String TEST_PASSWORD = "test";

    @Test
    public void testRegisterAsBot() {
        GameBot b = new PassBot();
        String botUsername = b.getUsername();
        InternalResponseObject<Player> duplicatePlayerResponse = GameEngine.instance().services.playerRepository.registerPlayer(botUsername, TEST_PASSWORD, TEST_PASSWORD);
        if (duplicatePlayerResponse.isNormal()) {
            fail("Should have caught that we were trying to register a player using a bot username!");
        } else if (duplicatePlayerResponse.getInternalErrorCode() != InternalErrorCode.RESTRICTED_USERNAME) {
            fail(duplicatePlayerResponse.getMessage());
        }
    }

    @Test
    public void testRetrieveBotFromPersistance() {
        GameBot b = new PassBot();
        InternalResponseObject<Player> retr = GameEngine.instance().services.playerRepository.findPlayer(b.getUsername());
        if (retr.isNormal() && retr.isPresent()) {
            Player retrieved = retr.get();
            if (retrieved instanceof GameBot) {
                GameBot retrievedBot = (GameBot) retrieved;
                assertEquals(b.getSessionId(), retrievedBot.getSessionId());
                assertEquals(b.getUsername(), retrievedBot.getUsername());
            } else {
                fail("Didnt find a bot - found somthing else.");
            }
        } else {
            fail(retr.getMessage());
        }
    }

    @Test
    public void testRetrieveBotFromSessionUsername() {
        GameBot b = new PassBot();
        InternalResponseObject<Player> retr = GameEngine.instance().services.sessionRepository.findPlayer(b.getUsername());
        if (retr.isNormal() && retr.isPresent()) {
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

    @Test
    public void testRetrieveBotFromSessionClientID() {
        GameBot b = new PassBot();
        InternalResponseObject<Player> retr = GameEngine.instance().services.sessionRepository.findPlayer(b.getSessionId());
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
