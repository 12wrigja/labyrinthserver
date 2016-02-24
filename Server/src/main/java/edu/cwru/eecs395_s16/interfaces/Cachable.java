package edu.cwru.eecs395_s16.interfaces;

import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;

/**
 * Created by james on 2/16/16.
 */
public interface Cachable {

    public void storeInCache(CacheService cache);

    public void fillFromCache(CacheService cache);

}
