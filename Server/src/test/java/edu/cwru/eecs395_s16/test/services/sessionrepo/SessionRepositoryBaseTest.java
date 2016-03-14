package edu.cwru.eecs395_s16.test.services.sessionrepo;

import com.corundumstudio.socketio.SocketIOClient;
import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;
import edu.cwru.eecs395_s16.interfaces.services.GameClient;
import edu.cwru.eecs395_s16.test.NetworkedTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by james on 3/13/16.
 */
public abstract class SessionRepositoryBaseTest extends NetworkedTest {

    public abstract SessionRepository getRepositoryImplementation();

    private static final String TEST_USERNAME = "USERNAMETEST";
    private static final String TEST_PASSWORD = "PASSWORDTEST";
    private static Player registeredPlayer;

    @BeforeClass
    public static void registerPlayer(){
        InternalResponseObject<Player> p = GameEngine.instance().services.playerRepository.registerPlayer(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD);
        assertTrue(p.isNormal());
        registeredPlayer = p.get();
    }

    @AfterClass
    public static void cleanupPlayer(){
        if(registeredPlayer != null) {
            boolean resp = GameEngine.instance().services.playerRepository.deletePlayer(registeredPlayer);
            if (!resp) {
                fail("Unable to clean up player.");
            }
        }
    }

    public void testFindPlayerFromSessionID(){
        connectSocketIOClient();
        //Now try to find the player based off the session identifier

    }

    public void testFindBotFromSessionID(){

    }

    public void testPlayerDisconnect(){
        connectSocketIOClient();
    }

    public void testBotDisconnect(){

    }

    public void testFindPlayerFromUsername(){
        connectSocketIOClient();
    }

    public void testFindBotFromUsername(){

    }

}
