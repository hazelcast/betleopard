package com.betleopard.logic;

import com.betleopard.domain.Horse;
import java.util.Map;

/**
 *
 * @author ben
 */
public final class ArbTools {
    private ArbTools() {}
    
    public static double arbPercent(final Map<Horse, Double> odds) {
        double round = 0.0;
        for (final Horse h : odds.keySet()) {
            final double current = odds.get(h);
            round += 1 / current;
        }
        return 1 - round;
    }
}
