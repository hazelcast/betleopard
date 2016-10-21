/*
 * 
 * 
 * 
 */
package com.betleopard;

/**
 *
 * @author ben
 */
public interface DomainFactory<T> {
    public T getByID(long ID);
    
    public default T getByName(String name) {
        return null;
    }

    public default boolean cacheIfSupported(T t) {
        return false;
    }
    
    public long getNext();
}
