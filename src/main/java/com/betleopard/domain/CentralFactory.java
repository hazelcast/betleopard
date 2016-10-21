package com.betleopard.domain;

import com.betleopard.DomainFactory;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author ben
 */
public final class CentralFactory {

    private static DomainFactory<Horse> horses;
    private static DomainFactory<Race> races;

    public static Horse horseOf(final String name) {
        Horse out = horses.getByName(name);
        if (out == null) {
            out = new Horse(name, horses.getNext());
        }
        horses.cacheIfSupported(out);

        return out;
    }

    public static void setHorses(final DomainFactory<Horse> inject) {
        horses = inject;
    }

    public static Horse horseOf(long runnerID) {
        return horses.getByID(runnerID);
    }

//    public static Race raceOf(final LocalDateTime time, final Map<Horse, Double> earlyPrices) {
//        return Race.of(time, raceID.getAndIncrement(), earlyPrices);
//    }

    public static Race raceOf(long raceID) {
        return races.getByID(raceID);
    }

    public static void setRaces(final DomainFactory<Race> inject) {
        races = inject;
    }

}
