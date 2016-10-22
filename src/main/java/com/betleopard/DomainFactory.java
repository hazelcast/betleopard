/*
 * 
 * 
 * 
 */
package com.betleopard;

/**
 *
 * @author ben
 * @param <T>
 */
public interface DomainFactory<T> {
    public T getByID(long ID);
    
    public default T getByName(String name) {
        return null;
    }

    public default boolean cacheIfSupported(T t) {
        return false;
    }
    
    public default void init() {
    }
    
    public long getNext();
}
