package edu.cwru.eecs395_s16.core;

import edu.cwru.eecs395_s16.GameEngine;
import io.netty.util.internal.ConcurrentSet;

import java.util.*;

/**
 * Created by james on 1/19/16.
 */
public class Match {

    private final TimerTask pingTask;

    private Player heroPlayer;
    private Player dmPlayer;

    private Set<Player> spectators;

    private UUID matchIdentifier;

    private Map gameMap;

    private GameState _state;

    public Match(Player heroPlayer, Player dmPlayer, UUID matchIdentifier) {
        this.heroPlayer = heroPlayer;
        this.dmPlayer = dmPlayer;
        this.matchIdentifier = matchIdentifier;

        pingTask = new TimerTask() {
            @Override
            public void run() {
                GameEngine.instance().getBroadcastServiceForRoom(matchIdentifier.toString()).sendEvent("room_ping","You are in room "+matchIdentifier.toString());
            }
        };

        this.heroPlayer.getClient().joinRoom(matchIdentifier.toString());
        this.dmPlayer.getClient().joinRoom(matchIdentifier.toString());
        GameEngine.instance().getGameTimer().scheduleAtFixedRate(pingTask,0,1000);
        _state = GameState.GAME_START;

        spectators = new HashSet<>(5);
    }

    //TODO figure out what inputs go here
    //Maybe some sort of action class?
    public synchronized void updateGameState(){

    }

    public synchronized void addSpectator(Player spectator){
        if(!this.spectators.contains(spectator)){
            this.spectators.add(spectator);
        }
    }

    public synchronized void removeSpectator(Player spectator){
        this.spectators.remove(spectator);
    }

    public void cleanup(){
        pingTask.cancel();
    }

}
