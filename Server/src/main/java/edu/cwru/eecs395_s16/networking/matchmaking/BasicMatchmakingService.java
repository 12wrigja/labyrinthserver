package edu.cwru.eecs395_s16.networking.matchmaking;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.RandomlyGeneratedGameMap;
import edu.cwru.eecs395_s16.interfaces.services.MatchmakingService;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by james on 2/15/16.
 */
public class BasicMatchmakingService implements MatchmakingService {

    private Queue<Player> heroesQueue;
    private Queue<Player> architectQueue;

    private Set<Player> queuedPlayers;

    private ReentrantReadWriteLock mutex = new ReentrantReadWriteLock(true);

    private static final String MATCH_FOUND_EVENT_ID = "match_found";

    private boolean started = false;

    public BasicMatchmakingService(){
        this.heroesQueue = new ArrayDeque<>(100);
        this.architectQueue = new ArrayDeque<Player>(100);
        queuedPlayers = new HashSet<>();
    }

    @Override
    public boolean queueAsHeroes(Player p){
        boolean isQueued = false;
        mutex.writeLock().lock();
        if(!queuedPlayers.contains(p)){
            heroesQueue.add(p);
            queuedPlayers.add(p);
            isQueued = true;
        }
        mutex.writeLock().unlock();
        return isQueued;
    }

    @Override
    public boolean queueAsArchitect(Player p){
        boolean isQueued = false;
        mutex.writeLock().lock();
        if(!queuedPlayers.contains(p)){
            architectQueue.add(p);
            queuedPlayers.add(p);
            isQueued = true;
        }
        mutex.writeLock().unlock();
        return isQueued;
    }

    @Override
    public boolean removeFromQueue(Player p) {
        boolean isDequeued = false;
        mutex.writeLock().lock();
        if(queuedPlayers.contains(p)){
            queuedPlayers.remove(p);
            if(heroesQueue.contains(p)){
                heroesQueue.remove(p);
            }
            if(architectQueue.contains(p)){
                architectQueue.remove(p);
            }
            isDequeued = true;
        } else {
            isDequeued = false;
        }
        mutex.writeLock().unlock();
        return isDequeued;
    }

    @Override
    public void start(){
        Runnable matchmakingThread = () -> {
            while(started) {
                mutex.readLock().lock();
                    if(architectQueue.peek() != null){
                        if(heroesQueue.peek() != null){
                            //Deque Players and make match
                            Player heroPlayer = heroesQueue.poll();
                            Player architectPlayer = architectQueue.poll();
                            createMatch(heroPlayer,architectPlayer);
                        }
                    }
                mutex.readLock().unlock();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
            }
        };
        Thread t = new Thread(matchmakingThread,"MATCHMAKING_THREAD");
        this.started = true;
        t.start();
    }

    @Override
    public void stop(){
        this.started = false;
    }

    private void createMatch(Player heroPlayer, Player architectPlayer){
        //TODO setup the match with the correct parameters
        Match m = Match.InitNewMatch(heroPlayer, architectPlayer, new RandomlyGeneratedGameMap(4,4));
        //TODO store player-match link somehow

    }
}

