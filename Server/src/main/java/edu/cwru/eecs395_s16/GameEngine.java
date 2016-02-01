package edu.cwru.eecs395_s16;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import edu.cwru.eecs395_s16.auth.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.auth.InMemorySessionRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.SessionRepository;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by james on 1/21/16.
 */
public class GameEngine {

    public static final GameEngine instance = new GameEngine();
    public final PlayerRepository userRepo;
    public final SessionRepository sessionRepo;

    private SocketIOServer gameSocket;
    private int serverPort = 4567;
    private boolean isStarted = false;

    private GameEngine() {
        this.userRepo = new InMemoryPlayerRepository();
        this.sessionRepo = new InMemorySessionRepository();

//        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
    }

    public static void main(String[] args) {
//        BasicConfigurator.configure();

        if(args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                GameEngine.instance.setServerPort(port);
            } catch (NumberFormatException e) {
                //Use the default port as defined in the class
            }
        }
        GameEngine.instance.start();
    }

    public void start(){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down socket connection.");
            gameSocket.stop();
            System.out.println("Shut down complete.");
        }));

        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(this.serverPort);
        gameSocket = new SocketIOServer(config);
        NetworkingInterface nF = new NetworkingInterface(gameSocket);
        gameSocket.addConnectListener(client -> {
            System.out.println("Client connected: "+client.getSessionId());
        });
        gameSocket.addDisconnectListener(client -> {
            System.out.println("Client disconnected: "+client.getSessionId());
        });
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(isStarted){
                    gameSocket.getRoomOperations("TestRoom").sendEvent("room_ping","Ping at time: "+System.currentTimeMillis());
                }
            }
        },0,1000);
        gameSocket.start();
        System.out.println("Engine is now running.");
        this.isStarted = true;

    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
