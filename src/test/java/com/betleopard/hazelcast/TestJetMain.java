package com.betleopard.hazelcast;

import com.betleopard.JSONSerializable;
import com.betleopard.domain.*;
import com.betleopard.domain.Bet.BetBuilder;
import static com.betleopard.hazelcast.JetBetMain.USER_ID;
import static com.betleopard.hazelcast.JetBetMain.WORST_ID;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import static java.time.temporal.TemporalAdjusters.next;
import java.util.Map;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestJetMain {

    private HazelcastInstance client;
    private JetInstance jet;

    @Before
    public void setup() throws Exception {
        CentralFactory.setHorses(HazelcastHorseFactory.getInstance());
        CentralFactory.setRaces(new HazelcastFactory<>(Race.class));
        CentralFactory.setEvents(new HazelcastFactory<>(Event.class));
        CentralFactory.setUsers(new HazelcastFactory<>(User.class));
        CentralFactory.setBets(new HazelcastFactory<>(Bet.class));

        jet = Jet.newJetInstance();

//        final ClientConfig config = new ClientConfig();
//        client = HazelcastClient.newHazelcastClient(config);
    }

    @Test
    public void testBuildDAG() throws Exception {
//        final Map<Long, User> users = jet.getMap(USER_ID);
        User u = makeUser();
        Bet b = makeBet();
        u.addBet(b);
        assertNotNull(b);
        try {
            jet.newJob(JetBetMain.buildDag()).execute().get();
            jet.getMap(WORST_ID);
        } finally {
            Jet.shutdownAll();
        }
    }

    private User makeUser() {
        return new User(1L, "Ben", "Evans");
    }

    private Bet makeBet() {
        String raw = "{\"id\":1,\"name\":\"CHELTENHAM 1924-03-14\",\"date\":{\"year\":1924,\"month\":\"MARCH\",\"dayOfMonth\":14,\"dayOfWeek\":\"FRIDAY\",\"era\":\"CE\",\"dayOfYear\":74,\"leapYear\":true,\"monthValue\":3,\"chronology\":{\"id\":\"ISO\",\"calendarType\":\"iso8601\"}},\"races\":[{\"id\":1,\"hasRun\":true,\"currentVersion\":{\"odds\":{\"1\":6.0,\"2\":3.0},\"raceTime\":{\"nano\":0,\"hour\":12,\"minute\":30,\"second\":0,\"year\":1924,\"month\":\"MARCH\",\"dayOfMonth\":14,\"dayOfWeek\":\"FRIDAY\",\"dayOfYear\":74,\"monthValue\":3,\"chronology\":{\"id\":\"ISO\",\"calendarType\":\"iso8601\"}},\"version\":0,\"runners\":[{\"name\":\"Red Splash\",\"id\":1},{\"name\":\"Not Red Splash\",\"id\":2}]},\"winner\":{\"name\":\"Red Splash\",\"id\":1}}]}";

        Event cheltenam04 = JSONSerializable.parse(raw, Event::parseBag);
        Map<Horse, Double> odds = cheltenam04.getRaces().get(0).currentOdds();

        // Now set up a new event based on the dummy one
        final LocalDate nextSat = LocalDate.now().with(next(DayOfWeek.SATURDAY));
        final LocalTime raceTime = LocalTime.of(11, 0); // 1100 start

        Event e = CentralFactory.eventOf("Test Next Saturday", nextSat);
        Race r = CentralFactory.raceOf(LocalDateTime.of(nextSat, raceTime), odds);
        e.addRace(r);

        BetBuilder bb = CentralFactory.betOf();
        Leg l = new Leg(r, r.findRunnerByID(0), OddsType.FIXED_ODDS, 2.0);
        
        return bb.stake(2).addLeg(l).build();
    }

}
