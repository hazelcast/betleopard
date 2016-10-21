package com.betleopard.examples;

import com.betleopard.domain.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Race;
import com.betleopard.domain.TestUtils;
import com.betleopard.simple.SimpleHorseFactory;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestMakeSimpleFiles {

    @BeforeClass
    public static void initialSetup() {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
    }

    @Test
    public void testDataCleansing() {
        final MakeSampleFiles msf = new MakeSampleFiles();
        final Event e = msf.makeSingleEvent("1978,Bachelor's Hall,N/A", MakeSampleFiles.MajorEvent.KING_GEORGE_V);
        final Race r = e.getRaces().get(0);
        assertTrue(r.getWinner().isPresent());
    }

}
