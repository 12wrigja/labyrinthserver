package edu.cwru.eecs395_s16.networking.matchmaking;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.*;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.interfaces.services.MatchmakingService;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by james on 2/15/16.
 */
public class BasicMatchmakingService implements MatchmakingService {

    private Queue<Player> heroesQueue;

    private Queue<Player> architectQueue;

    private Set<Player> queuedPlayers;

    private ReentrantReadWriteLock mutex = new ReentrantReadWriteLock(true);

    private boolean started = false;

    public BasicMatchmakingService(){
        this.heroesQueue = new ArrayDeque<>(100);
        this.architectQueue = new ArrayDeque<>(100);
        queuedPlayers = new HashSet<>();
    }

    @Override
    public InternalResponseObject<Boolean> queueAsHeroes(Player p) {
        mutex.writeLock().lock();
        try {
            if (!queuedPlayers.contains(p)) {
                heroesQueue.add(p);
                queuedPlayers.add(p);
            } else {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.ALREADY_IN_QUEUE);
            }
        } finally {
            mutex.writeLock().unlock();
        }
        return new InternalResponseObject<>(true,"queued");
    }

    @Override
    public InternalResponseObject<Boolean> queueAsArchitect(Player p) {
        mutex.writeLock().lock();
        try {
            if (!queuedPlayers.contains(p)) {
                architectQueue.add(p);
                queuedPlayers.add(p);
            } else {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA,InternalErrorCode.ALREADY_IN_QUEUE);
            }
        } finally {
            mutex.writeLock().unlock();
        }
        return new InternalResponseObject<>(true,"queued");
    }

    @Override
    public InternalResponseObject<Boolean> removeFromQueue(Player p) {
        mutex.writeLock().lock();
        if(queuedPlayers.contains(p)){
            queuedPlayers.remove(p);
            if(heroesQueue.contains(p)){
                heroesQueue.remove(p);
            }
            if(architectQueue.contains(p)){
                architectQueue.remove(p);
            }
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA,InternalErrorCode.NOT_IN_QUEUE);
        }
        mutex.writeLock().unlock();
        return new InternalResponseObject<>(false,"queued");
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
                            Match.InitNewMatch(heroPlayer, architectPlayer, new AlmostBlankMap(10,10));
                        }
                    }
                mutex.readLock().unlock();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    if(GameEngine.instance().IS_DEBUG_MODE){
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t = new Thread(matchmakingThread,"MATCHMAKING_THREAD");
        this.started = true;
        t.start();
    }

    //TODO remove players from queue if they disconnect prematurely

    @Override
    public void stop(){
        this.started = false;
    }

}

