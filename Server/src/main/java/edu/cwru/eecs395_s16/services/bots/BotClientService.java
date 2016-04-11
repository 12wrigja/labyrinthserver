package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.AuthenticationMiddleware;
import edu.cwru.eecs395_s16.services.bots.botimpls.GameBot;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.networking.Response;
import edu.cwru.eecs395_s16.services.bots.botimpls.PassBot;
import edu.cwru.eecs395_s16.services.bots.botimpls.TestBot;
import edu.cwru.eecs395_s16.services.connections.ClientConnectionService;
import edu.cwru.eecs395_s16.services.connections.GameClient;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.ui.FunctionDescription;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by james on 2/25/16.
 */
public class BotClientService implements ClientConnectionService {

    Map<UUID,GameBot> connectedClients;

    Map<String, Set<GameBot>> roomMap;

    Map<String, AuthenticationMiddleware> fds;

    ExecutorService executorService;

    @Override
    public void start() {
        initStorage();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void stop() {
        connectedClients.values().forEach(GameBot::onDisconnect);
        executorService.shutdownNow();
        initStorage();
    }

    private void initStorage() {
        connectedClients = new HashMap<>();
        roomMap = new HashMap<>();
    }

    @Override
    public void linkToGameEngine(GameEngine g) {
        fds = new HashMap<>();
        for (FunctionDescription fd : g.getAllFunctions()) {
            fds.put(fd.name, fd.invocationPoint);
        }
    }

    public final void register(GameBot c) {
        this.connectedClients.put(c.getSessionId(),c);
        c.onConnect();
    }

    public final void unregister(GameBot c) {
        this.connectedClients.remove(c.getSessionId());
        c.onDisconnect();
    }

    public final void addClientToRoom(String roomName, GameBot c) {
        Set<GameBot> botsInRoom = roomMap.get(roomName);
        if (botsInRoom == null) {
            botsInRoom = new HashSet<>();
            roomMap.put(roomName, botsInRoom);
        }
        if (!botsInRoom.contains(c)) {
            botsInRoom.add(c);
        }
    }

    public final void removeClientFromRoom(String roomName, GameBot c) {
        if (roomMap.containsKey(roomName)) {
            Set<GameBot> botsInRoom = roomMap.get(roomName);
            if (botsInRoom.contains(c)) {
                botsInRoom.remove(c);
            }
        }
    }

    @Override
    public void broadcastEventForRoom(String roomName, String eventName, Object data) {
        Set<GameBot> botsInRoom = roomMap.get(roomName);
        if (botsInRoom != null) {
            for (GameBot bot : botsInRoom) {
                executorService.execute(() -> {
                    bot.receiveEvent(eventName, data);
                });
            }
        }
    }

    @Override
    public InternalResponseObject<GameClient> findClientFromUUID(UUID clientID) {
        if(connectedClients.containsKey(clientID)){
            return new InternalResponseObject<>(connectedClients.get(clientID));
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.UNKNOWN_SESSION_IDENTIFIER);
        }
    }

    public Response submitEventForClient(GameBot client, String eventName, JSONObject data) {
        AuthenticationMiddleware fd = fds.get(eventName);
        if (fd != null) {
            return fd.onEvent(client, data);
        } else {
            return new Response(WebStatusCode.UNPROCESSABLE_DATA);
        }
    }

    public Optional<GameBot> botForUsername(String username) {
        List<GameBot> matches = connectedClients.values().stream().filter(gameBot -> gameBot.getUsername().equals(username)).collect(Collectors.toList());
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        } else {
            if(username.contains("_")){
                //This is where we would rebuild bots that we need.
                return Optional.of(new GameBot(username.split("_")[0],UUID.fromString(username.split("_")[1])){
                    @Override
                    public void receiveEvent(String event, Object data) {
                        //Do nothing.
                    }
                });
            } else {
                return Optional.empty();
            }
        }
    }

    public Optional<GameBot> botForSessionID(UUID sessionID) {
        List<GameBot> matches = connectedClients.values().stream().filter(gameBot -> gameBot.getSessionId().equals(sessionID)).collect(Collectors.toList());
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        } else {
            return Optional.empty();
        }
    }
}
