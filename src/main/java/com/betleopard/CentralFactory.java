package com.betleopard;

import com.betleopard.domain.Horse;
import com.betleopard.domain.Leg;
import com.betleopard.domain.Race;
import com.hazelcast.core.IAtomicLong;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author ben
 */
public class CentralFactory {

    // FIXME
    // Move to a Hazelcast factory using IAtomicLong
    
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

    public static Race raceOf(long raceID) {
        // Look it up in Hazelcast
        return null;
    }

    public static Horse horseOf(long runner) {
        // Look it up in Hazelcast
        return null;
    }
}
