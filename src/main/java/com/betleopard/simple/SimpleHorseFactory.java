package com.betleopard.simple;

import com.betleopard.domain.Horse;
import java.util.Collection;

/**
 *
 * @author ben
 */
public final class SimpleHorseFactory extends SimpleFactory<Horse> {

    private static final SimpleHorseFactory instance = new SimpleHorseFactory();

    private SimpleHorseFactory() {
    }

    public static SimpleFactory<Horse> getInstance() {
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
