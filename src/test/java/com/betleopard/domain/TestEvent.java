package com.betleopard.domain;

import com.betleopard.JSONSerializable;
import com.betleopard.simple.SimpleHorseFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author kittylyst
 */
public class TestEvent {

    @Before
    public void initialSetup() {
        CentralFactory.setHorses(SimpleHorseFactory.getInstance());
    }

    @Test
    public void test_defect_KickingKing_20161022() throws Exception {
        final String winStr = "{\"id\":227,\"name\":\"KING_GEORGE_V 2004-12-26\",\"date\":{\"year\":2004,\"month\":\"DECEMBER\",\"dayOfMonth\":26,\"dayOfWeek\":\"SUNDAY\",\"era\":\"CE\",\"dayOfYear\":361,\"leapYear\":true,\"monthValue\":12,\"chronology\":{\"id\":\"ISO\",\"calendarType\":\"iso8601\"}},\"races\":[{\"id\":227,\"hasRun\":true,\"currentVersion\":{\"odds\":{\"129\":1.5,\"130\":20.0},\"raceTime\":{\"nano\":0,\"hour\":12,\"minute\":30,\"second\":0,\"year\":2004,\"month\":\"DECEMBER\",\"dayOfMonth\":26,\"dayOfWeek\":\"SUNDAY\",\"dayOfYear\":361,\"monthValue\":12,\"chronology\":{\"id\":\"ISO\",\"calendarType\":\"iso8601\"}},\"version\":0,\"runners\":[{\"name\":\"Not Kicking King\",\"id\":130},{\"name\":\"Kicking King\",\"id\":129}]},\"winner\":{\"name\":\"Kicking King\",\"id\":129}}]}";
        
        final Event event = JSONSerializable.parse(winStr, Event::parseBag);
        final Horse winner = event.getRaces().get(0).getWinner().orElse(Horse.PALE);
        assertNotEquals(Horse.PALE, winner);
    }

}
