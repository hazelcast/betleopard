package com.betleopard.hazelcast;

import com.betleopard.DomainFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author ben
 */
public class HazelcastFactory<T> implements DomainFactory<T> {
   
    // Move to a Hazelcast factory using IAtomicLong
    private static AtomicLong horseID = new AtomicLong(1);
    private static AtomicLong raceID = new AtomicLong(1);
    
    public T getByID(long ID) {
        return null;
    }

    @Override
    public long getNext() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
