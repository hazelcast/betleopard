package com.betleopard.hazelcast;

import com.betleopard.DomainFactory;
import com.betleopard.LongIndexed;
import com.hazelcast.core.IAtomicLong;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

/**
 *
 * @author ben
 * @param <T>
 */
public final class HazelcastFactory<T extends LongIndexed> implements DomainFactory<T> {

    // Move to a Hazelcast factory using IAtomicLong
    protected static IAtomicLong id;
    protected Cache<Long, T> cache;

    /**
     *
     * @param cacheName
     * @param classOfT
     */
    public void init(final String cacheName, final Class<T> classOfT) {
        final CachingProvider cachingProvider = Caching.getCachingProvider();
        final CacheManager cacheManager = cachingProvider.getCacheManager();

        // Configuration for the cache, including type information
        CompleteConfiguration<Long, T> config
                = new MutableConfiguration<Long, T>()
                .setTypes(Long.class, classOfT);

        // Create the cache, and get a reference to it
        cacheManager.createCache(cacheName, config);
        cache = cacheManager.getCache(cacheName, Long.class, classOfT);
    }

    @Override
    public T getByID(long ID) {
        return cache.get(ID);
    }

    @Override
    public boolean cacheIfSupported(T t) {
        cache.put(t.getID(), t);
        return true;
    }

    @Override
    public long getNext() {
        return id.getAndIncrement();
    }

}
