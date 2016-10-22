package com.betleopard.hazelcast;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import com.betleopard.simple.SimpleFactory;
import com.betleopard.simple.SimpleHorseFactory;
import java.util.Iterator;
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
        final SparkConf conf = new SparkConf();
        final JavaSparkContext sc = new JavaSparkContext("local", "appname", conf);

        final JavaRDD<String> eventsText = sc.textFile("/tmp/historical_races.json");
        final JavaRDD<Event> events
                = eventsText.map(s -> JSONSerializable.parse(s, Event::parseBlob));

        final JavaPairRDD<Horse, Long> winners
                = events.mapToPair(e -> new Tuple2<>(e.getRaces().get(0).getWinner().orElse(Horse.PALE), 1L));
        final JavaPairRDD<Horse, Long> multipleWinners
                = winners.reduceByKey((a, b) -> a + b)
                         .filter(t -> t._2 > 1);

        System.out.println("Multiple Winners from List :");
        for (Iterator<Tuple2<Horse, Long>> it = multipleWinners.toLocalIterator(); it.hasNext();) {
            Tuple2<Horse, Long> t = (Tuple2<Horse, Long>) it.next();
            System.out.println(t._1 + ": " + t._2);
        }

        sc.stop();
    }
}
