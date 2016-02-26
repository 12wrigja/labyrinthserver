package edu.cwru.eecs395_s16.test;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.networking.matchmaking.BasicMatchmakingService;
import edu.cwru.eecs395_s16.services.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.InMemoryHeroRepository;
import edu.cwru.eecs395_s16.services.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.InMemorySessionRepository;
import edu.cwru.eecs395_s16.services.connections.SocketIOConnectionService;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.BindException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.fail;

/**
 * Created by james on 2/26/16.
 */
public abstract class EngineOnlyTest {

    protected static GameEngine engine;
    private static final int MAX_TRY_COUNT = 5;

    @BeforeClass
    public static void setUpGameEngine() throws Exception {
        System.out.println("Setting up game engine.");
        engine = new GameEngine(false, new InMemoryPlayerRepository(), new InMemorySessionRepository(), new InMemoryHeroRepository(), new BasicMatchmakingService(), new InMemoryCacheService());
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
                Thread.sleep(30000);
            }
        }
    }

    @AfterClass
    public static void tearDownGameEngine() throws Exception {
        if(engine != null) {
            engine.stop();
        }
    }

}
