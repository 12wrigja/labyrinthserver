package networking;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.auth.InMemorySessionRepository;
import edu.cwru.eecs395_s16.core.Player;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;

import java.net.BindException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

/**
 * Created by james on 1/26/16.
 */
public class AuthorizationTesting {

    static Socket socket;
    static GameEngine engine;
    static final int MAX_TRY_COUNT = 5;
    static final int PORT = 4500;
    private final String testUsername = "USERNAME_TEST";
    private final String testPassword = "PASSWORD_TEST";

    @BeforeClass
    public static void setUpGameEngine() throws Exception {
        System.out.println("Setting up game engine.");
        engine = new GameEngine(new InMemoryPlayerRepository(), new InMemorySessionRepository());
        engine.setServerPort(PORT);
        int try_count = 0;
        while (true) {
            try {
                engine.start();
                break;
            } catch (BindException e) {
                System.err.println("Retrying binding. Port not available.");
                try_count++;
                if(try_count > MAX_TRY_COUNT){
                    fail("Unable to setup game server.");
                }
                Thread.sleep(1000);
            }
        }
        socket = IO.socket("http://localhost:"+PORT);
        socket.connect();
    }


    @Test
    public void testCanEstablishConnection() throws JSONException, InterruptedException {
        JSONObject j = new JSONObject();
        int try_count = 0;
        while (true) {
            if (socket.connected()) {
                break;
            } else {
                try_count ++;
                if(try_count > MAX_TRY_COUNT){
                    fail("Client was unable to connect.");
                }
                Thread.sleep(1000);
            }
        }
    }

    @Test
    public void testRegistrationWithNoConfirmPassword() throws JSONException, InterruptedException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password",testPassword);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(422,result.getInt("status"));
    }

    @Test
    public void testRegisterWithNoPassword() throws JSONException, InterruptedException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password_confirm",testPassword);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(422,result.getInt("status"));
    }


    @Test
    public void testRegistrationWithMismatchingConfirmPassword() throws JSONException, InterruptedException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password",testPassword);
        registerData.put("password_confirm",testPassword+"BLAH");
        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(422,result.getInt("status"));
    }

    @Test
    public void testRegistrationWithMismatchingPassword() throws JSONException, InterruptedException {
        JSONObject registerData = new JSONObject();
        registerData.put("username",testUsername);
        registerData.put("password",testPassword+"BLAH");
        registerData.put("password_confirm",testPassword);

        //Test to make sure that registration fails if you try to register without matching passwords
        JSONObject result = emitEventAndWaitForResult("register",registerData);
        assertEquals(422,result.getInt("status"));
    }

    @Test
    public void testRegistration() throws JSONException, InterruptedException {
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
    public void testDuplicateRegistration() throws JSONException, InterruptedException {
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

    @AfterClass
    public static void tearDownGameEngine() throws Exception {
        System.out.println("Tearing down game engine.");
        socket.disconnect();
        engine.stop();
    }

    private void cleanupPlayer(){
        Player p = new Player(testUsername, testPassword);
        if(!engine.playerRepository.deletePlayer(p)) {
            fail("Unable to delete player from repo");
        }
    }

    private JSONObject emitEventAndWaitForResult(String event, JSONObject data) throws InterruptedException{
        final JSONObject[] response = {null};
        final Lock lock = new ReentrantLock();
        final Condition flag = lock.newCondition();
        socket.emit(event, data, (Ack) args -> {
            lock.lock();
            if(args.length > 0) {
                Object returnVal = args[0];
                if (returnVal instanceof JSONObject) {
                    response[0] = (JSONObject) returnVal;
                }
            }
            flag.signal();
            lock.unlock();
        });
        lock.lock();
        flag.await(5, TimeUnit.SECONDS);
        lock.unlock();
        if(response[0] == null){
            fail("No response was given for command "+event+" with data: "+data.toString());
        }
        return response[0];
    };
}
