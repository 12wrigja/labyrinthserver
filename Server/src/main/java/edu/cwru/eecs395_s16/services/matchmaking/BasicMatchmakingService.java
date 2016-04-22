package edu.cwru.eecs395_s16.services.matchmaking;

import edu.cwru.eecs395_s16.GameEngine;
import edu.cwru.eecs395_s16.core.InternalErrorCode;
import edu.cwru.eecs395_s16.core.InternalResponseObject;
import edu.cwru.eecs395_s16.core.Match;
import edu.cwru.eecs395_s16.core.Player;
import edu.cwru.eecs395_s16.core.objects.maps.GameMap;
import edu.cwru.eecs395_s16.core.objects.objectives.GameObjective;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueArchitectRequest;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueHeroesRequest;
import edu.cwru.eecs395_s16.networking.requests.queueing.QueueRequest;
import edu.cwru.eecs395_s16.networking.responses.WebStatusCode;
import edu.cwru.eecs395_s16.services.connections.GameClient;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by james on 2/15/16.
 */
public class BasicMatchmakingService implements MatchmakingService {

    private Set<Player> queuedPlayers;

    private ReentrantReadWriteLock mutex = new ReentrantReadWriteLock(true);

    private boolean started = false;

    private ConcurrentHashMap<Integer, MatchPool> matchmakingPools;

    public BasicMatchmakingService() {
        matchmakingPools = new ConcurrentHashMap<>();
        queuedPlayers = new HashSet<>();
    }

    private static int generateHash(GameMap map, GameObjective objective) {
        return (map.getDatabaseID() + "," + objective.getJSONRepresentation().optString(GameObjective.OBJECTIVE_TYPE_KEY, "deathmatch")).hashCode();
    }

    @Override
    public InternalResponseObject<Boolean> queueAsHeroes(Player p, QueueHeroesRequest request, GameMap map, GameObjective objective) {
        mutex.writeLock().lock();
        try {
            if (!queuedPlayers.contains(p)) {
                InternalResponseObject<Boolean> queueResp = queueUp(p, request, map, objective, true);
                if (!queueResp.isNormal()) {
                    return InternalResponseObject.cloneError(queueResp);
                }
                queuedPlayers.add(p);
            } else {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.ALREADY_IN_QUEUE);
            }
        } finally {
            mutex.writeLock().unlock();
        }
        return new InternalResponseObject<>(true, "queued");
    }

    @Override
    public InternalResponseObject<Boolean> queueAsArchitect(Player p, QueueArchitectRequest request, GameMap map, GameObjective objective) {
        mutex.writeLock().lock();
        try {
            if (!queuedPlayers.contains(p)) {
                InternalResponseObject<Boolean> queueResp = queueUp(p, request, map, objective, false);
                if (!queueResp.isNormal()) {
                    return InternalResponseObject.cloneError(queueResp);
                }
                queuedPlayers.add(p);
            } else {
                return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.ALREADY_IN_QUEUE);
            }
        } finally {
            mutex.writeLock().unlock();
        }
        return new InternalResponseObject<>(true, "queued");
    }

    @Override
    public InternalResponseObject<Boolean> removeFromQueue(Player p) {
        mutex.writeLock().lock();
        if (queuedPlayers.contains(p)) {
            queuedPlayers.remove(p);
            QueueObject obj = new QueueObject(p, null);
            for (MatchPool pool : matchmakingPools.values()) {
                pool.removeQueueRequest(obj);
            }
        } else {
            return new InternalResponseObject<>(WebStatusCode.UNPROCESSABLE_DATA, InternalErrorCode.NOT_IN_QUEUE);
        }
        mutex.writeLock().unlock();
        return new InternalResponseObject<>(false, "queued");
    }

    @Override
    public void start() {
        this.started = true;
    }

    @Override
    public void stop() {
        this.started = false;
    }

    private class QueueObject {
        public final Player p;
        final QueueRequest request;

        QueueObject(Player p, QueueRequest request) {
            this.p = p;
            this.request = request;
        }

        @Override
        public int hashCode() {
            return p.hashCode();
        }        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            QueueObject that = (QueueObject) o;
            return p.equals(that.p);
        }


    }

    private class MatchPool {

        private final Runnable matchingThread;
        private final Thread otherThread;
        private final int hashCode;
        private Queue<QueueObject> heroesQueue;
        private Queue<QueueObject> architectQueue;
        private ReentrantReadWriteLock _mutex = new ReentrantReadWriteLock(true);

        MatchPool(GameMap map, GameObjective objective) {
            this.hashCode = generateHash(map, objective);
            heroesQueue = new ArrayDeque<>(10);
            architectQueue = new ArrayDeque<>(10);
            matchingThread = () -> {
                while (started) {
                    _mutex.readLock().lock();
                    if (architectQueue.peek() != null) {
                        if (heroesQueue.peek() != null) {
                            //Deque Players and make match
                            QueueObject heroQueueObj = heroesQueue.poll();
                            QueueObject architectQueueObj = architectQueue.poll();
                            Player heroPlayer = heroQueueObj.p;
                            QueueHeroesRequest heroReq = (QueueHeroesRequest) heroQueueObj.request;
                            Player architectPlayer = architectQueueObj.p;
                            queuedPlayers.remove(heroPlayer);
                            queuedPlayers.remove(architectPlayer);
                            QueueArchitectRequest archReq = (QueueArchitectRequest) architectQueueObj.request;
                            InternalResponseObject<Match> match = Match.InitNewMatch(heroPlayer, architectPlayer, map, objective, heroReq.getSelectedHeroesIds(), archReq.getMonsterLocationMap());
                            Optional<GameClient> heroClient = heroPlayer.getClient();
                            Optional<GameClient> architectClient = architectPlayer.getClient();
                            if (!match.isNormal()) {
                                if (match.getInternalErrorCode() == InternalErrorCode.INCORRECT_INITIAL_HERO_SETUP) {
                                    architectQueue.add(architectQueueObj);
                                    queuedPlayers.add(architectPlayer);
                                    if (heroClient.isPresent()) {
                                        heroClient.get().receiveEvent("queue_error", match.getJSONRepresentation());
                                    }
                                } else if (match.getInternalErrorCode() == InternalErrorCode.INCORRECT_INITIAL_ARCHITECT_SETUP) {
                                    heroesQueue.add(heroQueueObj);
                                    queuedPlayers.add(heroPlayer);
                                    if (architectClient.isPresent()) {
                                        architectClient.get().receiveEvent("queue_error", match.getJSONRepresentation());
                                    }
                                } else {
                                    JSONObject errorObj = match.getJSONRepresentation();
                                    if (heroClient.isPresent()) {
                                        heroClient.get().receiveEvent("queue_error", errorObj);
                                    }
                                    if (architectClient.isPresent()) {
                                        architectClient.get().receiveEvent("queue_error", errorObj);
                                    }
                                }
                            }
                        }
                    }
                    _mutex.readLock().unlock();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        if (GameEngine.instance().IS_DEBUG_MODE) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            otherThread = new Thread(matchingThread);
            otherThread.start();
        }


        void addPlayer(Player p, QueueRequest request, boolean heroes) {
            mutex.writeLock().lock();
            QueueObject obj = new QueueObject(p, request);
            if (heroes) {
                heroesQueue.add(obj);
            } else {
                architectQueue.add(obj);
            }
            mutex.writeLock().unlock();
        }

        void removeQueueRequest(QueueObject p) {
            mutex.writeLock().lock();
            if (heroesQueue.contains(p)) {
                heroesQueue.remove(p);
            }
            if (architectQueue.contains(p)) {
                architectQueue.remove(p);
            }
            mutex.writeLock().unlock();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MatchPool matchPool = (MatchPool) o;

            return hashCode == matchPool.hashCode;

        }

        @Override
        public int hashCode() {
            return hashCode;
        }

    }

    private InternalResponseObject<Boolean> queueUp(Player p, QueueRequest request, GameMap map, GameObjective objective, boolean queueHeroes) {
        int poolHashCode = generateHash(map, objective);
        MatchPool pool;
        if (matchmakingPools.containsKey(poolHashCode)) {
            pool = matchmakingPools.get(poolHashCode);
        } else {
            pool = new MatchPool(map, objective);
            matchmakingPools.put(poolHashCode, pool);
        }
        pool.addPlayer(p, request, queueHeroes);
        return new InternalResponseObject<>(true, "queued");
    }
}

