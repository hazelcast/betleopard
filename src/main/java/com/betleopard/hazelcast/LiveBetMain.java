package com.betleopard.hazelcast;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.*;
import com.betleopard.Utils;
import com.betleopard.domain.Race.RaceDetails;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.spark.connector.rdd.HazelcastRDDFunctions;
import static com.hazelcast.spark.connector.HazelcastJavaPairRDDFunctions.javaPairRddFunctions;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static java.time.temporal.TemporalAdjusters.next;
import java.util.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 * The main example driver class. Uses both Hazlecast IMDG and Spark to perform
 * data storage and live analysis of in-running bets.
 *
 * @author kittylyst
 */
public class LiveBetMain implements RandomSimulationUtils {

    private HazelcastInstance client;

    private JavaSparkContext sc;
    private volatile boolean shutdown = false;
    private Path filePath;

    public static void main(String[] args) throws IOException {
        // Initialize the domain object factories with Hazelcast IMDG
        CentralFactory.setHorses(HazelcastHorseFactory.getInstance());
        CentralFactory.setRaces(new HazelcastFactory<>(Race.class));
        CentralFactory.setEvents(new HazelcastFactory<>(Event.class));
        CentralFactory.setUsers(new HazelcastFactory<>(User.class));
        CentralFactory.setBets(new HazelcastFactory<>(Bet.class));

        final LiveBetMain main = new LiveBetMain();
        main.init();
        main.run();
        main.stop();
    }

    @Override
    public HazelcastInstance getClient() {
        return client;
    }

    private void init() throws IOException {
        final ClientConfig config = new ClientConfig();
        client = HazelcastClient.newHazelcastClient(config);

        final SparkConf conf = new SparkConf()
                .set("hazelcast.server.addresses", "127.0.0.1:5701")
                .set("hazelcast.server.groupName", "dev")
                .set("hazelcast.server.groupPass", "dev-pass")
                .set("hazelcast.spark.valueBatchingEnabled", "true")
                .set("hazelcast.spark.readBatchSize", "5000")
                .set("hazelcast.spark.writeBatchSize", "5000");

        sc = new JavaSparkContext("local", "appname", conf);

        loadHistoricalRaces();
        createRandomUsers();
        createFutureEvent();
    }

    public void stop() throws IOException {
        sc.stop();
        Utils.cleanupDataInTmp(filePath);
    }

    /**
     * Main run loop 
     */
    public void run() {
        MAIN:
        while (!shutdown) {
            addSomeSimulatedBets();
            recalculateRiskReports();
            try {
                // Simulated delay
                Thread.sleep(20_000);
            } catch (InterruptedException ex) {
                shutdown = true;
                continue MAIN;
            }
        }
    }

    /**
     * Do live recalculation of how much potential loss the house is exposed to
     */
    public void recalculateRiskReports() {
        final IMap<Long, User> users = client.getMap("users");

        // Does this user have a bet on this Sat?
        final LocalDate thisSat = LocalDate.now().with(next(DayOfWeek.SATURDAY));
        final Predicate<Long, User> betOnSat = e -> {
            for (final Bet b : e.getValue().getKnownBets()) {
                INNER:
                for (final Leg l : b.getLegs()) {
                    final LocalDate legDate = l.getRace().getCurrentVersion().getRaceTime().toLocalDate();
                    if (legDate.equals(thisSat)) {
                        return true;
                    } else if (legDate.isBefore(thisSat)) {
                        break INNER;
                    }
                }
            }
            return false;
        };

        // Read bets that are ordered and happen on Saturday
        final List<Bet> bets = new ArrayList<>();
        for (final User u : users.values(betOnSat)) {
            // Construct a map of races -> set of bets
            for (final Bet b : u.getKnownBets()) {
                BETS:
                for (final Leg l : b.getLegs()) {
                    final RaceDetails rd = l.getRace().getCurrentVersion();
                    final LocalDate legDate = rd.getRaceTime().toLocalDate();
                    if (legDate.equals(thisSat)) {
                        bets.add(b);
                    } else if (legDate.isBefore(thisSat)) {
                        break BETS;
                    }
                }
            }
        }

        final JavaRDD<Bet> betRDD = sc.parallelize(bets);
        final JavaPairRDD<Race, Set<Bet>> betsByRace
                = betRDD.flatMapToPair(b -> {
                    final List<Tuple2<Race, Set<Bet>>> out = new ArrayList<>();
                    for (final Leg l : b.getLegs()) {
                        final Set<Bet> bs = new HashSet<>();
                        bs.add(b);
                        out.add(new Tuple2<>(l.getRace(), bs));
                    }
                    return out.iterator();
                }).reduceByKey((s1, s2) -> {
                    s1.addAll(s2);
                    return s1;
                });

        // For each race, partition the set of bets by the horse they're backing
        final JavaPairRDD<Race, Map<Horse, Set<Bet>>> partitionedBets
                = betsByRace.mapToPair(t -> {
                    final Race r = t._1;
                    final Map<Horse, Set<Bet>> p = new HashMap<>();
                    for (final Bet b : t._2) {
                        for (final Leg l : b.getLegs()) {
                            if (l.getRace().equals(r)) {
                                final Horse h = l.getBacking();
                                if (p.get(h) == null) {
                                    p.put(h, new HashSet<>());
                                }
                                p.get(h).add(b);
                            }
                        }
                    }
                    return new Tuple2<>(r, p);
                });

        // Now we can compute the potential loss if a specific horse wins each race
        // and can come up with a worst case analysis, where the house's losses are
        // maximised across all races...
        final JavaPairRDD<Race, Tuple2<Horse, Double>> badResults
                = partitionedBets.mapToPair(t -> {
                    final Race r = t._1;
                    final Map<Horse, Double> odds = r.currentOdds();
                    return new Tuple2<>(r, Utils.worstCase(odds, t._2()));
                });

        // Output "perfect storm" combination of top 20 results that caused the losses
        final List<Tuple2<Race, Tuple2<Horse, Double>>> topRisks
                = badResults.takeOrdered(20, new Utils.RaceCostComparator());

        topRisks.forEach(t -> {
            System.out.println(t._1 + " won by " + t._2._1 + " causes losses of " + t._2._2);
        });

        // Finally output the maximum possible loss
        final Tuple2<Horse, Double> zero = new Tuple2<>(Horse.PALE, 0.0);
        final Tuple2<Horse, Double> apocalypse
                = badResults.values()
                .fold(zero, (t1, t2) -> new Tuple2<>(Horse.PALE, t1._2 + t2._2));
        System.out.println("Worst case total losses: " + apocalypse._2);
    }

    /* 
     * After this point, everything is boilerplate and support methods for the simulation
     * 
     */
    /**
     * Loads in historical data and stores in Hazelcast IMDG. This is mostly to 
     * provide a source of horses for the bet simulation.
     * 
     * @throws IOException 
     */
    public void loadHistoricalRaces() throws IOException {
        filePath = Utils.unpackDataToTmp("historical_races.json");

        final JavaRDD<String> eventsText = sc.textFile(filePath.toString());
        final JavaRDD<Event> events
                = eventsText.map(s -> JSONSerializable.parse(s, Event::parseBag));

        final JavaPairRDD<Horse, Integer> winners
                = events.mapToPair(e -> new Tuple2<>(e.getRaces().get(0).getWinner().orElse(Horse.PALE), 1))
                .reduceByKey((a, b) -> a + b);

        final HazelcastRDDFunctions accessToHC = javaPairRddFunctions(winners);
        accessToHC.saveToHazelcastMap("winners");
    }

}
