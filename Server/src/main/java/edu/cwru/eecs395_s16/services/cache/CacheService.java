package edu.cwru.eecs395_s16.services.cache;

import java.util.Optional;

/**
 * Created by james on 2/16/16.
 */
public interface CacheService {

    void storeString(String key,String str);
    Optional<String> getString(String key);
    void removeString(String key);
    void stop();
}
