package com.betleopard;

import com.betleopard.domain.Bet;
import com.betleopard.domain.Horse;
import com.betleopard.domain.Race;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import scala.Tuple2;

/**
 *
 * @author ben
 */
public final class Utils {

    public static double arbPercent(final Map<Horse, Double> odds) {
        double round = 0.0;
        for (final Horse h : odds.keySet()) {
            final double current = odds.get(h);
            round += 1 / current;
        }
        return 1 - round;
    }

    public static Tuple2<Horse, Double> worstCase(Map<Horse, Double> odds, Map<Horse, Set<Bet>> partitions) {
        final Set<Horse> runners = odds.keySet();
        Tuple2<Horse, Double> out = new Tuple2<>(Horse.PALE, Double.MIN_VALUE);
        for (final Horse h : runners) {
            double runningTotal = 0;
            final Set<Bet> atStake = partitions.get(h);
            if (atStake == null)
                continue;
            for (final Bet b : atStake) {
                // Hack, avoid dealing with ackers for now:
                if (b.getLegs().size() > 1) {
                    continue;
                }
                runningTotal += b.projectedPayout(h);
            }
            if (runningTotal > out._2) {
                out = new Tuple2<>(h, runningTotal);
            }
        }

        return out;
    }

    /**
     * Helper class, needed because the comparator needs to serializable.
     */
    public static class RaceCostComparator implements Comparator<Tuple2<Race, Tuple2<Horse, Double>>>, Serializable {
        @Override
        public int compare(Tuple2<Race, Tuple2<Horse, Double>> t1, Tuple2<Race, Tuple2<Horse, Double>> t2) {
            return t1._2._2.compareTo(t2._2._2);
        }
    }
    
}