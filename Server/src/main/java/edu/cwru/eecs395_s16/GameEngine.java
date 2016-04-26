package edu.cwru.eecs395_s16;

import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.auth.AuthenticationMiddleware;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.bots.BotClientService;
import edu.cwru.eecs395_s16.services.connections.ClientConnectionService;
import edu.cwru.eecs395_s16.services.connections.GameClient;
import edu.cwru.eecs395_s16.services.containers.ServiceContainer;
import edu.cwru.eecs395_s16.ui.FunctionDescription;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The Game Engine is the heart of the server - it helps coordinate services, connections, and functionality. It also
 * houses global functions that provide functionality across client types, such as broadcasting events to all
 * connected clients in a room, regardless of how they are actually connected to the server.
 * <p>
 * Head on down to start for an explanation of the meat of this class.
 */

/**
 * Created by james on 1/21/16.
 */
public class GameEngine {

    public static final InheritableThreadLocal<GameEngine> threadLocalGameEngine = new InheritableThreadLocal<>();
    public final boolean IS_DEBUG_MODE;
    public final NetworkingInterface networkingInterface;
    public final ServiceContainer services;
    public final Timer gameTimer;
    public final BotClientService botService;
    private boolean isStarted = false;
    private Map<String, FunctionDescription> functionDescriptions;
    private UUID instanceID;
    private List<ClientConnectionService> clientConnectionServices;

    public GameEngine(ServiceContainer container) {
        this(false, container);
    }

    public GameEngine(boolean debugMode, ServiceContainer serviceContainer) {
        //Set an instance identifier for the engine - might be potentially useful for identification when doing
        // vertical scaling.
        this.instanceID = UUID.randomUUID();
        //Assign this instance of the game engine to all the threads that this game engine generates. Now, whenever
        // GameEngine.instance() is called that thread will receive the right instance of the engine it should be using.
        threadLocalGameEngine.set(this);

        //Set the service container and various other services.
        this.services = serviceContainer;
        this.IS_DEBUG_MODE = debugMode;
        this.gameTimer = new Timer();
        this.networkingInterface = new NetworkingInterface();
        this.clientConnectionServices = new ArrayList<>();

        //Add Bot support. To the rest of the server (minus the service wrappers), bots look and act like regular
        // players.
        this.botService = new BotClientService();
        clientConnectionServices.add(botService);
        System.out.println("GameEngine created with ID: " + instanceID.toString());
    }

    public static GameEngine instance() {
        return threadLocalGameEngine.get();
    }

    public void broadcastEventForRoom(String roomName, String eventName, Object data) {
        for (ClientConnectionService service : clientConnectionServices) {
            service.broadcastEventForRoom(roomName, eventName, data);
        }
    }

    public InternalResponseObject<GameClient> findClientFromUUID(UUID clientID) {
        for (ClientConnectionService service : clientConnectionServices) {
            InternalResponseObject<GameClient> resp = service.findClientFromUUID(clientID);
            if (resp.isNormal() && resp.isPresent()) {
                return resp;
            }
        }
        return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode
                .UNKNOWN_SESSION_IDENTIFIER);
    }

    public void addClientService(ClientConnectionService service) {
        this.clientConnectionServices.add(service);
    }

    public void start() throws IOException {

        /**
         * In start, the GameEngine scans the NetworkingInterface file for all the methods that it knows how to work
         * with, and connects them to the various client services it supports. As we need to read the functions
         * available in the NetworkingInterface file at runtime, we use reflection to accomplish this. In order to
         * add some compile-time protective wrappers on top of that we use Annotations in order to
         * a.) identify what methods are supposed to be server methods (as opposed to helper methods) and
         * b.) ensure those methods meet our runtime expectations to eliminate the possibility of a crash related to
         * reflection.
         *
         * Next up is the NetworkingInterface class.
         */

        //Read all methods from Networking Interface, create a description of them (containing the actual method name
        // to invoke, the event name to listen to to trigger that function, the description of that function, and
        // whether or not it needs the client to be authorized to invoke it) and connects those methods to all the
        // connected ClientConnectionServices.
        functionDescriptions = new HashMap<>();
        //Attach links to server events
        Method[] methods = this.networkingInterface.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(NetworkEvent.class)) {
                String functionSocketEventName = convertMethodNameToEventName(m.getName());
                System.out.println("Registering a game event method '" + functionSocketEventName + "'");
                NetworkEvent at = m.getAnnotation(NetworkEvent.class);
                AuthenticationMiddleware md = new AuthenticationMiddleware(this.networkingInterface, this.services
                        .sessionRepository, m, at.mustAuthenticate());
                FunctionDescription d = new FunctionDescription(functionSocketEventName, m.getName(), at.description
                        (), new String[]{}, at.mustAuthenticate(), md);
                functionDescriptions.put(d.humanName, d);
            }
        }

        //Start up all services.
        botService.start();
        services.matchService.start();
        for (ClientConnectionService service : clientConnectionServices) {
            service.linkToGameEngine(this);
            service.start();
        }
        TimerTask pingTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (isStarted) {
                        broadcastEventForRoom("TestRoom", "room_ping", "Ping at time: " + System.currentTimeMillis());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        System.out.println("Game Engine is now running.");
        this.isStarted = true;
        this.gameTimer.scheduleAtFixedRate(pingTask, 0, 1000);
    }

    public List<FunctionDescription> getAllFunctions() {
        return new ArrayList<>(functionDescriptions.values());
    }

    public FunctionDescription getFunctionDescription(String humanName) {
        return functionDescriptions.get(humanName);
    }

    public String getEngineID() {
        return this.instanceID.toString();
    }

    public void stop() {
        if (this.isStarted) {
            System.out.println("Shutting down game engine");
            clientConnectionServices.forEach(ClientConnectionService::stop);
            services.matchService.stop();
        }
        this.gameTimer.cancel();
        services.cacheService.stop();
        System.out.println("Shut down complete.");
    }

    public boolean isStarted() {
        return isStarted;
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
