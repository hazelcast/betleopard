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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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

    private JavaSparkContext sc;

    public static void main(String[] args) {
        final HazelcastFactory<Horse> stable = HazelcastHorseFactory.getInstance();
        CentralFactory.setHorses(stable);
        final HazelcastFactory<Race> raceFactory = new HazelcastFactory<>(Race.class);
        CentralFactory.setRaces(raceFactory);
        final HazelcastFactory<Event> eventFactory = new HazelcastFactory<>(Event.class);
        CentralFactory.setEvents(eventFactory);

        
        final LiveBetMain main = new LiveBetMain();
        main.init();
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
                .set("hazelcast.spark.writeBatchSize", "5000");

        sc = new JavaSparkContext("local", "appname", conf);

        final JavaRDD<String> eventsText = sc.textFile("/tmp/historical_races.json");
        final JavaRDD<Event> events
                = eventsText.map(s -> JSONSerializable.parse(s, Event::parseBlob));

        final JavaPairRDD<Horse, Integer> winners
                = events.mapToPair(e -> new Tuple2<>(e.getRaces().get(0).getWinner().orElse(Horse.PALE), 1))
                .reduceByKey((a, b) -> a + b);

        final HazelcastRDDFunctions accessToHC = javaPairRddFunctions(winners);
        accessToHC.saveToHazelcastMap("winners");

        createFutureEvents();
    }

    public void stop() {
        sc.stop();
    }

    public void run() {
    }

    public void createFutureEvents() {
        // Grab some horses to use as runners in races
        final HazelcastInstance client = HazelcastClient.newHazelcastClient();
        final IMap<Horse, Object> fromHC = client.getMap("winners");
        final Set<Horse> winners = fromHC.keySet();

        // Now set up some future-dated events for next Sat
        final LocalDate nextSat = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        
        
    }

}
