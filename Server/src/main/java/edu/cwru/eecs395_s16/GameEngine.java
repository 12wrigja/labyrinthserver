package edu.cwru.eecs395_s16;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.PlayerRepository;
import edu.cwru.eecs395_s16.core.Interfaces.Repositories.SessionRepository;
import edu.cwru.eecs395_s16.ui.FunctionDescription;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;

/**
 * Created by james on 1/21/16.
 */
public class GameEngine {

    private SocketIOServer gameSocket;
    private int serverPort = 4567;
    private String serverInterface = "0.0.0.0";
    private boolean isStarted = false;
    private NetworkingInterface networkingInterface;

    private Map<String,FunctionDescription> functionDescriptions;

    private UUID instanceID;

    private static InheritableThreadLocal<GameEngine> threadLocalGameEngine = new InheritableThreadLocal<GameEngine>();

    public static GameEngine instance(){
        return threadLocalGameEngine.get();
    }

    public final PlayerRepository playerRepository;
    public final SessionRepository sessionRepository;

    private Timer gameTimer;

    public GameEngine(PlayerRepository pRepo, SessionRepository sRepo){
        this.instanceID = UUID.randomUUID();
        threadLocalGameEngine.set(this);
        this.playerRepository = pRepo;
        this.sessionRepository = sRepo;
        System.out.println("GameEngine created with ID: "+instanceID.toString());
        gameTimer = new Timer();
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
        ServerSocket s = new ServerSocket(serverPort, 1, InetAddress.getByName(serverInterface));
        //Port is available.
        s.close();

        Configuration config = new Configuration();
        config.setHostname(this.serverInterface);
        config.setPort(this.serverPort);
        gameSocket = new SocketIOServer(config);

        //Link all created methods to socket server.
        networkingInterface = new NetworkingInterface();
        linkAllNetworkMethods(gameSocket,networkingInterface);

        gameSocket.addConnectListener(client -> {
            System.out.println("Client connected: "+client.getSessionId());
        });
        gameSocket.addDisconnectListener(client -> {
            System.out.println("Client disconnected: "+client.getSessionId());
        });
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
        gameTimer.scheduleAtFixedRate(pingTask,0,1000);
    }

    public List<FunctionDescription> getAllFunctions(){
        return  new ArrayList<>(functionDescriptions.values());
    }

    public FunctionDescription getFunctionDescription(String humanName){
        return functionDescriptions.get(humanName);
    }

    public void stop(){
        gameTimer.cancel();
        if(this.isStarted){
            System.out.println("Shutting down socket connection on interface " + this.serverInterface + " on port "+this.serverPort);
            gameSocket.stop();
            System.out.println("Shut down complete.");
        }
    }

    private void linkAllNetworkMethods(SocketIOServer server, NetworkingInterface iface){
        functionDescriptions = new HashMap<>();
        //Attach links to server events
        Method[] methods = iface.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(NetworkEvent.class)) {
                String functionSocketEventName = convertMethodNameToEventName(m.getName());
                System.out.println("Registering a network socket method '" + functionSocketEventName + "'");
                Class dataType = m.getParameterTypes()[0];
                NetworkEvent at = m.getAnnotation(NetworkEvent.class);
                server.addEventListener(m.getName(), dataType, iface.createTypecastMiddleware(m, at.mustAuthenticate()));
                FunctionDescription d = new FunctionDescription(functionSocketEventName, m.getName(), at.description(), new String[]{}, at.mustAuthenticate());
                functionDescriptions.put(d.humanName,d);
            }
        }
    }

    private String convertMethodNameToEventName(String methodName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < methodName.length(); i++) {
            char letter = methodName.charAt(i);
            if (letter <= 'Z' && letter >= 'A') {
                sb.append('_');
                sb.append((char) ((int) letter + 32));
            } else {
                sb.append(letter);
            }
        }
        return sb.toString();

    }
}
