package com.betleopard.domain;

import com.betleopard.DomainFactory;
import com.betleopard.JSONSerializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author ben
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Race implements JSONSerializable {

    private final long id;
    private final List<RaceDetails> versions = new ArrayList<>();
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
        final RaceDetails current = versions.get(version());
        return current.findRunner(horseID);
    }

    public int version() {
        return versions.size() - 1;
    }

    public double currentOdds(final Horse backing) {
        final RaceDetails current = versions.get(version());
        return current.getOdds(backing);
    }

    @JsonProperty
    public RaceDetails getCurrentVersion() {
        if (versions.isEmpty()) {
            return new RaceDetails(id);
        }
        return versions.get(version());
    }

    public void winner(final Horse fptp) {
        final RaceBodyBuilder rbb = new RaceBodyBuilder(this);
        rbb.winner = fptp;
        final RaceDetails finish = rbb.build();
        versions.add(finish);
        hasRun = true;
    }

    public Optional<Horse> getWinner() {
        if (!hasRun) {
            return Optional.empty();
        }
        return getCurrentVersion().winner;
    }

    public void newOdds(final Map<Horse, Double> book) {
        final RaceBodyBuilder rbb = new RaceBodyBuilder(this);
        rbb.odds = book;
        final RaceDetails nextVer = rbb.build();
        versions.add(nextVer);
    }

    public LocalDateTime raceTime() {
        return getCurrentVersion().raceTime;
    }

    @JsonProperty
    public long getID() {
        return id;
    }

    @JsonProperty
    public boolean hasRun() {
        return hasRun;
    }

    /**
     *
     * @author ben
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class RaceDetails {

        private final long id;
        private final Map<Horse, Double> odds; // Decimal "Betfair" style odds
        private final LocalDateTime raceTime;
        private final long version;
        private final Optional<Horse> winner;

        private RaceDetails(RaceBodyBuilder rb) {
            super();
            id = rb.id;
            odds = rb.odds;
            raceTime = rb.raceTime;
            version = rb.version++;
            winner = Optional.ofNullable(rb.winner);
        }

        private RaceDetails(long ID) {
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

        public Set<Horse> getRunners() {
            return odds.keySet();
        }

        // JSON Properties
        @JsonProperty
        public Map<Long, Double> getOdds() {
            return odds.keySet().stream()
                    .collect(Collectors.toMap(h -> h.getID(), h -> odds.get(h)));
        }

        @JsonProperty
        public LocalDateTime getRaceTime() {
            return raceTime;
        }

        @JsonProperty
        public long getVersion() {
            return version;
        }
    }

    public static class RaceBodyBuilder implements Builder<RaceDetails> {

        private long id = 1;
        private Map<Horse, Double> odds = new HashMap<>();
        private LocalDateTime raceTime = LocalDateTime.MIN;
        private long version = 1;
        private Horse winner = null;

        @Override
        public RaceDetails build() {
            return new RaceDetails(this);
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
            final RaceDetails rb = race.getCurrentVersion();
            id = rb.id;
            odds = rb.odds;
            raceTime = rb.raceTime;
            version = rb.version;
            winner = rb.winner.orElse(null);
        }

        public RaceBodyBuilder() {
            super();
        }
    }

    public static Race parseBlob(final Map<String, ?> raceBlob) {
        final long raceID = Long.parseLong("" + raceBlob.get("id"));
        final Map<String, ?> blob = (Map<String, ?>) raceBlob.get("currentVersion");
        final LocalDateTime raceTime = JSONSerializable.parseDateTime((Map<String, ?>) blob.get("raceTime"));

        // Do the runners and then the odds                
        final List<Map<String, ?>> runBlob = (List<Map<String, ?>>) blob.get("runners");
        final Map<Long, Horse> tmpRunners = new HashMap<>();
        DomainFactory<Horse> stable = CentralFactory.getHorseFactory();
        final Map<Long, Long> remappedIds = new HashMap<>();
        RUNNERS:
        for (Map<String, ?> hB : runBlob) {
            final long idInInput = Long.parseLong("" + hB.get("id"));
            final String name = "" + hB.get("name");
            Horse h = stable.getByID(idInInput);
            if (h != null) {
                // We've seen this ID before, but does it really correspond to this horse?
                if (h.getName().equals(name)) {
                    tmpRunners.put(h.getID(), h);
                    // Yes, so store an identity mapping between fileID and actual
                    remappedIds.put(idInInput, idInInput);
                    continue RUNNERS;
                }
            }
            // Create the horse object
            h = stable.getByName(name);
            // And store the mapping between fileID and actual
            remappedIds.put(idInInput, h.getID());
            tmpRunners.put(h.getID(), h);
        }

        // Now do the odds using the remapped IDs
        final Map<String, ?> oddBlob = (Map<String, ?>) blob.get("odds");
        final Map<Horse, Double> odds = new HashMap<>();
        for (final String idStr : oddBlob.keySet()) {
            Horse runner;
            final Long runnerFromInput = Long.parseLong(idStr);
            runner = tmpRunners.get(remappedIds.get(runnerFromInput));

            final double chances = Double.parseDouble("" + oddBlob.get(idStr));
            odds.put(runner, chances);
        }

        // Create the race object
        final Race out = Race.of(raceTime, raceID, odds);
        
        // Set the winner
        final Map<String, ?> winnerBlob = (Map<String, ?>) raceBlob.get("winner");
        final long winnerIdInFile = Long.parseLong("" + winnerBlob.get("id"));
        out.winner(tmpRunners.get(remappedIds.get(winnerIdInFile)));

        return out;
    }
}
