package edu.cwru.eecs395_s16.test.persistance;

import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;
import edu.cwru.eecs395_s16.services.cache.RedisCacheService;
import org.junit.Test;
import redis.clients.jedis.JedisPool;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by james on 2/17/16.
 */
public class RedisConfigTest {

    @Test
    public void testRedisCacheStoreString(){
        String key = "this key thing!";
        String value = "the value!";
        CacheService cache = new RedisCacheService(new JedisPool("localhost"));
        cache.storeString(key,value);
        Optional<String> returnedVal = cache.getString(key);
        if(returnedVal.isPresent()){
            assertEquals(value,returnedVal.get());
        } else {
            fail("Value could not be returned from redis cache.");
        }
    }

}
