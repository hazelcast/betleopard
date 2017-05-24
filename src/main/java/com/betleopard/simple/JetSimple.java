package com.betleopard.simple;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import static com.betleopard.hazelcast.JetBetMain.USER_ID;
import static com.betleopard.hazelcast.JetBetMain.WORST_ID;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.DAG;
import static com.hazelcast.jet.Edge.between;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import static com.hazelcast.jet.Processors.readMap;
import static com.hazelcast.jet.Processors.writeMap;
import com.hazelcast.jet.Vertex;
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

/**
 * Simple example for getting started - uses Jet adapted from Java 8 
 * collections
 * 
 * @author kittylyst
 */
public class JetSimple {

    public final static String EVENTS_BY_NAME = "events_by_name";

    private JetInstance jet;

    public static void main(String[] args) throws Exception {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
        CentralFactory.setRaces(new SimpleFactory<>());
        final JetSimple main = new JetSimple();
        main.go();
    }

    public void run() throws IOException, URISyntaxException {
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

        final Map<Horse, Set<Event>> inverted = new HashMap<>();
        for (Map.Entry<Event, Horse> entry : winners.entrySet()) {
            if (inverted.get(entry.getValue()) == null) {
                inverted.put(entry.getValue(), new HashSet<>());
            }
            inverted.get(entry.getValue()).add(entry.getKey());
        }

        final Function<Map.Entry<Horse, ?>, Horse> under1 = entry -> entry.getKey();
        final Function<Map.Entry<Horse, Integer>, Integer> under2 = entry -> entry.getValue();
        final Function<Map.Entry<Horse, Set<Event>>, Integer> setCount = entry -> entry.getValue().size();
        final Map<Horse, Integer> withWinCount
                = inverted.entrySet().stream()
                .collect(Collectors.toMap(under1, setCount));

        final Map<Horse, Integer> multipleWinners
                = withWinCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .collect(Collectors.toMap(under1, under2));

        System.out.println("Multiple Winners from List :");
        System.out.println(multipleWinners);
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
        try (BufferedReader r = new BufferedReader(new InputStreamReader(JetSimple.class.getResourceAsStream("historical_races.json"), UTF_8))) {
            r.lines().map(l -> JSONSerializable.parse(l, Event::parseBag)).forEach(e -> name2Event.put(e.getName(), e));
        } catch (IOException iox) {

        }

    }

    public static DAG buildDag() {
        final DAG dag = new DAG();

        Vertex source = dag.newVertex("source", readMap(USER_ID));

        Vertex sink = dag.newVertex("sink", writeMap(WORST_ID));

        return dag.edge(between(source.localParallelism(1), sink));
    }

}
