package com.betleopard.simple;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author ben
 */
public class AnalysisSimple {

    public static void main(String[] args) throws IOException {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
        CentralFactory.setRaces(SimpleRaceFactory.getInstance());
        final AnalysisSimple main = new AnalysisSimple();
        main.run();
    }

    private void run() throws IOException {

        final List<String> eventsText = Files.readAllLines(Paths.get("/tmp/historical_races.json"));
        final List<Event> events
                = eventsText.stream()
                .map(s -> JSONSerializable.parse(s, Event::parseBlob))
                .collect(Collectors.toList());

        final List<Event> noRaces = events.stream()
                                          .filter(e -> e.getRaces().size() < 1)
                                          .collect(Collectors.toList());
        
        System.out.println("No races: "+ noRaces);
        
        Function<Event, Horse> fptp = e -> e.getRaces().get(0).getWinner().get();
        final Map<Event, Horse> winners
                = events.stream()
                .collect(Collectors.toMap(Function.identity(), fptp));

        System.out.println("Results fetched from List :");
        for (Map.Entry<Event, Horse> entry : winners.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

//        for (Iterator<Tuple2<Event, Integer>> it = roundTrip.toLocalIterator(); it.hasNext();) {
//            Tuple2<Event, Integer> t = (Tuple2<Event, Integer>) it.next();
//            System.out.println(t._1 + ": " + t._2);
//        }

    }

}
