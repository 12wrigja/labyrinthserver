package edu.cwru.eecs395_s16;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import edu.cwru.eecs395_s16.auth.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.auth.InMemorySessionRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.SessionRepository;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.apache.log4j.BasicConfigurator;

import java.net.BindException;
import java.util.*;

/**
 * Created by james on 1/21/16.
 */
public class GameEngine {

    public static final GameEngine instance = new GameEngine();
    public final PlayerRepository userRepo;
    public final SessionRepository sessionRepo;

    private SocketIOServer gameSocket;
    private int serverPort = 4567;

    private GameEngine() {
        this.userRepo = new InMemoryPlayerRepository();
        this.sessionRepo = new InMemorySessionRepository();

        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        int port = 4568;
        if(args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                //Use the default port
            }
        }
        //This kicks off the game engine.
        GameEngine.instance.start(port);
    }

    public void start(int port) {
        this.serverPort = port;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down socket connection.");
            gameSocket.stop();
            System.out.println("Shut down complete.");
        }));

        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(port);
        gameSocket = new SocketIOServer(config);
        NetworkingInterface nF = new NetworkingInterface(gameSocket);
        gameSocket.addConnectListener(client -> {
            System.out.println("Client connected: "+client.getSessionId());
        });
        gameSocket.addDisconnectListener(client -> {
            System.out.println("Client disconnected: "+client.getSessionId());
        });
        gameSocket.start();
        System.out.println("Engine is now running.");

    }

}
