package edu.cwru.eecs395_s16.test.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.test.EngineOnlyTest;
import org.json.JSONObject;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

/**
 * Created by james on 2/26/16.
 */
public class testBotRooms extends EngineOnlyTest {

    @Test
    public void testBotRooms() {
        final boolean[] complete = {false};
        final boolean[] worked = {false};
        ReentrantLock l = new ReentrantLock();
        Condition c = l.newCondition();
        Thread testThread = Thread.currentThread();
        GameBot b = new GameBot("TESTBOT", UUID.randomUUID()) {

            @Override
            public void receiveEvent(String event, Object data) {
                if (event.equals("test_event")) {
                    l.lock();
                    assertNotEquals(Thread.currentThread(), testThread);
                    if (!complete[0]) {
                        c.signal();
                    }
                    complete[0] = true;
                    worked[0] = true;
                    l.unlock();
                }
            }

            @Override
            public void onConnect() {

            }

            @Override
            public void onDisconnect() {

            }

            @Override
            protected void populate() {

            }
        };
        b.joinRoom("test_room");
        while (!complete[0]) {
            l.lock();
            GameEngine.instance().broadcastEventForRoom("test_room", "test_event", new JSONObject());
            try {
                c.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail("Interrupted.");
            }
            l.unlock();
            assertTrue(worked[0]);
        }
    }

    @Test
    public void testBotLeaveRooms() {
        final boolean[] complete = {false};
        final boolean[] worked = {false};
        ReentrantLock l = new ReentrantLock();
        Condition c = l.newCondition();
        Thread testThread = Thread.currentThread();
        GameBot b = new GameBot("TESTBOT", UUID.randomUUID()) {

            @Override
            public void receiveEvent(String event, Object data) {
                if (event.equals("test_event")) {
                    l.lock();
                    assertNotEquals(Thread.currentThread(), testThread);
                    if (!complete[0]) {
                        c.signal();
                    }
                    complete[0] = !complete[0];
                    worked[0] = !worked[0];
                    l.unlock();
                }
            }
        };
        b.joinRoom("test_room");
        while (!complete[0]) {
            l.lock();
            GameEngine.instance().broadcastEventForRoom("test_room", "test_event", new JSONObject());
            try {
                c.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail("Interrupted.");
            }
            l.unlock();
            assertTrue(worked[0]);
            b.leaveRoom("test_room");
            l.lock();
            GameEngine.instance().broadcastEventForRoom("test_room", "test_event", new JSONObject());
            try {
                c.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                fail("Interrupted.");
            }
            l.unlock();
            assertTrue(worked[0]);
        }
    }
}
