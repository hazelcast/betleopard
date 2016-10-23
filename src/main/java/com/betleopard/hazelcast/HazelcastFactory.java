package com.betleopard.hazelcast;

import com.betleopard.DomainFactory;
import com.betleopard.LongIndexed;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;

/**
 *
 * @author ben
 * @param <T>
 */
public class HazelcastFactory<T extends LongIndexed> implements DomainFactory<T> {

    private final HazelcastInstance hz = Hazelcast.newHazelcastInstance();

    // Move to a Hazelcast factory using IAtomicLong
    protected IAtomicLong id;
    protected IMap<Long, T> cache;

    /**
     *
     * @param classOfT
     */
    public void init(final Class<T> classOfT) {
        cache = hz.getMap("cache-"+ classOfT);
        id = hz.getAtomicLong("counter-"+ classOfT);
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
