package com.betleopard.domain;

import com.betleopard.DomainFactory;
import java.time.LocalDate;

/**
 *
 * @author ben
 */
public final class CentralFactory {

    private static DomainFactory<Event> eventFactory;
    private static DomainFactory<Horse> horseFactory;
    private static DomainFactory<Race> raceFactory;

    public static void setHorses(final DomainFactory<Horse> inject) {
        horseFactory = inject;
    }

    public static Horse horseOf(long runnerID) {
        return horseFactory.getByID(runnerID);
    }

    public static Horse horseOf(final String name) {
        Horse out = horseFactory.getByName(name);
        if (out == null) {
            out = new Horse(name, horseFactory.getNext());
        }
        horseFactory.cacheIfSupported(out);

        return out;
    }

    public static DomainFactory<Horse> getHorseFactory() {
        return horseFactory;
    }

    public static void setRaces(final DomainFactory<Race> inject) {
        raceFactory = inject;
    }

    public static Race raceOf(long raceID) {
        return raceFactory.getByID(raceID);
    }

    public static DomainFactory<Race> getRaceFactory() {
        return raceFactory;
    }

    public static void setEvents(final DomainFactory<Event> inject) {
        eventFactory = inject;
    }

    public static Event eventOf(final String name, final LocalDate raceDay) {
        return new Event(eventFactory.getNext(), name, raceDay);
    }

    public static DomainFactory<Event> getEventFactory() {
        return eventFactory;
    }

}
