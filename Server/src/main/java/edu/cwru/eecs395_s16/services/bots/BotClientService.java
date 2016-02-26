package edu.cwru.eecs395_s16.services.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.auth.AuthenticationMiddleware;
import edu.cwru.eecs395_s16.bots.GameBot;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.services.ClientConnectionService;
import edu.cwru.eecs395_s16.networking.responses.StatusCode;
import edu.cwru.eecs395_s16.ui.FunctionDescription;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by james on 2/25/16.
 */
public class BotClientService implements ClientConnectionService {

    Set<GameBot> connectedClients;

    Map<String, Set<GameBot>> roomMap;

    Map<String, AuthenticationMiddleware> fds;

    @Override
    public void start() throws IOException {
        initStorage();
    }

    @Override
    public void stop() {
        connectedClients.forEach(GameBot::disconnectBot);
        initStorage();
    }

    private void initStorage(){
        connectedClients = new HashSet<>();
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
        this.connectedClients.add(c);
        c.onConnect();
    }

    public final void unregister(GameBot c) {
        this.connectedClients.remove(c);
    }

    public final void addClientToRoom(String roomName, GameBot c) {
        Set<GameBot> botsInRoom = roomMap.get(roomName);
        if(botsInRoom == null){
            botsInRoom = new HashSet<>();
            roomMap.put(roomName,botsInRoom);
        }
        if(!botsInRoom.contains(c)) {
            botsInRoom.add(c);
        }
    }

    public final void removeClientFromRoom(String roomName, GameBot c) {
        if(roomMap.containsKey(roomName)) {
            Set<GameBot> botsInRoom = roomMap.get(roomName);
            if(botsInRoom.contains(c)){
                botsInRoom.remove(c);
            }
        }
    }

    @Override
    public void broadcastEventForRoom(String roomName, String eventName, Object data) {
        Set<GameBot> botsInRoom = roomMap.get(roomName);
        if (botsInRoom != null) {
            for (GameBot bot : botsInRoom) {
                bot.receiveEvent(eventName, data);
            }
        }
    }

    public Response submitEventForClient(GameBot client, String eventName, JSONObject data) {
        AuthenticationMiddleware fd = fds.get(eventName);
        if (fd != null) {
            return fd.onEvent(client, data);
        } else {
            return new Response(StatusCode.UNPROCESSABLE_DATA);
        }
    }

    public Optional<GameBot> botForUsername(String username) {
        List<GameBot> matches = connectedClients.stream().filter(gameBot -> gameBot.getUsername().equals(username)).collect(Collectors.toList());
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        } else {
            return Optional.empty();
        }
    }

    public Optional<GameBot> botForSessionID(UUID sessionID) {
        List<GameBot> matches = connectedClients.stream().filter(gameBot -> gameBot.getSessionId().equals(sessionID)).collect(Collectors.toList());
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        } else {
            return Optional.empty();
        }
    }
}
