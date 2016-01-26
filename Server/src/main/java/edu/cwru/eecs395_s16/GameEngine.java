package edu.cwru.eecs395_s16;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import edu.cwru.eecs395_s16.auth.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.auth.InMemorySessionRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.SessionRepository;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;

/**
 * Created by james on 1/21/16.
 */
public class GameEngine {

    public static final GameEngine instance = new GameEngine();
    public final PlayerRepository userRepo;
    public final SessionRepository sessionRepo;

    private SocketIOServer gameSocket;

    private GameEngine() {
        this.userRepo = new InMemoryPlayerRepository();
        this.sessionRepo = new InMemorySessionRepository();
    }

    public static void main(String[] args) {
        //This kicks off the game engine.
        GameEngine.instance.start();
    }

    public void start() {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setContext("/StrategyGame");
        config.setPort(4567);
        gameSocket = new SocketIOServer(config);
        NetworkingInterface nF = new NetworkingInterface(gameSocket);
        gameSocket.addConnectListener(client -> {
            System.out.println("Connection established.");
            System.out.println(client.getSessionId());
        });
        gameSocket.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down socket connection.");
            gameSocket.stop();
            System.out.println("Shut down complete.");
        }));
    }

}
