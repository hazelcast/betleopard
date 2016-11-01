package com.betleopard.hazelcast;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import com.betleopard.simple.SimpleFactory;
import com.betleopard.simple.SimpleHorseFactory;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 *
 * @author ben
 */
public class AnalysisSpark {

    public static void main(String[] args) {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
        CentralFactory.setRaces(new SimpleFactory<>());
        final AnalysisSpark main = new AnalysisSpark();
        main.run();
    }

    private void run() {
        // Use Kryo to allow us to serialize java 8 Optional<T>
        final SparkConf conf = new SparkConf();
//        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        
        final JavaSparkContext sc = new JavaSparkContext("local", "appname", conf);

        final JavaRDD<String> eventsText = sc.textFile("/tmp/historical_races.json");
        final JavaRDD<Event> events
                = eventsText.map(s -> JSONSerializable.parse(s, Event::parseBlob));

//        final JavaPairRDD<Horse, Integer> winners
//                = events.mapToPair(e -> new Tuple2<>(e.getRaces().get(0).getWinner().orElse(Horse.PALE), 1));
//        final JavaPairRDD<Horse, Integer> multipleWinners
//                = winners.reduceByKey((a, b) -> a + b)
//                         .filter(t -> t._2 > 1);

        final JavaPairRDD<Horse, Set<Event>> winners
                = events.mapToPair(e -> {
                    final Set<Event> evts = new HashSet<>();
                    evts.add(e);
                    return new Tuple2<>(e.getRaces().get(0).getWinner().orElse(Horse.PALE), evts);
                });
        final JavaPairRDD<Horse, Set<Event>> inverted
                = winners.reduceByKey((e1, e2) -> {
                    e1.addAll(e2);
                    return e1;
                });
        
        final JavaPairRDD<Horse, Integer> withWinCount
                = inverted.mapToPair(t -> new Tuple2<>(t._1, t._2.size()));
        
        final JavaPairRDD<Horse, Integer> multipleWinners
                = withWinCount.filter(t -> t._2 > 1);

        System.out.println("Multiple Winners from List :");
        for (Iterator<Tuple2<Horse, Integer>> it = multipleWinners.toLocalIterator(); it.hasNext();) {
            Tuple2<Horse, Integer> t = (Tuple2<Horse, Integer>) it.next();
            System.out.println(t._1 + ": " + t._2);
        }

        sc.stop();
    }
}
