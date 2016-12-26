package com.betleopard.simple;

import com.betleopard.DomainFactory;
import com.betleopard.LongIndexed;
import com.betleopard.domain.Horse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author kittylyst
 * @param <T>
 */
public class SimpleFactory<T extends LongIndexed> implements DomainFactory<T> {

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

    @Override
    public boolean cacheIfSupported(T t) {
        cache.put(t.getID(), t);
        return true;
    }

}
