package edu.cwru.eecs395_s16;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;
import edu.cwru.eecs395_s16.interfaces.repositories.HeroRepository;
import edu.cwru.eecs395_s16.interfaces.repositories.PlayerRepository;
import edu.cwru.eecs395_s16.interfaces.repositories.SessionRepository;
import edu.cwru.eecs395_s16.interfaces.services.MatchmakingService;
import edu.cwru.eecs395_s16.ui.FunctionDescription;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
import org.json.JSONObject;

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

    private Map<String,FunctionDescription> functionDescriptions;

    private UUID instanceID;

    private static InheritableThreadLocal<GameEngine> threadLocalGameEngine = new InheritableThreadLocal<GameEngine>();

    public static GameEngine instance(){
        return threadLocalGameEngine.get();
    }

    public final boolean IS_DEBUG_MODE;

    private final PlayerRepository playerRepository;
    private final SessionRepository sessionRepository;
    private final MatchmakingService matchService;
    private final CacheService cacheService;
    private final HeroRepository heroRepository;
    private final NetworkingInterface networkingInterface;
    private Timer gameTimer;

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }

    public SessionRepository getSessionRepository() {
        return sessionRepository;
    }

    public MatchmakingService getMatchService() {
        return matchService;
    }

    public BroadcastOperations getGlobalBroadcastService() {
        return gameSocket.getBroadcastOperations();
    }

    public BroadcastOperations getBroadcastServiceForRoom(String roomName){
        return gameSocket.getRoomOperations(roomName);
    }

    public HeroRepository getHeroRepository(){
        return this.heroRepository;
    }

    public CacheService getCacheService(){
        return this.cacheService;
    }

    public Timer getGameTimer() {
        return gameTimer;
    }

    public NetworkingInterface getNetworkingInterface() {
        return networkingInterface;
    }

    public GameEngine(PlayerRepository pRepo, SessionRepository sRepo, HeroRepository heroRepository, MatchmakingService matchService, CacheService cache){
        this(false, pRepo,sRepo, heroRepository, matchService, cache);
    };


    public GameEngine(boolean debugMode, PlayerRepository pRepo, SessionRepository sRepo, HeroRepository heroRepository, MatchmakingService matchService, CacheService cache){
        this.instanceID = UUID.randomUUID();
        this.IS_DEBUG_MODE = debugMode;
        threadLocalGameEngine.set(this);
        this.playerRepository = pRepo;
        this.sessionRepository = sRepo;
        this.matchService = matchService;
        this.cacheService = cache;
        this.heroRepository = heroRepository;
        this.networkingInterface = new NetworkingInterface();
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
        config.getSocketConfig().setReuseAddress(true);

        JacksonJsonSupport jacksonJsonSupport = new JacksonJsonSupport(new JsonOrgModule());
        config.setJsonSupport(jacksonJsonSupport);
        gameSocket = new SocketIOServer(config);

        //Link all created methods to socket server.
        linkAllNetworkMethods(gameSocket, networkingInterface);

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
        matchService.start();
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

    public String getEngineID(){
        return this.instanceID.toString();
    }

    public void stop(){
        gameTimer.cancel();
        this.cacheService.stop();
        if(this.isStarted){
            System.out.println("Shutting down socket connection on interface " + this.serverInterface + " on port "+this.serverPort);
            matchService.stop();
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
                NetworkEvent at = m.getAnnotation(NetworkEvent.class);
                server.addEventListener(functionSocketEventName, JSONObject.class, iface.createTypecastMiddleware(m, at.mustAuthenticate()));
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
