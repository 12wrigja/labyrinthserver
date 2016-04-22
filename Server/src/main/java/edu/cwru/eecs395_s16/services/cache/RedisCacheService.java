package edu.cwru.eecs395_s16.services.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;

/**
 * Created by james on 2/16/16.
 */
public class RedisCacheService implements CacheService {

    final JedisPool pool;

    public RedisCacheService(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public void storeString(String key, String str) {
        try (Jedis jedis = pool.getResource()) {
            System.out.println("Redis Cache: Storing key " + key);
            jedis.set(key, str);
        }
    }

    @Override
    public Optional<String> getString(String key) {
        try (Jedis jedis = pool.getResource()) {
            System.out.println("Redis Cache: Retrieving key " + key);
            if (jedis.exists(key)) {
                return Optional.of(jedis.get(key));
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public void removeString(String key) {
        try (Jedis jedis = pool.getResource()) {
            System.out.println("Redis Cache: Removing key " + key);
            jedis.del(key);
        }
    }

    @Override
    public void stop() {
        pool.destroy();
    }
}
