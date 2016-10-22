package com.betleopard.domain;

import com.betleopard.JSONSerializable;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestBet {

    @BeforeClass
    public static void setup() {
        TestUtils.setupHorses();
    }

    @Test
    public void testAckerWon() {
        final Race r = TestUtils.makeSimpleRace();
        final Horse h = r.findRunnerByID(2);
        final Leg l = new Leg(r, h, OddsType.FIXED_ODDS, null);
        final Race r2 = TestUtils.makeSimpleRace();
        final Horse h2 = r.findRunnerByID(3);
        final Leg l2 = new Leg(r2, h2, OddsType.FIXED_ODDS, null);

        final Bet.BetBuilder bb = new Bet.BetBuilder();
        final Bet acc = bb.stake(2.0).id(42).type(BetType.ACCUM).addLeg(l).addLeg(l2).build();
        r.winner(h);
        r2.winner(h2);
        assertEquals("", 40.0, acc.payout(), TestUtils.EPSILON);

    }

    @Test
    public void testAckerLost() {
        final Race r = TestUtils.makeSimpleRace();
        final Horse h = r.findRunnerByID(2);
        final Leg l = new Leg(r, h, OddsType.FIXED_ODDS, null);
        final Race r2 = TestUtils.makeSimpleRace();
        final Horse h2 = r.findRunnerByID(3);
        final Leg l2 = new Leg(r2, h2, OddsType.FIXED_ODDS, null);

        final Bet.BetBuilder bb = new Bet.BetBuilder();
        final Bet acc = bb.stake(2.0).id(42).type(BetType.ACCUM).addLeg(l).addLeg(l2).build();
        r.winner(h);
        r2.winner(h);
        assertEquals("", 0.0, acc.payout(), TestUtils.EPSILON);
    }

    @Test
    public void testSerialize() throws Exception {
        final Race r = TestUtils.makeSimpleRace();
        final Horse h = r.findRunnerByID(2);
        final Leg l = new Leg(r, h, OddsType.FIXED_ODDS, 1.0);

        final Bet.BetBuilder bb = new Bet.BetBuilder();
        final Bet acc = bb.stake(2.0).id(42).type(BetType.SINGLE).addLeg(l).build();
        assertEquals("{\"id\":42,\"legs\":[{\"race\":1,\"backing\":2,\"oddsVersion\":0,\"oddsType\":\"FIXED_ODDS\",\"odds\":4.0,\"stake\":1.0}],\"stake\":2.0,\"type\":\"SINGLE\"}", acc.toJSONString());
    }

    @Test
    @Ignore
    public void testParse() throws Exception {
//        {"id":42,"legs":[{"race":1,"backing":2,"oddsVersion":0,"oddsType":"FIXED_ODDS","odds":4.0,"stake":1.0}],"stake":2.0,"type":"SINGLE"}
        final String s = "{\"id\":42,\"legs\":[{\"race\":1,\"backing\":2,\"oddsVersion\":0,\"oddsType\":\"FIXED_ODDS\",\"odds\":4.0,\"stake\":1.0}],\"stake\":2.0,\"type\":\"SINGLE\"}";
        Bet b = JSONSerializable.parse(s, Bet::parseBlob);
        assertNotNull(b);
        assertEquals(42, b.getID());
        assertEquals(2.0, b.getStake(), TestUtils.EPSILON);
        assertEquals(BetType.SINGLE, b.getType());
        for (final Leg l : b.getLegs()) {
            assertNotNull(l);
            assertEquals(TestUtils.makeSimpleRace(), l.getRace());
        }
//        assertEquals(s, b.toJSONString());

    }
}
