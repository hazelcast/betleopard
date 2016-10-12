package com.betleopard.domain;

import java.util.Optional;

/**
 *
 * @author ben
 */
public class Leg {

    private final Race race;
    private final int oddsVersion;
    private final OddsType oType;
    private final Optional<Double> odds;
    private final Horse backing;
    private final Optional<Double> stakeOrAcc;

    public Leg(final Race oppo, final Horse runner, final OddsType ot, final Double stake) {
        race = oppo;
        backing = runner;
        oType = ot;
        if (oType == OddsType.FIXED_ODDS) {
            odds = Optional.of(oppo.currentOdds(runner));
            oddsVersion = race.version();
        } else {
            odds = Optional.empty();
            oddsVersion = -1;
        }
        if (stake != null) {
            stakeOrAcc = Optional.of(stake);
        } else {
            stakeOrAcc = Optional.empty();
        }
    }

    private double stake() {
        if (!stakeOrAcc.isPresent()) {
            throw new IllegalStateException("Leg "+ toString() +" is not staked yet - part of an accumulator");
        }
        return stakeOrAcc.get();
    }

    public double odds() {
        return odds.orElse(race.currentOdds(backing));
    }

    public Race getRace() {
        return race;
    }

    public double payout() {
        final Optional<Horse> winner = race.getWinner();
        if (!winner.isPresent()) {
            throw new IllegalArgumentException("Race " + race.toString() + " has not been run");
        }
        final Horse fptp = winner.get();
        if (backing == fptp) {
            return stake() * odds();
        }
        return 0.0;
    }

    public double payout(final double startingStake) {
        final Optional<Horse> winner = race.getWinner();
        if (!winner.isPresent()) {
            throw new IllegalArgumentException("Race " + race.toString() + " has not been run");
        }
        final Horse fptp = winner.get();
        if (backing == fptp) {
            return startingStake * odds();
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "Leg{" + "race=" + race + ", oType=" + oType + ", odds=" + odds + '}';
    }

}
