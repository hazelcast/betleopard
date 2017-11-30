package com.betleopard;

import com.betleopard.domain.*;
import com.betleopard.hazelcast.AnalysisSpark;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.function.Function.identity;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toSet;
import scala.Tuple2;

/**
 * Holder for associated helper methods
 * 
 * @author kittylyst
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Calculate the arbitrage percentage on a given set of odds, for a single
     * race
     * 
     * @param odds the map of horses to odds
     * @return     the arbitrage percentage
     */
    public static double arbPercent(final Map<Horse, Double> odds) {
        double round = 0.0;
        for (final Horse h : odds.keySet()) {
            final double current = odds.get(h);
            round += 1 / current;
        }
        return 1 - round;
    }

    /**
     * A helper method to calculate the worst case outcome from a single race,
     * given the amount staked on each horse.
     * 
     * @param odds the horses and their odds
     * @param bets all bets pplaced that back a given horse
     * @return     the horse that induces the worst outcome and the houses loss
     */
    public static Tuple2<Horse, Double> worstCase(Map<Horse, Double> odds, Map<Horse, Set<Bet>> bets) {
        final Set<Horse> runners = odds.keySet();
        Tuple2<Horse, Double> out = new Tuple2<>(Horse.PALE, Double.MIN_VALUE);
        for (final Horse h : runners) {
            double runningTotal = 0;
            final Set<Bet> atStake = bets.get(h);
            if (atStake == null)
                continue;
            for (final Bet b : atStake) {
                // Avoid dealing with ackers for now:
                if (!b.single()) 
                    continue;
                
                runningTotal += b.projectedPayout(h);
            }
            if (runningTotal > out._2) {
                out = new Tuple2<>(h, runningTotal);
            }
        }

        return out;
    }

    /**
     * Helper class, needed because the comparator needs to be serializable,
     * and yet needs to be stateless and not a lambda
     */
    public static class RaceCostComparator implements Comparator<Tuple2<Race, Tuple2<Horse, Double>>>, Serializable {

        @Override
        public int compare(Tuple2<Race, Tuple2<Horse, Double>> t1, Tuple2<Race, Tuple2<Horse, Double>> t2) {
            return t1._2._2.compareTo(t2._2._2);
        }
    }

    public static Predicate<Long, User> bettingOnDate(final LocalDate thisSat) {
        // Does this user have a bet on this Sat?
        return userEntry -> userEntry.getValue().getKnownBets().stream()
                .flatMap(bet -> bet.getLegs().stream())
                .filter(leg -> leg.getRace().getCurrentVersion().getRaceTime().toLocalDate().equals(thisSat))
                .findAny()
                .isPresent();
    }

    /**
     * Utility method to get some horses for simulated races
     * 
     * @param horses
     * @param num
     * @return 
     */
    public static Set<Horse> makeRunners(final Set<Horse> horses, int num) {
        return horses.stream().limit(num).collect(toSet());
    }

    /**
     * Create some simulated odds for this set of runners
     * 
     * @param runners
     * @return 
     */
    public static Map<Horse, Double> makeSimulatedOdds(final Set<Horse> runners) {
        final AtomicInteger count = new AtomicInteger(1);
        return runners.stream()
                .limit(4)
                .collect(Collectors.toMap(identity(), h -> Math.random() * count.getAndIncrement()));

    }

    /**
     * Return a {@code Race} at random from the provided set
     * 
     * @param eventsByID
     * @return 
     */
    public static Race getRandomRace(final IMap<Long, Event> eventsByID) {
        final List<Event> events = new ArrayList<>(eventsByID.values());
        final int rI = new Random().nextInt(events.size());
        final Event theDay = events.get(rI);
        final List<Race> races = theDay.getRaces();
        final int rR = new Random().nextInt(races.size());
        return races.get(rR);
    }

    /**
     * Return a random horse from the set of runners in the provided {@code Race}
     * 
     * @param r
     * @return 
     */
    public static Horse getRandomHorse(final Race r) {
        final List<Horse> geegees = new ArrayList<>(r.getCurrentVersion().getRunners());
        final int rH = new Random().nextInt(geegees.size());
        return geegees.get(rH);
    }

    /**
     * Moves a file from a resource path to a temp dir under /tmp. 
     * 
     * @param resourceName a {@code String} representing the resource to be moved
     * @return             a {@code Path} to the moved file
     * @throws IOException a general, unexpected IO failure
     */
    public static Path unpackDataToTmp(final String resourceName) throws IOException {
        final InputStream in = AnalysisSpark.class.getResourceAsStream("/" + resourceName);
        final Path tmpdir = Files.createTempDirectory(Paths.get("/tmp"), "hc-spark-test");
        final Path dataFile = tmpdir.resolve(resourceName);
        Files.copy(in, dataFile, StandardCopyOption.REPLACE_EXISTING);

        return dataFile;
    }

    /**
     * Cleans up the temp directory.
     * 
     * @param tmpdir       the directory to be cleaned (the parent of the file that was moved)
     * @throws IOException a general, unexpected IO failure
     */
    public static void cleanupDataInTmp(final Path tmpdir) throws IOException {
        Files.walkFileTree(tmpdir, new Reaper());
    }

    /**
     * A helper class that can be used to recursively delete and clean up
     * the temp dir
     */
    public static final class Reaper extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            } else {
                throw exc;
            }
        }
    }

}
