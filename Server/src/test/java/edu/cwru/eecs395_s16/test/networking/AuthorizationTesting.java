package edu.cwru.eecs395_s16.test.networking;

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

    private final String testUsername = "USERNAME_TEST";
    private final String testPassword = "PASSWORD_TEST";

    @Test
    public void testRegistrationWithNoConfirmPassword() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password",testPassword);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(422,result.getInt("status"));
    }

    @Test
    public void testRegisterWithNoPassword() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password_confirm",testPassword);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(422,result.getInt("status"));
    }


    @Test
    public void testRegistrationWithMismatchingConfirmPassword() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password",testPassword);
        registerData.put("password_confirm",testPassword+"BLAH");
        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(422,result.getInt("status"));
    }

    @Test
    public void testRegistrationWithMismatchingPassword() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password",testPassword+"BLAH");
        registerData.put("password_confirm",testPassword);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(422,result.getInt("status"));
    }

    @Test
    public void testRegistration() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password",testPassword);
        registerData.put("password_confirm",testPassword);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(200,result.getInt("status"));

        cleanupPlayer();
    }

    @Test
    public void testDuplicateRegistration() throws JSONException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password",testPassword);
        registerData.put("password_confirm",testPassword);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(200,result.getInt("status"));

        JSONObject result2 = emitEventAndWaitForResult("register",registerData);
        assertEquals(422,result2.getInt("status"));

        cleanupPlayer();
    }

    private void cleanupPlayer(){
        Player p = new Player(-1,testUsername, testPassword);
        if(!engine.getPlayerRepository().deletePlayer(p)) {
            fail("Unable to delete player from repo");
        }
    }

}
