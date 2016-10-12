package com.betleopard.domain;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
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
    public void testSimpleWonBet() {
        final Race r = TestUtils.makeSimpleRace();
        final Horse h = r.findRunnerByID(2);
        final Leg l = new Leg(r, h, OddsType.FIXED_ODDS, 1.0);
        r.winner(h);
        assertEquals("", 4.0, l.payout(), TestUtils.EPSILON);
    }

    @Test
    public void testSimpleLostBet() {
        final Race r = TestUtils.makeSimpleRace();
        final Horse h = r.findRunnerByID(2);
        final Leg l = new Leg(r, h, OddsType.FIXED_ODDS, 1.0);
        final Horse outsider = r.findRunnerByID(3);
        r.winner(outsider);
        assertEquals("", 0.0, l.payout(), TestUtils.EPSILON);
    }

    @Test
    public void testSimpleWonSPBetNoMove() {
        final Race r = TestUtils.makeSimpleRace();
        final Horse h = r.findRunnerByID(2);
        final Leg l = new Leg(r, h, OddsType.STARTING_PRICE, 1.0);
        r.winner(h);
        assertEquals("", 4.0, l.payout(), TestUtils.EPSILON);
    }

    @Test
    public void testSimpleWonSPBetWithMove() {
        final Race r = TestUtils.makeSimpleRace();
        final Horse h = r.findRunnerByID(2);
        final Leg l = new Leg(r, h, OddsType.STARTING_PRICE, 1.0);
        r.newOdds(TestUtils.makeSimpleOverBook());
        r.winner(h);
        assertEquals("", 1.5, l.payout(), TestUtils.EPSILON);
    }

    @Test
    public void testAcker() {
        final Race r = TestUtils.makeSimpleRace();
        final Horse h = r.findRunnerByID(2);
        final Leg l = new Leg(r, h, OddsType.FIXED_ODDS, 2.0);
        final Race r2 = TestUtils.makeSimpleRace();
        final Horse h2 = r.findRunnerByID(3);
        final Leg l2 = new Leg(r2, h2, OddsType.FIXED_ODDS, null);
        
        final Bet.BetBuilder bb = new Bet.BetBuilder();
        final Bet acc = bb.stake(2.0).id(42).type(BetType.ACCUM).addLeg(l).addLeg(l2).build();
        r.winner(h);
        r2.winner(h2);
        assertEquals("", 1.5, acc.payout(), TestUtils.EPSILON);
        
    }
}
