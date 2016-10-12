/*
 * 
 * 
 * 
 */
package com.betleopard.domain;

import com.sun.org.apache.xalan.internal.lib.ExsltDatetime;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ben
 */
public final class Race {

    private final long id;
    private final Map<Horse, Double> odds;
    private final LocalDateTime raceTime;
    private final long version;

    private Race(RaceBuilder rb) {
        id = rb.id;
        odds = rb.odds;
        raceTime = rb.raceTime;
        version = ++(rb.version);
    }

    public static class RaceBuilder implements Builder<Race> {

        private long id = 0;
        private Map<Horse, Double> odds = new HashMap<>();
        private LocalDateTime raceTime = LocalDateTime.MIN;
        private long version = 0;

        @Override
        public Race build() {
            return new Race(this);
        }

        public RaceBuilder id(final long id) {
            this.id = id;
            return this;
        }

        public RaceBuilder odds(final Map<Horse, Double> odds) {
            this.odds = odds;
            return this;
        }

        public RaceBuilder raceTime(final LocalDateTime time) {
            raceTime = time;
            return this;
        }

        public RaceBuilder(final Race r) {
            id = r.id;
            odds = r.odds;
            raceTime = r.raceTime;
            version = r.version;
        }

        public RaceBuilder() {
        }
    }

    public double getOdds(Horse horse) {
        if (odds.containsKey(horse)) {
            return odds.get(horse);
        }
        throw new IllegalArgumentException("Horse " + horse + " not running in " + this);
    }

//    public double getOdds(int horseID) {
//
//    }
    public Set<Horse> getRunners() {
        return odds.keySet();
    }

    static Race of(final LocalDateTime time, final long id, final Map<Horse, Double> odds) {
        final RaceBuilder rb = new RaceBuilder();
        return rb.raceTime(time).id(id).odds(odds).build();
    }

}
