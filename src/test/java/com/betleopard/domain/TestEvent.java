package com.betleopard.domain;

import com.betleopard.JSONSerializable;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestEvent {

    @Test
    public void testParse() throws Exception {
        final String s = "{\"id\":1,\"name\":\"CHELTENHAM 1924-03-14\",\"date\":{\"year\":1924,\"month\":\"MARCH\",\"chronology\":{\"id\":\"ISO\",\"calendarType\":\"iso8601\"},\"dayOfMonth\":14,\"dayOfWeek\":\"FRIDAY\",\"era\":\"CE\",\"dayOfYear\":74,\"leapYear\":true,\"monthValue\":3},\"races\":[{\"id\":1,\"hasRun\":true,\"currentVersion\":{\"odds\":{\"Horse{name=Not Red Splash, id=2}\":3.0,\"Horse{name=Red Splash, id=1}\":6.0},\"raceTime\":{\"hour\":12,\"minute\":30,\"second\":0,\"year\":1924,\"month\":\"MARCH\",\"dayOfMonth\":14,\"dayOfWeek\":\"FRIDAY\",\"dayOfYear\":74,\"monthValue\":3,\"nano\":0,\"chronology\":{\"id\":\"ISO\",\"calendarType\":\"iso8601\"}},\"version\":0,\"runners\":[{\"name\":\"Not Red Splash\",\"id\":2},{\"name\":\"Red Splash\",\"id\":1}]},\"winner\":{\"name\":\"Red Splash\",\"id\":1}}]}";
        final Event e = JSONSerializable.parse(s, Event::parseBlob);
        assertNotNull(e);
    }

}
