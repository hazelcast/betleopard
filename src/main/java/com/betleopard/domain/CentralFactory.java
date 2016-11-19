package com.betleopard.domain;

import com.betleopard.DomainFactory;
import com.betleopard.domain.Bet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 *
 * @author ben
 */
public final class CentralFactory {

    private static DomainFactory<Event> eventFactory;
    private static DomainFactory<Horse> horseFactory;
    private static DomainFactory<Race> raceFactory;
    private static DomainFactory<User> userFactory;
    private static DomainFactory<Bet.BetBuilder> betFactory;

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

    public static Race raceOf(final LocalDateTime time, final Map<Horse, Double> odds) {
        return Race.of(time, raceFactory.getNext(), odds);
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

    public static void setUsers(final DomainFactory<User> inject) {
        userFactory = inject;
    }

    public static User userOf(final String first, final String last) {
        return new User(eventFactory.getNext(), first, last);
    }

    public static DomainFactory<User> getUserFactory() {
        return userFactory;
    }

    public static Bet.BetBuilder betOf() {
        final Bet.BetBuilder out = new Bet.BetBuilder();
        out.id(betFactory.getNext());
        return out;
    }

    public static DomainFactory<Bet.BetBuilder> getBetFactory() {
        return betFactory;
    }

}
