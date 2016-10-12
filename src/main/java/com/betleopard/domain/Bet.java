package com.betleopard.domain;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ben
 */
public class Bet {

    private final long id;
    private final Set<Leg> legs;
    private final int stake;
    private final BetType type;

    private Bet(BetBuilder bb) {
        id = bb.id;
        legs = bb.legs;
        stake = bb.stake;
        type = bb.type;
    }

    public static class BetBuilder implements Builder<Bet> {

        private long id;
        private Set<Leg> legs = new HashSet<>();
        private int stake;
        private BetType type;

        @Override
        public Bet build() {
            return new Bet(this);
        }

        public BetBuilder id(final int id) {
            this.id = id;
            return this;
        }

        public BetBuilder stake(final int stake) {
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
}
