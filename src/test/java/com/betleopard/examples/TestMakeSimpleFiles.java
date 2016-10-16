package com.betleopard.examples;

import com.betleopard.domain.Event;
import com.betleopard.domain.Race;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestMakeSimpleFiles {

    @Test
    public void testDataCleansing() {
        final MakeSampleFiles msf = new MakeSampleFiles();
        final Event e = msf.makeSingleEvent("1978,Bachelor's Hall,N/A", MakeSampleFiles.MajorEvent.KING_GEORGE_V);
        final Race r = e.getRaces().get(0);
        assertTrue(r.getWinner().isPresent());
    }
    
}
