package com.betleopard.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author ben
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bet {

    private final long id;
    private final List<Leg> legs;
    private final double stake;
    private final BetType type;

    @JsonProperty
    public long getId() {
        return id;
    }
    
    @JsonProperty
    public List<Leg> getLegs() {
        return legs;
    }

    @JsonProperty
    public double getStake() {
        return stake;
    }

    @JsonProperty
    public BetType getType() {
        return type;
    }

    private Bet(BetBuilder bb) {
        id = bb.id;
        stake = bb.stake;
        legs = orderLegsByTime(bb.legs);
        type = bb.type;
    }

    private static List<Leg> orderLegsByTime(final Set<Leg> legs) {
        return legs.stream()
                .sorted((l1, l2) -> l1.getRace().raceTime().isBefore(l2.getRace().raceTime()) ? -1 : 1)
                .collect(Collectors.toList());
    }

    public static class BetBuilder implements Builder<Bet> {

        private long id;
        private Set<Leg> legs = new HashSet<>();
        private double stake;
        private BetType type;

        @Override
        public Bet build() {
            return new Bet(this);
        }

        public BetBuilder id(final int id) {
            this.id = id;
            return this;
        }

        public BetBuilder stake(final double stake) {
            this.stake = stake;
            return this;
        }

        public BetBuilder type(final BetType type) {
            this.type = type;
            return this;
        }

        public BetBuilder addLeg(final Leg leg) {
            legs.add(leg);
            return this;
        }

        public BetBuilder clearLegs() {
            legs = new HashSet<>();
            return this;
        }
    }

    public double payout() {
        if (legs.size() == 1) {
            if (type != BetType.SINGLE)
                throw new IllegalStateException("Bet has " + legs.size() + " legs but claims to be a single bet");
            return legs.iterator().next().payout();
        }

        // Accer case
        if (type != BetType.ACCUM)
            throw new IllegalStateException("Bet has " + legs.size() + " legs but claims not to be an accumulator");

        // Check the races have all finished
        for (final Leg l : legs) {
            final Race race = l.getRace();
            if (!race.getWinner().isPresent())
                throw new IllegalArgumentException("Race " + race.toString() + " has not been run");
        }

        // OK, we have run all the races, now order by time 
        final List<Leg> sortedLegs = new ArrayList<>();
        sortedLegs.addAll(legs);
        sortedLegs.sort((a, b) -> a.getRace().raceTime().compareTo(b.getRace().raceTime()));

        double out = stake;
        for (final Leg l : sortedLegs) {
            out = l.payout(out);
        }
        return out;
    }

    public static Bet parse(final String betText) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return parseBlob(mapper.readValue(betText, new TypeReference<Map<String, ?>>() {
        }));
    }

    public static Bet parseBlob(final Map<String, ?> blob) throws IOException {
        final BetBuilder bb = new BetBuilder();
        bb.id = Long.parseLong("" + blob.get("id"));
        bb.stake = Double.parseDouble("" + blob.get("stake"));
        bb.type = BetType.valueOf("" + blob.get("type"));

        List<Map<String, ?>> legBlobs = (List<Map<String, ?>>) blob.get("legs");
        for (Map<String, ?> lB : legBlobs) {
            bb.addLeg(Leg.parseBlob(lB));
        }

        return bb.build();
    }

    public String toJSONString() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

}
