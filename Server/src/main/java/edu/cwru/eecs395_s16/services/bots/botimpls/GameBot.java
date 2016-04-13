package edu.cwru.eecs395_s16.services.bots.botimpls;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.GameState;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.Hero;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.heroes.HeroType;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.Monster;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterBuilder;
import edu.cwru.eecs395_s16.core.objects.creatures.monsters.MonsterDefinition;
import edu.cwru.eecs395_s16.core.objects.objectives.GameObjective;
import edu.cwru.eecs395_s16.networking.Response;
import edu.cwru.eecs395_s16.core.objects.GameObject;
import edu.cwru.eecs395_s16.services.connections.GameClient;
import edu.cwru.eecs395_s16.services.monsters.MonsterRepository;
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
        super(-1, botTypeName+"_"+botID.toString(), "", false);
        this.botID = botID;
        setClient(Optional.of(this));
        GameEngine.instance().botService.register(this);
        heroes = new ArrayList<>();
        architectObjects = new ArrayList<>();
        populate();
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

    public void onConnect(){};

    public final void disconnect(){
        GameEngine.instance().botService.unregister(this);
    }

    public void onDisconnect(){
        if(getCurrentMatchID().isPresent()){
            Match m =  Match.fromCacheWithMatchIdentifier(getCurrentMatchID().get()).get();
            if(m.getGameState() != GameState.GAME_END) {
                m.end("Bot disconnected.", GameObjective.GAME_WINNER.NO_WINNER);
            }
            setCurrentMatch(Optional.empty());
        }
    };

    public final List<Hero> getBotsHeroes(){
        return heroes;
    }

    public final void replaceBotHeroes(List<Hero> heroes){
        if(!getCurrentMatchID().isPresent()) {
            this.heroes.clear();
            this.heroes.addAll(heroes);
        }
    }

    public final List<GameObject> getArchitectObjects(){
        return architectObjects;
    }

    public final void replaceArchitectObjects(List<GameObject> architectObjects){
        if(!getCurrentMatchID().isPresent()){
            this.architectObjects.clear();
            this.architectObjects.addAll(architectObjects);
        }
    }

    protected void populate() {
        heroes.add(new HeroBuilder(UUID.randomUUID(), getUsername(), Optional.of(getUsername()), -1, HeroType.WARRIOR).createHero());
        MonsterDefinition def = GameEngine.instance().services.monsterRepository.getMonsterDefinitionForId(1).get();
        architectObjects.add(new MonsterBuilder(UUID.randomUUID(), def, getUsername(), Optional.of(getUsername())).createMonster());
    }
}
