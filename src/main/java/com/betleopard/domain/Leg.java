package com.betleopard.domain;

/**
 *
 * @author ben
 */
public class Leg {

    private final OddsType oType;
    private final double odds;

    public Leg(final Opportunity oppo, final Horse runner, final OddsType ot) {
        oType = ot;
        if (oType == OddsType.FIXED_ODDS) {
            odds = oppo.getOdds(runner.getID());
        } else {
            odds = 0.0;
        }
    }
}
