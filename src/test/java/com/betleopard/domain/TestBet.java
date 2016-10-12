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

}
