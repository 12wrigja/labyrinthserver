package edu.cwru.eecs395_s16.services.cache;

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
        System.out.println("InMemory Cache: Storing for key: "+key);
        stringStorage.put(key,str);
    }

    @Override
    public Optional<String> getString(String key) {
        System.out.println("InMemory Cache: Getting for key: "+key);
        if(stringStorage.keySet().contains(key)) {
            return Optional.ofNullable(stringStorage.get(key));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void removeString(String key) {
        System.out.println("InMemory Cache: Deleting key: "+key);
        stringStorage.remove(key);
    }

    @Override
    public void stop() {
        //Do nothing here. This is used to do cleanup if the engine is stopping.
    }
}
