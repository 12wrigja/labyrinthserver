package edu.cwru.eecs395_s16.bots;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.interfaces.Response;
import edu.cwru.eecs395_s16.interfaces.objects.GameObject;
import edu.cwru.eecs395_s16.interfaces.services.GameClient;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by james on 2/25/16.
 */
public abstract class GameBot extends Player implements GameClient {

    final UUID botID;
    final List<Hero> heroes;
    final List<GameObject> architectObjects;

    public GameBot(String botTypeName, UUID botID) {
        super(-1, botTypeName+"_"+botID.toString(), "");
        this.botID = botID;
        setClient(Optional.of(this));
        GameEngine.instance().botService.register(this);
        heroes = new ArrayList<>();
        architectObjects = new ArrayList<>();
    }

    @Override
    public final Response sendEvent(String event, JSONObject data) {
        return GameEngine.instance().botService.submitEventForClient(this,event,data);
    }

    @Override
    public abstract void receiveEvent(String event, Object data);

    @Override
    public UUID getSessionId() {
        return botID;
    }

    @Override
    public final void joinRoom(String roomName) {
        GameEngine.instance().botService.addClientToRoom(roomName,this);
    }

    @Override
    public void leaveRoom(String roomName) {
        GameEngine.instance().botService.removeClientFromRoom(roomName,this);
    }

    public abstract void onConnect();

    public final void disconnect(){
        GameEngine.instance().botService.unregister(this);
    }

    public abstract void onDisconnect();

    public final List<Hero> getBotsHeroes(){
        return heroes;
    }

    public final List<GameObject> getArchitectObjects(){
        return architectObjects;
    }
}
