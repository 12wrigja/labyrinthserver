package edu.cwru.eecs395_s16;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import edu.cwru.eecs395_s16.auth.InMemoryPlayerRepository;
import edu.cwru.eecs395_s16.auth.InMemorySessionRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.SessionRepository;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by james on 1/21/16.
 */
public class GameEngine {

    public final PlayerRepository userRepo;
    public final SessionRepository sessionRepo;

    private SocketIOServer gameSocket;
    private int serverPort = 4567;
    private String serverInterface = "0.0.0.0";
    private boolean isStarted = false;

    private UUID instanceID;

    private static InheritableThreadLocal<GameEngine> threadLocalGameEngine = new InheritableThreadLocal<GameEngine>(){
        @Override
        public GameEngine get() {
            GameEngine temp = super.get();
            System.out.println("Getting thread local instance with id: "+temp.instanceID);
            return temp;
        }
    };

    public static GameEngine instance(){
        return threadLocalGameEngine.get();
    }

    public GameEngine(PlayerRepository pRepo, SessionRepository sRepo){
        this.userRepo = pRepo;
        this.sessionRepo = sRepo;
        this.instanceID = UUID.randomUUID();
    }

    public void setServerInterface(String serverInterface){
        if(!this.isStarted) {
            this.serverInterface = serverInterface;
        }
    }

    public void setServerPort(int port){
        if(!this.isStarted) {
            this.serverPort = port;
        }
    }

    public int getServerPort(){
        return this.serverPort;
    }

    public void start() throws IOException {
        //Check here for port availability
        Socket s = new Socket(serverInterface,serverPort);
        //Port is available.
        s.close();

        Configuration config = new Configuration();
        config.setHostname(this.serverInterface);
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
        TimerTask pingTask = new TimerTask() {
            @Override
            public void run() {
                if(isStarted){
                    gameSocket.getRoomOperations("TestRoom").sendEvent("room_ping","Ping at time: "+System.currentTimeMillis());
                }
            }
        };

        gameSocket.start();
        System.out.println("Engine is now running, bound to interface "+this.serverInterface+" on port "+this.serverPort);
        this.isStarted = true;
        t.scheduleAtFixedRate(pingTask,0,1000);
    }

    public void stop(){
        if(this.isStarted){
            System.out.println("Shutting down socket connection on interface " + this.serverInterface + " on port "+this.serverPort);
            gameSocket.stop();
            System.out.println("Shut down complete.");
        }
    }
}
