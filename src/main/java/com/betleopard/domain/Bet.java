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

}
