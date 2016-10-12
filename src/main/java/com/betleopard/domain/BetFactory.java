package com.betleopard.domain;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author ben
 */
public class BetFactory {

    private static AtomicLong horseID = new AtomicLong(1);
    private static AtomicLong raceID = new AtomicLong(1);

    public static Leg newLeg() {
        return null;
    }

    public static Horse newHorse(final String name) {
        return Horse.of(name, horseID.getAndIncrement());
    }

    public static Race newRace(final LocalDateTime time, final Map<Horse, Double> earlyPrices) {
        return Race.of(time, raceID.getAndIncrement(), earlyPrices);
    }
}
