package networking;

import edu.cwru.eecs395_s16.GameEngine;
import io.socket.client.*;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by james on 1/26/16.
 */
public class AuthorizationTesting extends TestCase {

    Socket socket;
    JSONObject currentResponse;

    final Lock lock = new ReentrantLock();
    final Condition flag = lock.newCondition();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GameEngine.instance.start(4500);
        socket = IO.socket("http://localhost:4500/");
        socket.connect();
    }

    public void testCanEstablishConnection() throws JSONException, InterruptedException {
        JSONObject j = new JSONObject();
        while(true){
            if(socket.connected()){
                break;
            }
            Thread.sleep(1000);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        socket.disconnect();
    }
}
