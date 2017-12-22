package com.betleopard.hazelcast;

import com.betleopard.domain.Horse;
import com.hazelcast.core.HazelcastInstance;
import java.util.Collection;

/**
 * A concrete {@code Horse} factory that demonstrates extending the factory
 * with the optional "lookup by name" functionality
 * 
 * @author kittylyst
 */
public final class HazelcastHorseFactory extends HazelcastFactory<Horse> {

    private static HazelcastHorseFactory instance;

    private HazelcastHorseFactory(final HazelcastInstance instance) {
        super(instance, Horse.class);
    }

    public static synchronized HazelcastHorseFactory getInstance(HazelcastInstance client) {
        if (instance == null) {
            instance = new HazelcastHorseFactory(client);
        }
        return instance;
    }
    
    @Override
    public Horse getByName(final String name) {
        final Collection<Horse> stud = cache.values();
                
        final Horse horse = stud.stream()
                                .filter(h -> h.getName().equals(name))
                                .findFirst()
                                .orElse(null);
        if (horse != null)
            return horse;
        final Horse newHorse = new Horse(name, id.getAndIncrement());
        cache.put(newHorse.getID(), newHorse);
        return newHorse;
    }
}
