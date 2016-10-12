package com.betleopard.logic;

import com.betleopard.domain.Horse;
import com.betleopard.domain.TestUtils;
import static com.betleopard.domain.TestUtils.EPSILON;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestArbTools {

    @Test
    public void testSimpleArb() {
        final Map<Horse, Double> odds = TestUtils.makeSimple0_95UnderBook();
        assertEquals("Expected under round book", 0.05, ArbTools.arbPercent(odds), EPSILON);
    }
}
