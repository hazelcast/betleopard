package com.betleopard.hazelcast;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import com.betleopard.domain.Race;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.spark.connector.rdd.HazelcastRDDFunctions;
import static com.hazelcast.spark.connector.HazelcastJavaPairRDDFunctions.javaPairRddFunctions;
import java.util.HashSet;
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
public class LiveBetMain {

//    private HazelcastSparkContext ctx;
    private JavaSparkContext sc;

    public static void main(String[] args) {
        final HazelcastFactory<Horse> stable = HazelcastHorseFactory.getInstance();
        stable.init(Horse.class);
        CentralFactory.setHorses(stable);
        final HazelcastFactory<Race> raceFactory = new HazelcastFactory<>();
        raceFactory.init(Race.class);
        CentralFactory.setRaces(raceFactory);

        final LiveBetMain main = new LiveBetMain();
        main.init();
        System.out.println("------------------------------------------------------------------------------------------------------------------------------");
        main.createFutureEvents();
        main.run();
        main.stop();
    }

    private void init() {
        final SparkConf conf = new SparkConf()
                .set("hazelcast.server.addresses", "127.0.0.1:5701")
                .set("hazelcast.server.groupName", "dev")
                .set("hazelcast.server.groupPass", "dev-pass")
                .set("hazelcast.spark.valueBatchingEnabled", "true")
                .set("hazelcast.spark.readBatchSize", "5000")
                .set("hazelcast.spark.writeBatchSize", "5000")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        sc = new JavaSparkContext("local", "appname", conf);
//        ctx = new HazelcastSparkContext(sc);

        final JavaRDD<String> eventsText = sc.textFile("/tmp/historical_races.json");
        final JavaRDD<Event> events
                = eventsText.map(s -> JSONSerializable.parse(s, Event::parseBlob));

        final JavaPairRDD<Horse, Set<Event>> winners
                = events.mapToPair(e -> {
                    final Set<Event> evts = new HashSet<>();
                    evts.add(e);
                    return new Tuple2<>(e.getRaces().get(0).getWinner().orElse(Horse.PALE), evts);
                });

//        System.out.println("--------------------------- Writing: ");
//        System.out.println(winners.collectAsMap());

        final HazelcastRDDFunctions tmp = javaPairRddFunctions(winners);
        tmp.saveToHazelcastMap("winners");
    }

    public void stop() {
        sc.stop();
    }

    public void scrap() {
        // FIXME Do we now get this back again via a Hazelcast client?
        final HazelcastInstance client = HazelcastClient.newHazelcastClient();
        IMap<Horse, Event> countsMap = client.getMap("winners");

        client.getLifecycleService().terminate();
    }

    public void run() {
    }

    public void createFutureEvents() {
        final HazelcastInstance client = HazelcastClient.newHazelcastClient();
        final IMap<Object, Object> fromHC = client.getMap("winners");
        final Set<Object> winners = fromHC.keySet();
        System.out.println("--------------------------- Winners: ");
        System.out.println(winners);
    }

}
