package com.betleopard.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ben
 */
public class TestUtils {

    public static final double EPSILON = 0.00001;

    private static final List<Horse> runners = new ArrayList<>();

    public static void setupHorses() {
        final Horse h0 = Horse.of("Beach Boy", 1);
        final Horse h1 = Horse.of("Rolling Stone", 2);
        final Horse h2 = Horse.of("Speedwell", 3);
        runners.add(h0);
        runners.add(h1);
        runners.add(h2);
    }

    public static Map<Horse, Double> makeSimple0_95UnderBook() {
        final Map<Horse, Double> odds = new HashMap<>();
        odds.put(runners.get(0), 2.0);
        odds.put(runners.get(1), 4.0);
        odds.put(runners.get(2), 5.0);
        return odds;
    }

    public static Map<Horse, Double> makeSimpleOverBook() {
        final Map<Horse, Double> odds = new HashMap<>();
        odds.put(runners.get(0), 3.0);
        odds.put(runners.get(1), 1.5);
        odds.put(runners.get(2), 8.0);
        return odds;
    }

    public static Race makeSimpleRace() {
        final Map<Horse, Double> odds = TestUtils.makeSimple0_95UnderBook();
        final LocalDate raceDay = LocalDate.now().plusDays(1);
        final Race out = Race.of(raceDay.atTime(15, 20), 1, odds);

        return out;
    }
}
