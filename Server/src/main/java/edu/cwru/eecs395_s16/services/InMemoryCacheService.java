package edu.cwru.eecs395_s16.services;

import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by james on 2/16/16.
 */
public class InMemoryCacheService implements CacheService {

    Map<String,String> stringStorage = new ConcurrentHashMap<>();

    @Override
    public void storeString(String key, String str) {
        stringStorage.put(key,str);
    }

    @Override
    public Optional<String> getString(String key) {
        if(stringStorage.keySet().contains(key)) {
            return Optional.ofNullable(stringStorage.get(key));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void stop() {
        //Do nothing here. This is used to do cleanup if the engine is stopping.
    }
}
