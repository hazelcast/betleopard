package com.betleopard.hazelcast;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.*;
import com.betleopard.Utils;
import com.betleopard.domain.Race.RaceDetails;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.*;
import com.hazelcast.query.Predicate;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import scala.Tuple2;

import static java.time.temporal.TemporalAdjusters.next;

import static com.hazelcast.jet.Edge.between;
import static com.hazelcast.jet.Traversers.traverseIterable;
import static com.hazelcast.jet.Traversers.traverseStream;
import static com.hazelcast.jet.Util.entry;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * The main example driver class. Uses both Hazlecast IMDG and Jet to perform
 * data storage and live analysis of in-running bets.
 *
 * @author kittylyst
 */
public class JetBetMain {

    private JetInstance jet;
    private DAG dag;
    private volatile boolean shutdown = false;
    private HazelcastInstance client;
    private Path filePath;

    private final int NUM_USERS = 100;
    public final static String USER_ID = "users";
    public final static String EVENT_ID = "events";
    public static final String WORST_ID = "worst_case";

    public static void main(String[] args) throws Exception {
        // Initialize the domain object factories with Hazelcast IMDG
        CentralFactory.setHorses(HazelcastHorseFactory.getInstance());
        CentralFactory.setRaces(new HazelcastFactory<>(Race.class));
        CentralFactory.setEvents(new HazelcastFactory<>(Event.class));
        CentralFactory.setUsers(new HazelcastFactory<>(User.class));
        CentralFactory.setBets(new HazelcastFactory<>(Bet.class));

        final JetBetMain main = new JetBetMain();
        main.init();
        main.run();
        main.stop();
    }

    private void init() throws IOException, URISyntaxException {
        jet = Jet.newJetInstance();
        dag = buildDag();

        final ClientConfig config = new ClientConfig();
        client = HazelcastClient.newHazelcastClient(config);

        loadHistoricalRaces();
        createRandomUsers();
        createFutureEvent();
    }

    public void stop() throws IOException {
        Jet.shutdownAll();
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

    public static Predicate<Long, User> bettingOnSat(final LocalDate thisSat) {

        // Does this user have a bet on this Sat?
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

        return betOnSat;
    }

    public static final List<Entry<Race, Entry<Horse, Bet>>> rearraneBet(final Bet b) {
        final List<Entry<Race, Entry<Horse, Bet>>> out = new ArrayList<>();
        for (Leg l : b.getLegs()) {
            
        }
        return out;
    }
    
    /**
     * Do live recalculation of how much potential loss the house is exposed to
     */
    public void recalculateRiskReports() {
        final IMap<Long, User> users = client.getMap(USER_ID);

        final LocalDate thisSat = LocalDate.now().with(next(DayOfWeek.SATURDAY));
        Predicate<Long, User> betOnSat = bettingOnSat(thisSat);

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

//        final JavaRDD<Bet> betRDD = sc.parallelize(bets);
//        final JavaPairRDD<Race, Set<Bet>> betsByRace
//                = betRDD.flatMapToPair(b -> {
//                    final List<Tuple2<Race, Set<Bet>>> out = new ArrayList<>();
//                    for (final Leg l : b.getLegs()) {
//                        final Set<Bet> bs = new HashSet<>();
//                        bs.add(b);
//                        out.add(new Tuple2<>(l.getRace(), bs));
//                    }
//                    return out;
//                }).reduceByKey((s1, s2) -> {
//                    s1.addAll(s2);
//                    return s1;
//                });
//
//        // For each race, partition the set of bets by the horse they're backing
//        final JavaPairRDD<Race, Map<Horse, Set<Bet>>> partitionedBets
//                = betsByRace.mapToPair(t -> {
//                    final Race r = t._1;
//                    final Map<Horse, Set<Bet>> p = new HashMap<>();
//                    for (final Bet b : t._2) {
//                        for (final Leg l : b.getLegs()) {
//                            if (l.getRace().equals(r)) {
//                                final Horse h = l.getBacking();
//                                if (p.get(h) == null) {
//                                    p.put(h, new HashSet<>());
//                                }
//                                p.get(h).add(b);
//                            }
//                        }
//                    }
//                    return new Tuple2<>(r, p);
//                });
//
//        // Now we can compute the potential loss if a specific horse wins each race
//        // and can come up with a worst case analysis, where the house's losses are
//        // maximised across all races...
//        final JavaPairRDD<Race, Tuple2<Horse, Double>> badResults
//                = partitionedBets.mapToPair(t -> {
//                    final Race r = t._1;
//                    final Map<Horse, Double> odds = r.currentOdds();
//                    return new Tuple2<>(r, Utils.worstCase(odds, t._2()));
//                });
        // Output "perfect storm" combination of top 20 results that caused the losses
        final List<Tuple2<Race, Tuple2<Horse, Double>>> topRisks = null;
//                = badResults.takeOrdered(20, new Utils.RaceCostComparator());

        topRisks.forEach(t -> {
            System.out.println(t._1 + " won by " + t._2._1 + " causes losses of " + t._2._2);
        });

        // Finally output the maximum possible loss
        final Tuple2<Horse, Double> zero = new Tuple2<>(Horse.PALE, 0.0);
        final Tuple2<Horse, Double> apocalypse = null;
//                = badResults.values()
//                .fold(zero, (t1, t2) -> new Tuple2<>(Horse.PALE, t1._2 + t2._2));
        System.out.println("Worst case total losses: " + apocalypse._2);
    }

    static DAG buildDag() {
        final DAG dag = new DAG();

        Vertex source = dag.newVertex("source", readMap(USER_ID));

        Vertex scanBets = dag.newVertex("scan-for-bets", flatMap((Entry<?, User> e) -> traverseIterable(e.getValue().getKnownBets())));
        
        Vertex racesToBets = dag.newVertex("races-to-bets", flatMap((Bet b) -> {
            Stream<Leg> sl = b.getLegs().stream();
            sl.map(l -> entry(l.getRace(), entry(l.getBacking(), b)));
            // r -> <h, b>
            return traverseStream(sl);
        }));
        
        Vertex sink = dag.newVertex("sink", writeMap(WORST_ID));

        return dag.edge(between(source.localParallelism(1), scanBets))
                .edge(between(scanBets.localParallelism(1), racesToBets))
                .edge(between(racesToBets.localParallelism(1), sink));
    }
    
    /* 
     * After this point, everything is boilerplate and support methods for the simulation
     * 
     */
    /**
     * Set up an event to hang the bets off  
     */
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

    /**
     * Generates some simulated bets to test the risk reporting
     */
    public void addSomeSimulatedBets() {
        final IMap<Long, Event> events = client.getMap(EVENT_ID);
        final IMap<Long, User> users = client.getMap("users");
        System.out.println("Events: " + events.size());
        System.out.println("Users: " + users.size());

        final int numBets = 100;
        for (int i = 0; i < numBets; i++) {
            final Race r = getRandomRace(events);
            final Horse shergar = getRandomHorse(r);
            final Leg l = new Leg(r, shergar, OddsType.FIXED_ODDS, 2.0);
            final Bet.BetBuilder bb = CentralFactory.betOf();
            final Bet b = bb.addLeg(l).stake(l.stake()).build();
            final int rU = new Random().nextInt(users.size());
            User u = null;
            int j = 0;
            USERS:
            for (final User tmp : users.values()) {
                if (j >= rU) {
                    u = tmp;
                    break USERS;
                }
                j++;
            }
            if (u == null)
                throw new IllegalStateException("Failed to pick a user for a random bet");
            if (!u.addBet(b)) {
                System.out.println("Bet " + b + " not added successfully");
            }
            users.put(u.getID(), u);
        }
        int betCount = 0;
        for (final User u : users.values()) {
            betCount += u.getKnownBets().size();
        }
        System.out.println("Total Bets: " + betCount);
    }

    /**
     * Utility method to get some horses for simulated races
     * 
     * @param horses
     * @param num
     * @return 
     */
    public Set<Horse> makeRunners(final Set<Horse> horses, int num) {
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

    /**
     * Create some simulated odds for this set of runners
     * 
     * @param runners
     * @return 
     */
    public Map<Horse, Double> makeSimulatedOdds(final Set<Horse> runners) {
        final Set<Horse> thisRace = makeRunners(runners, 4);
        final Map<Horse, Double> out = new HashMap<>();
        int i = 1;
        for (Horse h : thisRace) {
            out.put(h, Math.random() * i++);
        }
        return out;
    }

    /**
     * Return a {@code Race} at random from the provided set
     * 
     * @param eventsByID
     * @return 
     */
    public Race getRandomRace(final IMap<Long, Event> eventsByID) {
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
    public Horse getRandomHorse(final Race r) {
        final List<Horse> geegees = new ArrayList<>(r.getCurrentVersion().getRunners());
        final int rH = new Random().nextInt(geegees.size());
        return geegees.get(rH);
    }

    /**
     * Sets up some random users (to place bets) and stores them in Hazlecast IMDG
     */
    public void createRandomUsers() {
        final IMap<Long, User> users = client.getMap("users");

        final String[] firstNames = {"Dave", "Christine", "Sarah", "Sadiq", "Zoe", "Helen", "Mike", "George", "Joanne"};
        final String[] lastNames = {"Baker", "Jones", "Smith", "Singh", "Shah", "Johnson", "Taylor", "Evans", "Howe"};
        final Random r = new Random();
        for (int i = 0; i < NUM_USERS; i++) {
            final User u = CentralFactory.userOf(firstNames[r.nextInt(firstNames.length)], lastNames[r.nextInt(lastNames.length)]);
            users.put(u.getID(), u);
        }
    }

    /**
     * Loads in historical data and stores in Hazelcast IMDG. This is mostly to 
     * provide a source of horses for the bet simulation.
     * 
     * @throws IOException 
     */
    public void loadHistoricalRaces() throws IOException, URISyntaxException {
        filePath = Utils.unpackDataToTmp("historical_races.json");

        final Path p = Paths.get(getClass().getClassLoader().getResource("historical_races.json").toURI());
        final List<String> eventsText = Files.readAllLines(p);

        final List<Event> events
                = eventsText.stream()
                .map(s -> JSONSerializable.parse(s, Event::parseBag))
                .collect(Collectors.toList());

        final Function<Event, Horse> fptp = e -> e.getRaces().get(0).getWinner().orElse(Horse.PALE);
        final Map<Event, Horse> winners
                = events.stream()
                .collect(Collectors.toMap(Function.identity(), fptp));

        final Map<Horse, List<Event>> inverted = new HashMap<>();
        for (Map.Entry<Event, Horse> entry : winners.entrySet()) {
            if (inverted.get(entry.getValue()) == null) {
                inverted.put(entry.getValue(), new ArrayList<>());
            }
            inverted.get(entry.getValue()).add(entry.getKey());
        }
        final IMap<Horse, List<Event>> invertHC = client.getMap("inverted");
        for (final Horse h : inverted.keySet()) {
            invertHC.put(h, inverted.get(h));
        }

        final Function<Map.Entry<Horse, ?>, Horse> under1 = entry -> entry.getKey();
        final Function<Map.Entry<Horse, Integer>, Integer> under2 = entry -> entry.getValue();
        final Function<Map.Entry<Horse, List<Event>>, Integer> setCount = entry -> entry.getValue().size();
        final Map<Horse, Integer> withWinCount
                = inverted.entrySet().stream()
                .collect(Collectors.toMap(under1, setCount));

        final Map<Horse, Integer> multipleWinners
                = withWinCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .collect(Collectors.toMap(under1, under2));

        final IMap<Horse, Integer> fromHC = client.getMap("winners");
        for (final Horse h : multipleWinners.keySet()) {
            System.out.println("Putting: " + h + " : " + multipleWinners.get(h));
            fromHC.put(h, multipleWinners.get(h));
        }
    }
}
