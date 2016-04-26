package edu.cwru.eecs395_s16.test.services.playerrepo;

import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.services.players.PlayerRepository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by james on 2/29/16.
 */
public abstract class PlayerRepositoryBaseTest {

    private final String TEST_USERNAME = "USERNAMETEST";
    private final String TEST_PASSWORD = "PASSWORDTEST";
    private final String TEST_BAD_USERNAME = "USERNAME_TEST";

    public abstract PlayerRepository getRepositoryImplementation();

    @Test
    public void testNormalRegisterPlayer() {
        InternalResponseObject<Player> playerResponse = getRepositoryImplementation().registerPlayer(TEST_USERNAME,
                TEST_PASSWORD, TEST_PASSWORD);
        if (playerResponse.isNormal()) {
            Player pl = playerResponse.get();
            assertEquals(TEST_USERNAME, pl.getUsername());
        } else {
            fail(playerResponse.getMessage());
        }
    }


    @Test
    public void testRegistrationWithNoConfirmPassword() {
        //Test to make sure that registration fails if you try to register without matching passwords
        InternalResponseObject<Player> playerResponse = getRepositoryImplementation().registerPlayer(TEST_USERNAME,
                TEST_PASSWORD, null);
        if (playerResponse.isNormal()) {
            fail("Should have caught that the player's password confirmation was invalid.");
        } else if (playerResponse.getInternalErrorCode() != InternalErrorCode.MISMATCHED_PASSWORD) {
            fail(playerResponse.getMessage());
        }
    }

    @Test
    public void testRegisterWithNoPassword() {
        //Test to make sure that registration fails if you try to register without matching passwords
        InternalResponseObject<Player> playerResponse = getRepositoryImplementation().registerPlayer(TEST_USERNAME,
                null, TEST_PASSWORD);
        if (playerResponse.isNormal()) {
            fail("Should have caught that the player's password was invalid.");
        } else if (playerResponse.getInternalErrorCode() != InternalErrorCode.MISMATCHED_PASSWORD) {
            fail(playerResponse.getMessage());
        }
    }


    @Test
    public void testRegistrationWithMismatchingConfirmPassword() {
        //Test to make sure that registration fails if you try to register without matching passwords
        InternalResponseObject<Player> playerResponse = getRepositoryImplementation().registerPlayer(TEST_USERNAME,
                TEST_PASSWORD, TEST_PASSWORD + "BLAH");
        if (playerResponse.isNormal()) {
            fail("Should have caught that the player's password confirmation was invalid.");
        } else if (playerResponse.getInternalErrorCode() != InternalErrorCode.MISMATCHED_PASSWORD) {
            fail(playerResponse.getMessage());
        }
    }

    @Test
    public void testRegistrationWithMismatchingPassword() {
        InternalResponseObject<Player> playerResponse = getRepositoryImplementation().registerPlayer(TEST_USERNAME,
                TEST_PASSWORD + "BLAH", TEST_PASSWORD);
        if (playerResponse.isNormal()) {
            fail("Should have caught that the player's password confirmation was invalid.");
        } else if (playerResponse.getInternalErrorCode() != InternalErrorCode.MISMATCHED_PASSWORD) {
            fail(playerResponse.getMessage());
        }
    }

    @Test
    public void testDuplicateRegistration() {
        InternalResponseObject<Player> playerResponse = getRepositoryImplementation().registerPlayer(TEST_USERNAME,
                TEST_PASSWORD, TEST_PASSWORD);
        if (playerResponse.isNormal()) {
            Player pl = playerResponse.get();
            assertEquals(TEST_USERNAME, pl.getUsername());
        } else {
            fail(playerResponse.getMessage());
        }

        InternalResponseObject<Player> duplicatePlayerResponse = getRepositoryImplementation().registerPlayer
                (TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD);
        if (duplicatePlayerResponse.isNormal()) {
            fail("Should have caught that we were trying to register a player with the same username");
        } else if (duplicatePlayerResponse.getInternalErrorCode() != InternalErrorCode.DUPLICATE_USERNAME) {
            fail(duplicatePlayerResponse.getMessage());
        }
    }

    @Test
    public void testInvalidUsernameRegistration() {
        InternalResponseObject<Player> duplicatePlayerResponse = getRepositoryImplementation().registerPlayer
                (TEST_BAD_USERNAME, TEST_PASSWORD, TEST_PASSWORD);
        if (duplicatePlayerResponse.isNormal()) {
            fail("Should have caught that we were trying to register a player with a bad username");
        } else if (duplicatePlayerResponse.getInternalErrorCode() != InternalErrorCode.INVALID_USERNAME) {
            fail(duplicatePlayerResponse.getMessage());
        }
    }

    @Test
    public void testCleanPlayerMethod() {
        InternalResponseObject<Player> p1 = getRepositoryImplementation().registerPlayer(TEST_USERNAME,
                TEST_PASSWORD, TEST_PASSWORD);
        assertTrue(p1.isPresent());
        p1 = getRepositoryImplementation().findPlayer(TEST_USERNAME);
        assertTrue(p1.isPresent());
        cleanupPlayer();
        p1 = getRepositoryImplementation().findPlayer(TEST_USERNAME);
        assertFalse(p1.isPresent());
    }

    @Before
    public void cleanupPlayer() {
        //Check to see if the player exists first.
        InternalResponseObject<Player> p1 = getRepositoryImplementation().findPlayer(TEST_USERNAME);
        if (p1.isPresent()) {
            if (!getRepositoryImplementation().deletePlayer(p1.get())) {
                fail("Unable to delete player from repo");
            }
        }
    }

}
