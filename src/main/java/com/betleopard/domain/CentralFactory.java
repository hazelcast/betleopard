package com.betleopard.domain;

import com.betleopard.DomainFactory;

/**
 *
 * @author ben
 */
public final class CentralFactory {

    private static DomainFactory<Horse> horseFactory;
    private static DomainFactory<Race> raceFactory;

    public static Horse horseOf(final String name) {
        Horse out = horseFactory.getByName(name);
        if (out == null) {
            out = new Horse(name, horseFactory.getNext());
        }
        horseFactory.cacheIfSupported(out);

        return out;
    }

    public static void setHorses(final DomainFactory<Horse> inject) {
        horseFactory = inject;
    }

    public static Horse horseOf(long runnerID) {
        return horseFactory.getByID(runnerID);
    }

    public static Race raceOf(long raceID) {
        return raceFactory.getByID(raceID);
    }

    public static void setRaces(final DomainFactory<Race> inject) {
        raceFactory = inject;
    }

    public static DomainFactory<Horse> getHorseFactory() {
        return horseFactory;
    }

    public static DomainFactory<Race> getRaceFactory() {
        return raceFactory;
    }
    
}
