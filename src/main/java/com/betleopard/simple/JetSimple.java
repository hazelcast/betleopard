package com.betleopard.simple;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.*;
import static com.hazelcast.jet.Edge.between;
import static com.hazelcast.jet.KeyExtractors.entryKey;
import static com.hazelcast.jet.Processors.readMap;
import static com.hazelcast.jet.Processors.writeMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.hazelcast.jet.Processors.map;
import static com.hazelcast.jet.Processors.groupAndAccumulate;
import static com.hazelcast.jet.Util.entry;
import java.util.Map.Entry;

/**
 * Simple example for getting started - uses Jet adapted from Java 8 
 * collections
 * 
 * @author kittylyst
 */
public class JetSimple {

    public final static String EVENTS_BY_NAME = "events_by_name";
    public final static String MULTIPLE = "multiple_winners";

    private final static String HISTORICAL = "historical_races.json";

    private final static Function<Event, Horse> FIRST_PAST_THE_POST = e -> e.getRaces().get(0).getWinner().orElse(Horse.PALE);
    private final static Function<Map.Entry<Horse, ?>, Horse> UNDER_1 = entry -> entry.getKey();
    private final static Function<Map.Entry<Horse, Integer>, Integer> UNDER_2 = entry -> entry.getValue();
    private final static Distributed.Supplier<Long> INITIAL_ZERO = () -> 0L;

    private JetInstance jet;

    public static void main(String[] args) throws Exception {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
        CentralFactory.setRaces(new SimpleFactory<>());
        final JetSimple main = new JetSimple();
        main.go();
    }

    public void run() throws IOException, URISyntaxException {
        final Path p = Paths.get(getClass().getClassLoader().getResource(HISTORICAL).toURI());
        final List<String> eventsText = Files.readAllLines(p);

        final List<Event> events
                = eventsText.stream()
                .map(s -> JSONSerializable.parse(s, Event::parseBag))
                .collect(Collectors.toList());

        final Map<Event, Horse> winners
                = events.stream()
                .collect(Collectors.toMap(Function.identity(), FIRST_PAST_THE_POST));

        final Map<Horse, Set<Event>> inverted = new HashMap<>();
        for (Map.Entry<Event, Horse> entry : winners.entrySet()) {
            if (inverted.get(entry.getValue()) == null) {
                inverted.put(entry.getValue(), new HashSet<>());
            }
            inverted.get(entry.getValue()).add(entry.getKey());
        }

        final Function<Map.Entry<Horse, Set<Event>>, Integer> setCount = entry -> entry.getValue().size();
        final Map<Horse, Integer> withWinCount
                = inverted.entrySet().stream()
                .collect(Collectors.toMap(UNDER_1, setCount));

        final Map<Horse, Integer> multipleWinners
                = withWinCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .collect(Collectors.toMap(UNDER_1, UNDER_2));

        System.out.println("Multiple Winners from List :");
        System.out.println(multipleWinners);
    }

    public static DAG buildDag() {
        final DAG dag = new DAG();

        final Vertex source = dag.newVertex("source", readMap(EVENTS_BY_NAME));
        
        // Take in a (NAME, EVENT) return an (HORSE, EVENT)
        final Vertex winners = dag.newVertex("winners", map((Entry<String, Event> e) -> {
            Event evt = e.getValue();
            return entry(FIRST_PAST_THE_POST.apply(evt), evt);
        }));
        
        // How many events has this horse won? Use groupAndCollect() to reduce
        final Vertex count = dag.newVertex("reduce", groupAndAccumulate(INITIAL_ZERO, (tot, x) -> tot + 1));
        
        // (HORSE, HORSE) -> (word, count)
        Vertex combine = dag.newVertex("combine",
                groupAndAccumulate(Entry<Horse, Long>::getKey, INITIAL_ZERO,
                        (Long val, Entry<Horse, Long> winCount) -> val + winCount.getValue())
        );

        final Vertex sink = dag.newVertex("sink", writeMap(MULTIPLE));

        return dag.edge(between(source.localParallelism(1), winners))
                .edge(between(winners.localParallelism(1), count))
                .edge(between(count, combine)
                          .distributed()
                          .partitioned(entryKey()))
                  .edge(between(combine, sink));
    }

    private void go() throws Exception {
        try {
            setup();
            System.out.print("\nStarting up... ");
            long start = System.nanoTime();
            jet.newJob(buildDag()).execute().get();
            System.out.print("done in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " milliseconds.");
//            printResults();
        } finally {
            Jet.shutdownAll();
        }
    }

    public void setup() {
        jet = Jet.newJetInstance();

        final ClientConfig config = new ClientConfig();
//        client = HazelcastClient.newHazelcastClient(config);

        // Prime a map
        final IMap<String, Event> name2Event = jet.getMap(EVENTS_BY_NAME);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(JetSimple.class.getResourceAsStream(HISTORICAL), UTF_8))) {
            r.lines().map(l -> JSONSerializable.parse(l, Event::parseBag)).forEach(e -> name2Event.put(e.getName(), e));
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }

}
