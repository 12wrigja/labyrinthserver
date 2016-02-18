package edu.cwru.eecs395_s16.interfaces;

import edu.cwru.eecs395_s16.interfaces.repositories.CacheService;

/**
 * Created by james on 2/16/16.
 */
public interface Cachable<T> {

    public void storeInCache(CacheService cache);

    public T fromCache(CacheService cache);

}
