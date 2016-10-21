package com.betleopard.hazelcast;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import static com.hazelcast.spark.connector.HazelcastJavaPairRDDFunctions.javaPairRddFunctions;
import com.hazelcast.spark.connector.HazelcastSparkContext;
import com.hazelcast.spark.connector.rdd.HazelcastJavaRDD;
import com.hazelcast.spark.connector.rdd.HazelcastRDDFunctions;
import java.util.Iterator;
import java.util.Map;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 *
 * @author ben
 */
public class AnalysisMain {

    public static void main(String[] args) {
        final AnalysisMain main = new AnalysisMain();
        main.run();
    }

    private void run() {
        final SparkConf conf = new SparkConf()
                .set("hazelcast.server.addresses", "127.0.0.1:5701")
                .set("hazelcast.server.groupName", "dev")
                .set("hazelcast.server.groupPass", "dev-pass")
                .set("hazelcast.spark.valueBatchingEnabled", "true")
                .set("hazelcast.spark.readBatchSize", "5000")
                .set("hazelcast.spark.writeBatchSize", "5000");

        final JavaSparkContext sc = new JavaSparkContext("local", "appname", conf);
        final HazelcastSparkContext ctx = new HazelcastSparkContext(sc);

        // FIXME 
        final JavaRDD<String> eventsText = sc.textFile("/tmp/historical_races.json");
        final JavaRDD<Event> events = 
                eventsText.map(s -> JSONSerializable.parse(s, Event::parseBlob));

        final JavaPairRDD<Horse, Event> winners = 
                events.mapToPair(e -> { 
                    return new Tuple2<Horse, Event>(e.getRaces().get(0).getWinner().get(), e); 
                });
        
//        final JavaPairRDD<Event, Integer> pairs = events.mapToPair(s -> {
//            return new Tuple2<Event, Integer>(s, 1);
//        });
//        final JavaPairRDD<Event, Integer> counts = pairs.reduceByKey((a, b) -> {
//            return a + b;
//        });

        final HazelcastRDDFunctions tmp = javaPairRddFunctions(winners);
        tmp.saveToHazelcastMap("counts");

//        boolean shutdown = false;
//        while (!shutdown) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//                shutdown = true;
//            }
//        }

        HazelcastJavaRDD<Event, Integer> roundTrip = ctx.fromHazelcastMap("counts");
//        System.out.println(""+ roundTrip.keys());

        System.out.println("Results fetched from HazelcastJavaRDD :");
        for (Iterator<Tuple2<Event, Integer>> it = roundTrip.toLocalIterator(); it.hasNext();) {
            Tuple2<Event, Integer> t = (Tuple2<Event, Integer>)it.next();
            System.out.println(t._1 + ": " + t._2);
        }


//        client.getLifecycleService().terminate();
        sc.stop();
    }

}
