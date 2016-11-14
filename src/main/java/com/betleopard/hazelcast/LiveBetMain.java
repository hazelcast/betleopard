package com.betleopard.hazelcast;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import com.betleopard.domain.Race;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spark.connector.rdd.HazelcastRDDFunctions;
import static com.hazelcast.spark.connector.HazelcastJavaPairRDDFunctions.javaPairRddFunctions;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 *
 * @author ben
 */
public class LiveBetMain {

    private JavaSparkContext sc;
    private volatile boolean shutdown = false;
    private final HazelcastInstance client = HazelcastClient.newHazelcastClient();

    public static void main(String[] args) {
        final HazelcastFactory<Horse> stable = HazelcastHorseFactory.getInstance();
        CentralFactory.setHorses(stable);
        final HazelcastFactory<Race> raceFactory = new HazelcastFactory<>(Race.class);
        CentralFactory.setRaces(raceFactory);
        final HazelcastFactory<Event> eventFactory = new HazelcastFactory<>(Event.class);
        CentralFactory.setEvents(eventFactory);

        final LiveBetMain main = new LiveBetMain();
        main.init();
        main.run();
        main.stop();
    }

    private void init() {
        final SparkConf conf = new SparkConf()
                .set("hazelcast.server.addresses", "127.0.0.1:5701")
                .set("hazelcast.server.groupName", "dev")
                .set("hazelcast.server.groupPass", "dev-pass")
                .set("hazelcast.spark.valueBatchingEnabled", "true")
                .set("hazelcast.spark.readBatchSize", "5000")
                .set("hazelcast.spark.writeBatchSize", "5000");

        sc = new JavaSparkContext("local", "appname", conf);

        final JavaRDD<String> eventsText = sc.textFile("/tmp/historical_races.json");
        final JavaRDD<Event> events
                = eventsText.map(s -> JSONSerializable.parse(s, Event::parseBlob));

        final JavaPairRDD<Horse, Integer> winners
                = events.mapToPair(e -> new Tuple2<>(e.getRaces().get(0).getWinner().orElse(Horse.PALE), 1))
                .reduceByKey((a, b) -> a + b);

        final HazelcastRDDFunctions accessToHC = javaPairRddFunctions(winners);
        accessToHC.saveToHazelcastMap("winners");

        createFutureEvent();
    }

    public void stop() {
        sc.stop();
    }

    public void run() {
        MAIN:
        while (!shutdown) {
            addSomeSimulatedBets();
            try {
                Thread.sleep(2);
            } catch (InterruptedException ex) {
                shutdown = true;
                continue MAIN;
            }
            recalculateRiskReports();
            try {
                Thread.sleep(2);
            } catch (InterruptedException ex) {
                shutdown = true;
                continue MAIN;
            }
        }
    }

    public void createFutureEvent() {
        // Grab some horses to use as runners in races
        final IMap<Horse, Object> fromHC = client.getMap("winners");
        final Set<Horse> horses = fromHC.keySet();

        // Now set up some future-dated events for next Sat
        final LocalDate nextSat = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        LocalTime raceTime = LocalTime.of(11, 0); // 1100 start
        final Event e = CentralFactory.eventOf("Racing from Epsom", nextSat);
        final Set<Horse> runners = makeRunners(horses, 10);
        for (int i = 0; i < 18; i++) {
            final Map<Horse, Double> runnersWithOdds = makeSimulatedOdds(runners);
            final Race r = CentralFactory.raceOf(LocalDateTime.of(nextSat, raceTime), runnersWithOdds);
            e.addRace(r);

            raceTime = raceTime.plusMinutes(10);
        }
        final IMap<Long, Event> events = client.getMap("events");
        events.put(e.getID(), e);
    }

    public void addSomeSimulatedBets() {
        final IMap<Long, Event> events = client.getMap("events");
        final int numBets = 100;
        for (int i = 0; i < numBets; i++) {
            final Race r = getRandomRace(events);
            final Map<Long, Double> odds = r.getCurrentVersion().getOdds();
            final Horse shergar = getRandomHorse(r);
        }
    }

    public void recalculateRiskReports() {

    }

    Set<Horse> makeRunners(final Set<Horse> horses, int num) {
        if (horses.size() < num) {
            return horses;
        }
        final Set<Horse> out = new HashSet<>();
        final Iterator<Horse> it = horses.iterator();
        for (int i = 0; i < num; i++) {
            out.add(it.next());
        }
        return out;
    }

    Map<Horse, Double> makeSimulatedOdds(final Set<Horse> runners) {
        final Set<Horse> thisRace = makeRunners(runners, 4);
        final Map<Horse, Double> out = new HashMap<>();
        int i = 1;
        for (Horse h : thisRace) {
            out.put(h, Math.random() * i++);
        }
        return out;
    }

    Race getRandomRace(final IMap<Long, Event> eventsByID) {
        final List<Event> events = new ArrayList<>(eventsByID.values());
        final int rI = new Random().nextInt(events.size());
        final Event theDay = events.get(rI);
        final List<Race> races = theDay.getRaces();
        final int rR = new Random().nextInt(races.size());
        return races.get(rR);
    }

    Horse getRandomHorse(final Race r) {
        final List<Horse> geegees = new ArrayList<>(r.getCurrentVersion().getRunners());
        final int rH = new Random().nextInt(geegees.size());
        return geegees.get(rH);
    }
    
}
