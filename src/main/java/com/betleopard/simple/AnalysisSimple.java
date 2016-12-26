package com.betleopard.simple;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
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
 * @author kittylyst
 */
public class AnalysisSimple {

    public static void main(String[] args) throws IOException, URISyntaxException {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
        CentralFactory.setRaces(new SimpleFactory<>());
        final AnalysisSimple main = new AnalysisSimple();
        main.run();
    }

    private void run() throws IOException, URISyntaxException {
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

}
