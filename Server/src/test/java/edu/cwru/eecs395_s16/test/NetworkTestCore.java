package edu.cwru.eecs395_s16.test;


import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.services.connections.SocketIOConnectionService;
import edu.cwru.eecs395_s16.services.containers.ServiceContainer;
import edu.cwru.eecs395_s16.services.matchmaking.BasicMatchmakingService;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.BindException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.fail;

/**
 * Created by james on 2/12/16.
 */
public abstract class NetworkTestCore {

    protected static final int MAX_TRY_COUNT = 5;
    protected static final int PORT = 4500;
    protected static GameEngine engine;

    @BeforeClass
    public static void setUpGameEngine() throws Exception {
        System.out.println("Setting up game engine.");
        engine = new GameEngine(false, ServiceContainer.buildInMemoryContainer(CoreDataUtils.defaultCoreData(), new
                BasicMatchmakingService()));
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
                if (try_count > MAX_TRY_COUNT) {
                    fail("Unable to setup game server.");
                }
                Thread.sleep(30000);
            }
        }
    }

    @AfterClass
    public static void tearDownGameEngine() throws Exception {
        if (engine != null) {
            engine.stop();
        }
    }

    public static JSONObject emitEventAndWaitForResult(Socket socket, String event, JSONObject data, long
            waitTimeSeconds) {
        final JSONObject[] response = {null};
        final Lock lock = new ReentrantLock();
        final Condition flag = lock.newCondition();
        socket.emit(event, data, (Ack) args -> {
            lock.lock();
            if (args.length > 0) {
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
            flag.await(waitTimeSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Ran out of time on request.");
        }
        lock.unlock();
        if (response[0] == null) {
            fail("No response was given for command " + event + " with data: " + data.toString());
        }
        return response[0];
    }

    public static LinkedBlockingQueue<JSONObject> storeDataForEvents(Socket socket, String event) {
        LinkedBlockingQueue<JSONObject> resultQueue = new LinkedBlockingQueue<>();
        socket.on(event, args -> {
            JSONObject data = (JSONObject) args[0];
            resultQueue.add(data);
        });
        return resultQueue;
    }

    public Optional<Socket> connectSocketIOClient() {
        Socket socket;
        IO.Options opt = new IO.Options();
        opt.forceNew = true;
        try {
            socket = IO.socket("http://localhost:" + PORT, opt);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return Optional.empty();
        }
        socket.connect();
        int try_count = 0;
        while (true) {
            if (socket.connected()) {
                break;
            } else {
                try_count++;
                if (try_count > MAX_TRY_COUNT) {
                    return Optional.empty();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return Optional.empty();
                }
            }
        }
        return Optional.of(socket);
    }

}
