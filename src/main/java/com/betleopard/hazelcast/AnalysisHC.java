package com.betleopard.hazelcast;

import com.betleopard.simple.*;
import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author ben
 */
public class AnalysisHC {

    public static void main(String[] args) throws IOException, URISyntaxException {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
        CentralFactory.setRaces(new SimpleFactory<>());
        final AnalysisHC main = new AnalysisHC();

        final HazelcastInstance client = HazelcastClient.newHazelcastClient();
        main.run(client);

        final IMap<Horse, Integer> fromHC = client.getMap("winners");
        final IMap<Horse, List<Event>> invertHC = client.getMap("inverted");
        System.out.println("Multiple Winners from HC :");

        for (final Horse h : fromHC.keySet()) {
            System.out.println("Retrieving from HC: " + h );
            System.out.println(invertHC.get(h));
        }
        client.getLifecycleService().terminate();
    }

    private void run(final HazelcastInstance client) throws IOException, URISyntaxException {
        final Path p = Paths.get(getClass().getClassLoader().getResource("historical_races.json").toURI());
        final List<String> eventsText = Files.readAllLines(p);

        final List<Event> events
                = eventsText.stream()
                .map(s -> JSONSerializable.parse(s, Event::parseBlob))
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

        final Function<HashMap.Entry<Horse, ?>, Horse> under1 = entry -> entry.getKey();
        final Function<HashMap.Entry<Horse, Integer>, Integer> under2 = entry -> entry.getValue();
        final Function<HashMap.Entry<Horse, List<Event>>, Integer> setCount = entry -> entry.getValue().size();
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
