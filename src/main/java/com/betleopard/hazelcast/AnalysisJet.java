package com.betleopard.hazelcast;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import com.betleopard.simple.SimpleFactory;
import com.betleopard.simple.SimpleHorseFactory;
import com.hazelcast.core.IMap;
import com.hazelcast.jet.*;
import static com.hazelcast.jet.Edge.between;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.betleopard.simple.AnalysisSimple.*;
import com.hazelcast.jet.function.DistributedFunction;
import com.hazelcast.jet.function.DistributedSupplier;
import com.hazelcast.jet.pipeline.ComputeStage;
import com.hazelcast.jet.pipeline.Pipeline;
import static com.hazelcast.jet.pipeline.Sinks.writeMap;
import static com.hazelcast.jet.processor.Processors.filter;
import static com.hazelcast.jet.processor.SourceProcessors.readMap;
import java.util.Map.Entry;
import static com.hazelcast.jet.pipeline.Sources.streamKafka;

/**
 * Simple example for getting started - uses Jet adapted from Java 8 
 * collections
 * 
 * @author kittylyst
 */
public class AnalysisJet {
    public final static String EVENTS_BY_NAME = "events_by_name";
    public final static String MULTIPLE = "multiple_winners";

    private final static DistributedSupplier<Long> INITIAL_ZERO = () -> 0L;

    private final static DistributedFunction<Entry<String, Event>, Horse> HORSE_FROM_EVENT = e -> FIRST_PAST_THE_POST.apply(e.getValue());

    private JetInstance jet;

    public static void main(String[] args) throws Exception {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
        CentralFactory.setRaces(new SimpleFactory<>());
        final AnalysisJet main = new AnalysisJet();
        main.setup();
        try {
            main.go();
            final Map<Horse, Long> multiple = main.getResults();
            final SimpleHorseFactory fact = SimpleHorseFactory.getInstance();
            System.out.println("Result set size: " + multiple.size());
            for (Horse h : multiple.keySet()) {
//                Horse h = fact.getByID(l);
                System.out.println(h + " : " + multiple.get(h));
            }
        } finally {
            Jet.shutdownAll();
        }
    }

    public void go() throws Exception {
        System.out.print("\nStarting up... ");
        long start = System.nanoTime();
        
        
        Pipeline p = Pipeline.create();
        ComputeStage<Entry<String, Long>> c = p.drawFrom(streamKafka());
//        c.attach(slidingWindow(entryKey(), slidingWindowDef(1, 1), counting()))
//         .drainTo(Sinks.writeMap("sink"));
        p.execute(jet);
        
        
        System.out.println("done in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " milliseconds.");
    }

    public void setup() {
        jet = Jet.newJetInstance();

        // Populate the map with data from disc
        final IMap<String, Event> name2Event = jet.getMap(EVENTS_BY_NAME);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(AnalysisJet.class.getResourceAsStream(HISTORICAL), UTF_8))) {
            r.lines().map(l -> JSONSerializable.parse(l, Event::parseBag)).forEach(e -> name2Event.put(e.getName(), e));
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }

    public Map<Horse, Long> getResults() {
        return jet.getMap(MULTIPLE);
    }

    public static DAG buildDag() {
        
        
        final DAG dag = new DAG();
        final Vertex source = dag.newVertex("source", readMap(EVENTS_BY_NAME));

        // How many events has this horse won? Use groupAndAccumulate() to reduce
        final Vertex count = dag.newVertex("reduce", reduce(HORSE_FROM_EVENT, INITIAL_ZERO, (tot, x) -> tot + 1));

        final Vertex multiple = dag.newVertex("multiple", filter((Entry<Long, Long> ent) -> ent.getValue() > 1));

        final Vertex sink = dag.newVertex("sink", writeMap(MULTIPLE));

        return dag.edge(between(source.localParallelism(1), count).partitioned(HORSE_FROM_EVENT))
                .edge(between(count, multiple))
                .edge(between(multiple, sink));
    }

}
