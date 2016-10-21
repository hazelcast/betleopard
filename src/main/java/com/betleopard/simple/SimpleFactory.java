package com.betleopard.simple;

import com.betleopard.DomainFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author ben
 */
public class SimpleFactory<T> implements DomainFactory<T> {

    protected static AtomicLong id = new AtomicLong(1);

    protected final Map<Long, T> cache = new HashMap<>();
    
    @Override
    public T getByID(long ID) {
        return cache.get(ID);
    }

    @Override
    public long getNext() {
        return id.getAndIncrement();
    }

}
