package com.betleopard.simple;

import com.betleopard.domain.Horse;
import java.util.Collection;

/**
 * Extends the simple {@code Horse} factory with lookup-by-name functionality
 * 
 * @author kittylyst
 */
public final class SimpleHorseFactory extends SimpleFactory<Horse> {

    private static final SimpleHorseFactory instance = new SimpleHorseFactory();

    private SimpleHorseFactory() {
    }

    public static SimpleHorseFactory getInstance() {
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
