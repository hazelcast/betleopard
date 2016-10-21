package com.betleopard.simple;

import com.betleopard.domain.Horse;
import com.betleopard.domain.Race;
import static com.betleopard.simple.SimpleFactory.id;
import java.util.Collection;

/**
 *
 * @author ben
 */
public class SimpleRaceFactory extends SimpleFactory<Race> {

    private static final SimpleRaceFactory instance = new SimpleRaceFactory();

    private SimpleRaceFactory() {
    }

    public static SimpleFactory<Race> getInstance() {
        return instance;
    }

//    @Override
//    public Race getByName(final String name) {
//        final Collection<Horse> stud = cache.values();
//        final Horse horse = stud.stream()
//                .filter(h -> h.getName().equals(name))
//                .findFirst()
//                .orElse(null);
//        if (horse != null)
//            return horse;
//        final Horse newHorse = new Horse(name, id.getAndIncrement());
//        cache.put(newHorse.getID(), newHorse);
//        return newHorse;
//    }

    @Override
    public boolean cacheIfSupported(Race r) {
        cache.put(r.getID(), r);
        return true;
    }

}
