package edu.cwru.eecs395_s16.test.matches;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.networking.Response;
import edu.cwru.eecs395_s16.networking.requests.LoginUserRequest;
import edu.cwru.eecs395_s16.networking.requests.NoInputRequest;
import edu.cwru.eecs395_s16.networking.requests.RegisterUserRequest;
import edu.cwru.eecs395_s16.networking.requests.SpectateMatchRequest;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueArchitectRequest;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueHeroesRequest;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.test.NetworkTestCore;
import edu.cwru.eecs395_s16.test.SingleUserNetworkTest;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by james on 4/13/16.
 */
public class QueueingAndSpectatingTesting extends NetworkTestCore {

    private Socket heroClient;
    private Socket architectClient;

    private boolean isHeroRegistered = false;
    private boolean isArchitectRegistered = false;

    @Before
    public void setup() {
        String heroUsername = "HEROTEST";
        String architectUsername = "ARCHITESTTEST";
        String password = "tst123";

        engine.services.playerRepository.deletePlayer(new Player(-1,heroUsername,password,false));
        engine.services.playerRepository.deletePlayer(new Player(-1,architectUsername,password,false));

        Optional<Socket> heroSock = connectSocketIOClient();
        if(!heroSock.isPresent()){
            fail("Unable to create the hero socket.");
            return;
        }
        heroClient = heroSock.get();
        Optional<Socket> archSock = connectSocketIOClient();
        if(!archSock.isPresent()){
            fail("Unable to create the architect socket.");
            return;
        }
        architectClient = archSock.get();
        assertFalse(heroClient.id().equals(architectClient.id()));
        try {
            JSONObject heroRegisterResults = SingleUserNetworkTest.emitEventAndWaitForResult(heroClient, "register", new RegisterUserRequest(heroUsername, password, password).convertToJSON(), 5);
            JSONObject architectRegisterResults = SingleUserNetworkTest.emitEventAndWaitForResult(architectClient, "register", new RegisterUserRequest(architectUsername, password, password).convertToJSON(), 5);
            assertEquals(200, heroRegisterResults.getInt("status"));
            isHeroRegistered = true;
            assertEquals(200, architectRegisterResults.getInt("status"));
            isArchitectRegistered = true;
            JSONObject heroLoginResults = SingleUserNetworkTest.emitEventAndWaitForResult(heroClient, "login", new LoginUserRequest(heroUsername, password).convertToJSON(), 5);
            JSONObject architectLoginResults = SingleUserNetworkTest.emitEventAndWaitForResult(architectClient, "login", new LoginUserRequest(architectUsername, password).convertToJSON(), 5);
            assertEquals(200, heroLoginResults.getInt("status"));
            assertEquals(200, architectLoginResults.get("status"));
        } catch (JSONException e){
            if(isHeroRegistered){
                engine.services.playerRepository.deletePlayer(new Player(-1,heroUsername,password,false));
            }
            if(isArchitectRegistered){
                engine.services.playerRepository.deletePlayer(new Player(-1,architectUsername,password,false));
            }
        }
    }

    @Test
    public void testQueueingAndSpectating() throws JSONException {
        JSONObject heroHeroes = SingleUserNetworkTest.emitEventAndWaitForResult(heroClient, "get_heroes", new NoInputRequest().convertToJSON(), 10);
        Set<UUID> heroIDs = new HashSet<>();
        JSONArray heroArr = heroHeroes.getJSONArray("heroes");
        for (int i = 0; i < Math.min(2, heroArr.length()); i++) {
            JSONObject hero = heroArr.getJSONObject(i);
            System.out.println(hero);
            heroIDs.add(UUID.fromString(hero.getString(GameObject.GAMEOBJECT_ID_KEY)));
        }
        QueueHeroesRequest heroReq = new QueueHeroesRequest(false, 10, 10, heroIDs);
        JSONObject heroQueueEvent = NetworkTestCore.emitEventAndWaitForResult(heroClient, "queue_up_heroes", heroReq.convertToJSON(), 10);
        assertEquals(200,heroQueueEvent.getInt("status"));
        LinkedBlockingQueue<JSONObject> heroQueueErrorQueue = NetworkTestCore.storeDataForEvents(heroClient,"queue_error");
        LinkedBlockingQueue<JSONObject> archQueueErrorQueue = NetworkTestCore.storeDataForEvents(architectClient,"queue_error");
        LinkedBlockingQueue<JSONObject> heroMatchFoundQueue = NetworkTestCore.storeDataForEvents(heroClient,"match_found");

        QueueArchitectRequest archRequest = new QueueArchitectRequest(false,10,10,null);
        JSONObject architectQueueEvent = NetworkTestCore.emitEventAndWaitForResult(architectClient,"queue_up_architect",archRequest.convertToJSON(),10);
        assertEquals(200,architectQueueEvent.getInt("status"));
        LinkedBlockingQueue<JSONObject> archMatchFoundQueue = NetworkTestCore.storeDataForEvents(architectClient,"match_found");

        JSONObject heroMatchObj = null;
        try {
            heroMatchObj = heroMatchFoundQueue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Interrupted while waiting for the heroes to find a match.");
            return;
        }
        JSONObject architectMatchObj = null;
        try {
            architectMatchObj = archMatchFoundQueue.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Interrupted while waiting for the architect to find a match.");
            return;
        }

        if(heroMatchObj == null || architectMatchObj == null){
            if(heroQueueErrorQueue.peek() != null && archQueueErrorQueue.peek() != null){
                fail("Hero or Architect have received a queue error.");
            } else if (heroQueueErrorQueue.peek() != null){
                fail("Hero has received a queue error.");
            } else if (archQueueErrorQueue.peek() != null){
                fail("Architect has received a queue error.");
            } else {
                fail("Either heroes or architect have not received their match found events.");
            }
            return;
        }
        JSONObject matchObj = heroMatchObj;
        UUID matchID = UUID.fromString(matchObj.getString(Match.MATCH_ID_KEY));

        SpectateBot bot = new SpectateBot(UUID.randomUUID());
        assertTrue(bot.specateMatch(matchID));

        JSONObject heroLeaveResp = NetworkTestCore.emitEventAndWaitForResult(architectClient,"leave_match",new NoInputRequest().convertToJSON(),10);
        assertEquals(200,heroLeaveResp.getInt("status"));
        JSONObject architectLeaveResp = NetworkTestCore.emitEventAndWaitForResult(heroClient, "leave_match",new NoInputRequest().convertToJSON(),10);
        assertEquals(200,architectLeaveResp.getInt("status"));

        assertTrue(bot.getSpectatedEventCount() > 0);
        bot.disconnect();
    }

    private class SpectateBot extends GameBot {

        private static final String BOT_TYPE = "Spectator";

        public SpectateBot(UUID botID) {
            super(BOT_TYPE, botID);
        }

        private int spectatedEventsReceived = 0;

        public synchronized int getSpectatedEventCount(){
            return spectatedEventsReceived;
        }

        public boolean specateMatch(UUID matchID) {
            Response resp = sendEvent("spectate", new SpectateMatchRequest(matchID.toString()).convertToJSON());
            if(resp.getStatus() == WebStatusCode.OK){
                return true;
            } else {
                return false;
            }
        }

        @Override
        public synchronized void receiveEvent(String event, Object data) {
            if (event.equals("player_left")) {
                Response r = sendEvent("match_state", new NoInputRequest().convertToJSON());
                if (r.getStatus() == WebStatusCode.OK) {
                    if (r instanceof InternalResponseObject) {
                        InternalResponseObject<Match> actualResp = (InternalResponseObject<Match>) r;
                        if (actualResp.isNormal()) {
                            Match match = actualResp.get();
                            if (!match.getHeroPlayer().getUsername().equals(getUsername()) && !match.getArchitectPlayer().getUsername().equals(getUsername())) {
                                spectatedEventsReceived++;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onDisconnect() {
            super.onDisconnect();
        }
    }

}
