package edu.cwru.eecs395_s16;

import edu.cwru.eecs395_s16.annotations.NetworkEvent;
import edu.cwru.eecs395_s16.auth.AuthenticationMiddleware;
import edu.cwru.eecs395_s16.interfaces.repositories.*;
import edu.cwru.eecs395_s16.interfaces.services.ClientConnectionService;
import edu.cwru.eecs395_s16.interfaces.services.MatchmakingService;
import edu.cwru.eecs395_s16.networking.NetworkingInterface;
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

    private static InheritableThreadLocal<GameEngine> threadLocalGameEngine = new InheritableThreadLocal<>();

    public static GameEngine instance() {
        return threadLocalGameEngine.get();
    }

    public final boolean IS_DEBUG_MODE;

    private final PlayerRepository playerRepository;
    private final SessionRepository sessionRepository;
    private final MatchmakingService matchService;
    private final CacheService cacheService;
    private final HeroRepository heroRepository;
    private final NetworkingInterface networkingInterface;
    private final MapRepository mapRepository;
    private BotClientService botService;
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

    public void broadcastEventForRoom(String roomName, String eventName, Object data) {
        for (ClientConnectionService service : clientConnectionServices) {
            service.broadcastEventForRoom(roomName, eventName, data);
        }
    }

    public HeroRepository getHeroRepository() {
        return this.heroRepository;
    }

    public CacheService getCacheService() {
        return this.cacheService;
    }

    public Timer getGameTimer() {
        return gameTimer;
    }

    public NetworkingInterface getNetworkingInterface() {
        return networkingInterface;
    }

    public BotClientService getBotService() {
        return botService;
    }

    public MapRepository getMapRepository() {
        return mapRepository;
    }

    public GameEngine(PlayerRepository pRepo, SessionRepository sRepo, HeroRepository heroRepository, MatchmakingService matchService, CacheService cache, MapRepository mapRepository) {
        this(false, pRepo, sRepo, heroRepository, matchService, cache, mapRepository);
    }

    private List<ClientConnectionService> clientConnectionServices;

    public GameEngine(boolean debugMode, PlayerRepository pRepo, SessionRepository sRepo, HeroRepository heroRepository, MatchmakingService matchService, CacheService cache, MapRepository mapRepository) {
        this.mapRepository = mapRepository;
        this.instanceID = UUID.randomUUID();
        this.IS_DEBUG_MODE = debugMode;
        threadLocalGameEngine.set(this);
        this.playerRepository = new PlayerRepositoryBotWrapper(pRepo);
        this.sessionRepository = new SessionRepositoryBotWrapper(sRepo);
        this.matchService = matchService;
        this.cacheService = cache;
        this.heroRepository = heroRepository;
        this.networkingInterface = new NetworkingInterface();
        this.clientConnectionServices = new ArrayList<>();
        this.botService = new BotClientService();
        clientConnectionServices.add(this.botService);
        System.out.println("GameEngine created with ID: " + instanceID.toString());
        gameTimer = new Timer();
    }

    public void addClientService(ClientConnectionService service) {
        this.clientConnectionServices.add(service);
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
                AuthenticationMiddleware md = new AuthenticationMiddleware(this.networkingInterface, this.sessionRepository, m, at.mustAuthenticate());
                FunctionDescription d = new FunctionDescription(functionSocketEventName, m.getName(), at.description(), new String[]{}, at.mustAuthenticate(), md);
                functionDescriptions.put(d.humanName, d);
            }
        }
        botService.start();
        matchService.start();
        for (ClientConnectionService service : clientConnectionServices) {
            service.linkToGameEngine(this);
            service.start();
        }
        TimerTask pingTask = new TimerTask() {
            @Override
            public void run() {
                if (isStarted) {
                    broadcastEventForRoom("TestRoom", "room_ping", "Ping at time: " + System.currentTimeMillis());
                }
            }
        };

        System.out.println("Game Engine is now running.");
        this.isStarted = true;
        gameTimer.scheduleAtFixedRate(pingTask, 0, 1000);
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
            matchService.stop();
        }
        gameTimer.cancel();
        this.cacheService.stop();
        System.out.println("Shut down complete.");
    }

    public boolean isStarted() {
        return isStarted;
    }
}
