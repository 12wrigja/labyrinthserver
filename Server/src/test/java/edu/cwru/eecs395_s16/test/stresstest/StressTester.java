package edu.cwru.eecs395_s16.test.stresstest;

import edu.cwru.eecs395_s16.test.NetworkedTest;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by james on 4/7/16.
 */
public class StressTester {

    private final String TEST_USERNAME = "USERNAMETEST";
    private final String TEST_PASSWORD = "PASSWORDTEST";

    //
    public static void main(String[] args) throws Exception {
        StressTester t = new StressTester();
        Date startTime = new Date();
        t.runTest();
        Date endTime = new Date();
        System.out.println("Elapsed Test Time: " + (endTime.getTime() - startTime.getTime()) + " milliseconds");
    }

    public void runTest() throws Exception {
        System.out.println("Start,"+ (new Date().getTime()));
        Socket socket = IO.socket("http://localhost:4600");
        socket.connect();

        int try_count = 0;
        while (true) {
            if (socket.connected()) {
                break;
            } else {
                try_count++;
                if (try_count > 5) {
                    fail("Client was unable to connect.");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    fail("Interrupted while waiting.");
                }
            }
        }

        System.out.println("Connect,"+ (new Date().getTime()));

        //Packet used to store results
        JSONObject res;

        //Packet used to store input for events
        JSONObject packet = new JSONObject();

        //Register user
        packet.put("username", TEST_USERNAME);
        packet.put("password", TEST_PASSWORD);
        packet.put("password_confirm", TEST_PASSWORD);
        res = NetworkedTest.emitEventAndWaitForResult(socket, "register", packet, 10);
        if (res.getInt("status") != 200) {
            fail("Unable to register user.");
        }
        packet = new JSONObject();

        System.out.println("Register,"+ (new Date().getTime()));

        //Login User
        packet.put("username", TEST_USERNAME);
        packet.put("password", TEST_PASSWORD);
        res = NetworkedTest.emitEventAndWaitForResult(socket, "login", packet, 10);
        if (res.getInt("status") != 200) {
            fail("Unable to login user.");
        }
        packet = new JSONObject();

        System.out.println("Login,"+ (new Date().getTime()));

        //Setup a match against a passbot
        packet.put("queue_with_passbot", true);
        res = NetworkedTest.emitEventAndWaitForResult(socket, "queue_up_heroes", packet, 10);
        if (res.getInt("status") != 200) {
            fail("Unable to setup match");
        }

        System.out.println("Queue,"+ (new Date().getTime()));

        JSONObject matchState = NetworkedTest.emitEventAndWaitForResult(socket, "match_state", new JSONObject(), 10);
        if (matchState.getInt("status") != 200) {
            fail("Unable to get match state.");
        }

        System.out.println("CheckStartMatch,"+ (new Date().getTime()));

        for (int i = 0; i < 300; i++) {
            System.out.println("Turn"+i+"Start,"+ (new Date().getTime()));
            boolean isMyTurn;
            do {
                isMyTurn = matchState.getJSONObject("match").getString("game_state").equals("hero_turn");
                Thread.sleep(100);
            } while (!isMyTurn);
            List<String> keys = new ArrayList<>();
            Iterator keyIter = matchState.getJSONObject("match").getJSONObject("board_objects").keys();
            while (keyIter.hasNext()) {
                keys.add(keyIter.next().toString());
            }
            List<JSONObject> myObjects = keys.stream().map(key -> {
                try {
                    return matchState.getJSONObject("match").getJSONObject("board_objects").getJSONObject(key);
                } catch (JSONException e) {
                    return null;
                }
            }).filter(obj -> obj != null && obj.optString("controller_id", "---").equals(TEST_USERNAME)).collect(Collectors.toList());

            myObjects.forEach(obj -> {
                JSONObject pkt = new JSONObject();
                try {
                    pkt.put("type", "pass");
                    pkt.put("character_id", obj.optString("id", "--"));
                    JSONObject result = NetworkedTest.emitEventAndWaitForResult(socket, "game_action", pkt, 60);
                    if (result.getInt("status") != 200) {
                        fail("Couldn't pass on a character we own!");
                    }
                } catch (JSONException e) {
                    //Do nothing should never be hit
                }
            });
            System.out.println("Turn"+i+"End,"+ (new Date().getTime()));
            Thread.sleep(1000);
        }

        packet = new JSONObject();

        System.out.println("Cleanup,"+ (new Date().getTime()));

        res = NetworkedTest.emitEventAndWaitForResult(socket, "leave_match", packet, 10);
        if(res.getInt("status") != 200){
            fail("Unable to leave match.");
        }

        System.out.println("LeaveMatch,"+ (new Date().getTime()));

        socket.disconnect();

        System.out.println("Disconnect,"+ (new Date().getTime()));

    }

    private void fail(String s) {
        System.err.println(s);
    }

}
