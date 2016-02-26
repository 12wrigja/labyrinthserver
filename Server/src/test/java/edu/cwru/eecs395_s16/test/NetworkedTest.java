package edu.cwru.eecs395_s16.test;


import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.services.InMemoryCacheService;
import edu.cwru.eecs395_s16.services.InMemoryHeroRepository;
import edu.cwru.eecs395_s16.services.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.services.InMemorySessionRepository;
import edu.cwru.eecs395_s16.networking.matchmaking.BasicMatchmakingService;
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
 * Created by james on 2/12/16.
 */
public abstract class NetworkedTest {
    protected static Socket socket;
    protected static GameEngine engine;
    protected static final int MAX_TRY_COUNT = 5;
    protected static final int PORT = 4500;

    @BeforeClass
    public static void setUpGameEngine() throws Exception {
        System.out.println("Setting up game engine.");
        engine = new GameEngine(false, new InMemoryPlayerRepository(), new InMemorySessionRepository(), new InMemoryHeroRepository(), new BasicMatchmakingService(), new InMemoryCacheService());
        SocketIOConnectionService socketIO = new SocketIOConnectionService();
        socketIO.setServerPort(PORT);
        engine.addClientService(socketIO);
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
        socket = IO.socket("http://localhost:"+PORT);
        socket.connect();
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

    @AfterClass
    public static void tearDownGameEngine() throws Exception {
        if(socket != null) {
            System.out.println("Tearing down game engine.");
            socket.disconnect();
        }
        if(engine != null) {
            engine.stop();
        }
    }


    public final JSONObject emitEventAndWaitForResult(String event, JSONObject data){
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
        try {
            flag.await(10, TimeUnit.SECONDS);
        }catch(InterruptedException e){
            fail("Ran out of time on request.");
        }
        lock.unlock();
        if(response[0] == null){
            fail("No response was given for command "+event+" with data: "+data.toString());
        }
        return response[0];
    };

}
