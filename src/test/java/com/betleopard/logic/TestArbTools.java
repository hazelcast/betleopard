package com.betleopard.logic;

import com.betleopard.Utils;
import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Horse;
import com.betleopard.domain.TestUtils;
import static com.betleopard.domain.TestUtils.EPSILON;
import com.betleopard.simple.SimpleHorseFactory;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestArbTools {

    @BeforeClass
    public static void initialSetup() {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
        TestUtils.setupHorses();
    }

    @Test
    public void testSimpleArb() {
        final Map<Horse, Double> odds = TestUtils.makeSimple0_95UnderBook();
        assertEquals("Expected under round book", 0.05, Utils.arbPercent(odds), EPSILON);
    }
}
