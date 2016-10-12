package com.betleopard.domain;

import java.util.Optional;

/**
 *
 * @author ben
 */
public class Leg {
    private final Race race;
    private final OddsType oType;
    private final Optional<Double> odds;

    public Leg(final Race oppo, final Horse runner, final OddsType ot) {
        race = oppo;
        oType = ot;
        if (oType == OddsType.FIXED_ODDS) {
            odds = Optional.of(oppo.getOdds(runner));
        } else {
            odds = Optional.empty();
        }
    }
}
