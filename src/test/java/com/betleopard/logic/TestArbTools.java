package com.betleopard.logic;

import com.betleopard.domain.Horse;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestArbTools {

    private static final double EPSILON = 0.00001;
    
    @Test
    public void testSimpleArb() {
        final Horse h1 = Horse.of("Beach Boy", 1);
        final Horse h2 = Horse.of("Rolling Stone", 2);
        final Horse h3 = Horse.of("Speedwell", 3);

        final Map<Horse, Double> odds = new HashMap<>();
        odds.put(h1, 2.0);
        odds.put(h2, 4.0);
        odds.put(h3, 5.0);
        assertEquals("Expected under round book", 0.05, ArbTools.arbPercent(odds), EPSILON);
    }
}
