package com.betleopard;

import com.betleopard.domain.Bet;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spark.connector.HazelcastSparkContext;
import static com.hazelcast.spark.connector.HazelcastJavaPairRDDFunctions.javaPairRddFunctions;
import com.hazelcast.spark.connector.rdd.HazelcastRDDFunctions;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import scala.Tuple2;

/**
 *
 * @author ben
 */
public class Main {

    public static void main(String[] args) {
        final Main main = new Main();
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

        final JavaRDD<String> betsText = sc.textFile(null);
        final JavaRDD<Bet> words = betsText.map(Bet::parse);
        
        
        final JavaPairRDD<Bet, Integer> pairs = words.mapToPair(s -> {
            return new Tuple2<Bet, Integer>(s, 1);
        });
        final JavaPairRDD<Bet, Integer> counts = pairs.reduceByKey((a, b) -> {
            return a + b;
        });

        HazelcastRDDFunctions tmp = javaPairRddFunctions(counts);
        tmp.saveToHazelcastMap("counts");

        final HazelcastInstance client = HazelcastClient.newHazelcastClient();
        final IMap<Object, Object> countsMap = client.getMap("counts");

        System.out.println("Results fetched from Hazelcast Map :");
        for (Map.Entry<Object, Object> entry : countsMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        client.getLifecycleService().terminate();
        sc.stop();
    }

}
