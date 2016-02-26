package edu.cwru.eecs395_s16.test.networking;

import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.bots.PassBot;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.test.NetworkedTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by james on 1/26/16.
 */
public class AuthorizationTesting extends NetworkedTest {

    private final String TEST_USERNAME = "USERNAMETEST";
    private final String TEST_PASSWORD = "PASSWORDTEST";

    private final String TEST_BAD_USERNAME = "USERNAME_TEST";

    @Test
    public void testRegistrationWithNoConfirmPassword() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username", TEST_USERNAME);
        registerData.put("password", TEST_PASSWORD);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register", registerData);
        assertEquals(422, result.getInt("status"));
    }

    @Test
    public void testRegisterWithNoPassword() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username", TEST_USERNAME);
        registerData.put("password_confirm", TEST_PASSWORD);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register", registerData);
        assertEquals(422, result.getInt("status"));
    }


    @Test
    public void testRegistrationWithMismatchingConfirmPassword() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username", TEST_USERNAME);
        registerData.put("password", TEST_PASSWORD);
        registerData.put("password_confirm", TEST_PASSWORD + "BLAH");
        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register", registerData);
        assertEquals(422, result.getInt("status"));
    }

    @Test
    public void testRegistrationWithMismatchingPassword() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username", TEST_USERNAME);
        registerData.put("password", TEST_PASSWORD + "BLAH");
        registerData.put("password_confirm", TEST_PASSWORD);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register", registerData);
        assertEquals(422, result.getInt("status"));
    }

    @Test
    public void testRegistration() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username", TEST_USERNAME);
        registerData.put("password", TEST_PASSWORD);
        registerData.put("password_confirm", TEST_PASSWORD);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register", registerData);
        assertEquals(200, result.getInt("status"));

        cleanupPlayer();
    }

    @Test
    public void testDuplicateRegistration() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username", TEST_USERNAME);
        registerData.put("password", TEST_PASSWORD);
        registerData.put("password_confirm", TEST_PASSWORD);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register", registerData);
        assertEquals(200, result.getInt("status"));

        JSONObject result2 = emitEventAndWaitForResult("register", registerData);
        assertEquals(422, result2.getInt("status"));

        cleanupPlayer();
    }

    @Test
    public void testInvalidUsernameRegistration() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username", TEST_BAD_USERNAME);
        registerData.put("password", TEST_PASSWORD);
        registerData.put("password_confirm", TEST_PASSWORD);

        JSONObject result2 = emitEventAndWaitForResult("register", registerData);
        assertEquals(422, result2.getInt("status"));
    }

    @Test
    public void testRegisterAsBot() throws JSONException {
        GameBot b = new PassBot();
        String botUsername = b.getUsername();

        JSONObject registerData = new JSONObject();
        registerData.put("username", botUsername);
        registerData.put("password", TEST_PASSWORD);
        registerData.put("password_confirm", TEST_PASSWORD);

        JSONObject result2 = emitEventAndWaitForResult("register", registerData);
        assertEquals(422, result2.getInt("status"));
    }

    private void cleanupPlayer() {
        Player p = new Player(-1, TEST_USERNAME, TEST_PASSWORD);
        if (!engine.getPlayerRepository().deletePlayer(p)) {
            fail("Unable to delete player from repo");
        }
    }

}
