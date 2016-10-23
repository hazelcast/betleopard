package com.betleopard.hazelcast;

import com.betleopard.simple.*;
import com.betleopard.domain.Horse;
import java.util.Collection;

/**
 *
 * @author ben
 */
public final class HazelcastHorseFactory extends HazelcastFactory<Horse> {

    private static final HazelcastHorseFactory instance = new HazelcastHorseFactory();

    private HazelcastHorseFactory() {
    }

    public static HazelcastHorseFactory getInstance() {
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
