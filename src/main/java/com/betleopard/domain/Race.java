package com.betleopard.domain;

import java.time.LocalDateTime;
import java.util.*;

/**
 *
 * @author ben
 */
public final class Race {

    private final long id;
    private final List<RaceBody> versions = new ArrayList<>();
    private boolean hasRun = false;

    private Race(long raceID) {
        id = raceID;
    }

    /**
     * 
     * @param time
     * @param id
     * @param odds
     * @return 
     */
    public static Race of(final LocalDateTime time, final long id, final Map<Horse, Double> odds) {
        final Race out = new Race(id);

        final RaceBodyBuilder rbb = new RaceBodyBuilder(out);
        rbb.raceTime = time;
        rbb.odds = odds;
        out.versions.add(rbb.build());

        return out;
    }

    public Horse findRunnerByID(int horseID) {
        final RaceBody current = versions.get(version());
        return current.findRunner(horseID);
    }

    public int version() {
        return versions.size() - 1;
    }

    public double currentOdds(final Horse backing) {
        final RaceBody current = versions.get(version());
        return current.getOdds(backing);
    }

    private RaceBody getCurrentBody() {
        if (versions.isEmpty()) {
            return new RaceBody(id);
        }
        return versions.get(version());
    }

    public void winner(final Horse fptp) {
        final RaceBodyBuilder rbb = new RaceBodyBuilder(this);
        rbb.winner = fptp;
        final RaceBody finish = rbb.build();
        versions.add(finish);
        hasRun = true;
    }

    public Optional<Horse> getWinner() {
        if (!hasRun) {
            return Optional.empty();
        }
        return getCurrentBody().winner;
    }

    public void newOdds(final Map<Horse, Double> book) {
        final RaceBodyBuilder rbb = new RaceBodyBuilder(this);
        rbb.odds = book;
        final RaceBody nextVer = rbb.build();
        versions.add(nextVer);
    }

    /**
     *
     * @author ben
     */
    public static final class RaceBody {

        private final long id;
        private final Map<Horse, Double> odds; // Decimal "Betfair" style odds
        private final LocalDateTime raceTime;
        private final long version;
        private final Optional<Horse> winner;

        private RaceBody(RaceBodyBuilder rb) {
            super();
            id = rb.id;
            odds = rb.odds;
            raceTime = rb.raceTime;
            version = rb.version++;
            winner = Optional.ofNullable(rb.winner);
        }

        private RaceBody(long ID) {
            super();
            id = ID;
            odds = new HashMap<>();
            raceTime = LocalDateTime.MIN;
            version = 0;
            winner = Optional.empty();
        }

        public double getOdds(Horse horse) {
            if (odds.containsKey(horse)) {
                return odds.get(horse);
            }
            throw new IllegalArgumentException("Horse " + horse + " not running in " + this);
        }

        public Horse findRunner(final long id) {
            return odds.keySet().stream().filter((Horse h) -> h.getID() == id).findFirst().orElse(null);
        }

        //    public double getOdds(int horseID) {
        //
        //    }
        public Set<Horse> getRunners() {
            return odds.keySet();
        }
    }

    public static class RaceBodyBuilder implements Builder<RaceBody> {

        private long id = 1;
        private Map<Horse, Double> odds = new HashMap<>();
        private LocalDateTime raceTime = LocalDateTime.MIN;
        private long version = 1;
        private Horse winner = null;

        @Override
        public RaceBody build() {
            return new RaceBody(this);
        }

        public RaceBodyBuilder odds(final Map<Horse, Double> odds) {
            this.odds = odds;
            return this;
        }

        public RaceBodyBuilder raceTime(final LocalDateTime time) {
            raceTime = time;
            return this;
        }

        public RaceBodyBuilder winner(final Horse fptp) {
            winner = fptp;
            return this;
        }

        public RaceBodyBuilder(final Race race) {
            super();
            final RaceBody rb = race.getCurrentBody();
            id = rb.id;
            odds = rb.odds;
            raceTime = rb.raceTime;
            version = rb.version;
            winner = rb.winner.orElse(null);
        }

        public RaceBodyBuilder() {
            super();
        }

        private void incVersion() {
            version++;
        }
    }
}
