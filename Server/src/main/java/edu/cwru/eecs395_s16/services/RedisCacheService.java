package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;

/**
 * Created by james on 2/16/16.
 */
public class RedisCacheService implements CacheService {

    final JedisPool pool;

    public RedisCacheService(){
        this.pool = new JedisPool(new JedisPoolConfig(),"localhost");
    }

    @Override
    public void storeString(String key, String str) {
        try (Jedis jedis = pool.getResource()){
            System.out.println("Redis Cache: Storing key "+ key);
            jedis.set(key,str);
        }
    }

    @Override
    public Optional<String> getString(String key) {
        try (Jedis jedis = pool.getResource()){
            System.out.println("Redis Cache: Retrieving key "+ key);
            if(jedis.exists(key)){
                return Optional.of(jedis.get(key));
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public void removeString(String key) {
        try(Jedis jedis = pool.getResource()){
            System.out.println("Redis Cache: Removing key "+ key);
            jedis.del(key);
        }
    }

    @Override
    public void stop() {
        pool.destroy();
    }
}