package edu.cwru.eecs395_s16.test;


import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.services.matchmaking.BasicMatchmakingService;
import edu.cwru.eecs395_s16.services.connections.SocketIOConnectionService;
import edu.cwru.eecs395_s16.services.containers.ServiceContainer;
import edu.cwru.eecs395_s16.utils.CoreDataUtils;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.BindException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.fail;

/**
 * Created by james on 2/12/16.
 */
public abstract class SingleUserNetworkTest extends NetworkTestCore {

    protected Socket socket;

    public void setupSingleClient(){
        Optional<Socket> sock = super.connectSocketIOClient();
        if(sock.isPresent()){
            socket = sock.get();
        }
    }

    @After
    public void disconnectClient(){
        if(socket != null){
            socket.disconnect();
        }
    }

}
