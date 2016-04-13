package edu.cwru.eecs395_s16.services.matchmaking;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.*;
import edu.cwru.eecs395_s16.core.objects.maps.AlmostBlankMap;
import edu.cwru.eecs395_s16.core.objects.objectives.DeathmatchGameObjective;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueArchitectRequest;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueHeroesRequest;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueRequest;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by james on 2/15/16.
 */
public class BasicMatchmakingService implements MatchmakingService {

    private Queue<QueueObject> heroesQueue;

    private Queue<QueueObject> architectQueue;

    private Set<Player> queuedPlayers;

    private ReentrantReadWriteLock mutex = new ReentrantReadWriteLock(true);

    private boolean started = false;

    public BasicMatchmakingService(){
        this.heroesQueue = new ArrayDeque<>(100);
        this.architectQueue = new ArrayDeque<>(100);
        queuedPlayers = new HashSet<>();
    }

    @Override
    public InternalResponseObject<Boolean> queueAsHeroes(Player p, QueueHeroesRequest request) {
        mutex.writeLock().lock();
        try {
            if (!queuedPlayers.contains(p)) {
                heroesQueue.add(new QueueObject(p,request));
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
    public InternalResponseObject<Boolean> queueAsArchitect(Player p, QueueArchitectRequest request) {
        mutex.writeLock().lock();
        try {
            if (!queuedPlayers.contains(p)) {
                architectQueue.add(new QueueObject(p,request));
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
            QueueObject obj = new QueueObject(p,null);
            if(heroesQueue.contains(obj)){
                heroesQueue.remove(obj);
            }
            if(architectQueue.contains(obj)){
                architectQueue.remove(obj);
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
                            QueueObject heroQueueObj = heroesQueue.poll();
                            QueueObject architectQueueObj = architectQueue.poll();
                            Player heroPlayer = heroQueueObj.p;
                            QueueHeroesRequest heroReq = (QueueHeroesRequest) heroQueueObj.request;
                            Player architectPlayer = architectQueueObj.p;
                            queuedPlayers.remove(heroPlayer);
                            queuedPlayers.remove(architectPlayer);
                            QueueArchitectRequest archReq = (QueueArchitectRequest) architectQueueObj.request;
                            InternalResponseObject<Match> match = Match.InitNewMatch(heroPlayer, architectPlayer, new AlmostBlankMap(10,10), new DeathmatchGameObjective(), heroReq.getSelectedHeroesIds(), archReq.getMonsterLocationMap());
                            if(!match.isNormal()){
                                if(match.getInternalErrorCode() == InternalErrorCode.INCORRECT_INITIAL_HERO_SETUP){
                                    architectQueue.add(architectQueueObj);
                                    queuedPlayers.add(architectPlayer);
                                    if(heroPlayer.getClient().isPresent()){
                                        heroPlayer.getClient().get().sendEvent("queue_error",match.getJSONRepresentation());
                                    }
                                } else if (match.getInternalErrorCode() == InternalErrorCode.INCORRECT_INITIAL_ARCHITECT_SETUP){
                                    heroesQueue.add(heroQueueObj);
                                    queuedPlayers.add(heroPlayer);
                                    if(architectPlayer.getClient().isPresent()){
                                        architectPlayer.getClient().get().sendEvent("queue_error",match.getJSONRepresentation());
                                    }
                                } else {
                                    JSONObject errorObj = match.getJSONRepresentation();
                                    if(heroPlayer.getClient().isPresent()){
                                        heroPlayer.getClient().get().sendEvent("queue_error",errorObj);
                                    }
                                    if(architectPlayer.getClient().isPresent()){
                                        architectPlayer.getClient().get().sendEvent("queue_error",errorObj);
                                    }
                                }
                            }
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

    private class QueueObject {
        public final Player p;
        public final QueueRequest request;

        public QueueObject(Player p, QueueRequest request) {
            this.p = p;
            this.request = request;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QueueObject that = (QueueObject) o;

            return p.equals(that.p);

        }

        @Override
        public int hashCode() {
            return p.hashCode();
        }
    }

}

