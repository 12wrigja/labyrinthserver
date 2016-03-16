package edu.cwru.eecs395_s16;

import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.auth.AuthenticationMiddleware;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.interfaces.repositories.*;
import edu.cwru.eecs395_s16.interfaces.services.ClientConnectionService;
import edu.cwru.eecs395_s16.interfaces.services.GameClient;
import edu.cwru.eecs395_s16.interfaces.services.MatchmakingService;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.ServiceContainer;
import edu.cwru.eecs395_s16.services.ServiceContainerBuilder;
import edu.cwru.eecs395_s16.services.bots.BotClientService;
import edu.cwru.eecs395_s16.services.bots.PlayerRepositoryBotWrapper;
import edu.cwru.eecs395_s16.services.bots.SessionRepositoryBotWrapper;
import edu.cwru.eecs395_s16.ui.FunctionDescription;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by james on 1/21/16.
 */
public class GameEngine {

    private boolean isStarted = false;

    private Map<String, FunctionDescription> functionDescriptions;

    private UUID instanceID;

    public static final InheritableThreadLocal<GameEngine> threadLocalGameEngine = new InheritableThreadLocal<>();

    public static GameEngine instance() {
        return threadLocalGameEngine.get();
    }

    public final boolean IS_DEBUG_MODE;

    public final NetworkingInterface networkingInterface;
    public final ServiceContainer services;
    public final Timer gameTimer;
    public final BotClientService botService;

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
        return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.UNKNOWN_SESSION_IDENTIFIER);
    }

    public GameEngine(ServiceContainer container) {
        this(false, container);
    }

    private List<ClientConnectionService> clientConnectionServices;

    public GameEngine(boolean debugMode, ServiceContainer serviceContainer) {
        this.instanceID = UUID.randomUUID();
        threadLocalGameEngine.set(this);
        this.services = serviceContainer;
        this.IS_DEBUG_MODE = debugMode;
        this.gameTimer = new Timer();
        this.networkingInterface = new NetworkingInterface();
        this.clientConnectionServices = new ArrayList<>();
        this.botService = new BotClientService();
        clientConnectionServices.add(botService);
        System.out.println("GameEngine created with ID: " + instanceID.toString());
    }

    public void addClientService(ClientConnectionService service) {
        if(!this.isStarted) {
            this.clientConnectionServices.add(service);
        }
    }

    public void start() throws IOException {
        functionDescriptions = new HashMap<>();
        //Attach links to server events
        Method[] methods = this.networkingInterface.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(NetworkEvent.class)) {
                String functionSocketEventName = convertMethodNameToEventName(m.getName());
                System.out.println("Registering a game event method '" + functionSocketEventName + "'");
                NetworkEvent at = m.getAnnotation(NetworkEvent.class);
                AuthenticationMiddleware md = new AuthenticationMiddleware(this.networkingInterface, this.services.sessionRepository, m, at.mustAuthenticate());
                FunctionDescription d = new FunctionDescription(functionSocketEventName, m.getName(), at.description(), new String[]{}, at.mustAuthenticate(), md);
                functionDescriptions.put(d.humanName, d);
            }
        }
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
}
