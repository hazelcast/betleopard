package com.betleopard.domain;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ben
 */
public class Bet {

    // unique bet ID - use a ThreadLocal atomic?

    private final Set<Leg> legs = new HashSet<>();
    private final int stake;
    private final BetType type;

    public Bet(final BetType singleOrAcc, final int amount) {
        type = singleOrAcc;
        stake = amount;
    }
}
