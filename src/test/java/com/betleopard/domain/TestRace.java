package com.betleopard.domain;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestRace {

    @BeforeClass
    public static void setup() {
        TestUtils.setupHorses();
    }

    @Test
    public void testRaceSerialize() throws Exception {
        final Race r = TestUtils.makeSimpleRace();
//        System.out.println(r.toJSONString());
        assertEquals("{\"id\":1,\"hasRun\":false,\"currentVersion\":{\"odds\":{\"Horse{name=Rolling Stone, id=2}\":4.0,\"Horse{name=Beach Boy, id=1}\":2.0,\"Horse{name=Speedwell, id=3}\":5.0},\"raceTime\":[2016,10,16,15,20],\"version\":0,\"runners\":[{\"id\":2},{\"id\":1},{\"id\":3}]},\"winner\":{\"present\":false}}", r.toJSONString());
    }
}
