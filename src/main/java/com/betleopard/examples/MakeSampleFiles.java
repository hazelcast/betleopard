package com.betleopard.examples;

import com.betleopard.CentralFactory;
import com.betleopard.domain.Event;
import com.betleopard.domain.Horse;
import com.betleopard.domain.Race;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.dayOfWeekInMonth;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author ben
 */
public final class MakeSampleFiles {

    private static final String DUMMY_PREFIX = "Not ";
    private static final Pattern SIMPLE_CSV_PATTERN = Pattern.compile("([^,]+),([^,]+),([^,]+)");
    private static final Pattern SCRUBBED_YEAR_PATTERN = Pattern.compile("^(\\d\\d\\d\\d)");
    private static final Pattern SCRUBBED_ODDS_PATTERN = Pattern.compile("^(\\d+)/(\\d+)");

    private static final Map<String, Horse> allWinners = new HashMap<>();

    private final AtomicLong raceCounter = new AtomicLong(1);

    public enum MajorEvent {

        CHELTENHAM("Cheltenham Gold Cup") {
                    // CGP : 2nd Fri in March
                    @Override
                    public TemporalAdjuster offsetForRace() {
                        return t -> t.with(firstDayOfYear())
                        .plus(2, ChronoUnit.MONTHS)
                        .with(dayOfWeekInMonth(2, DayOfWeek.FRIDAY));
                    }
                },
        GRAND_NATIONAL("Grand National") {
                    // GN : 2nd Sat in April
                    @Override
                    public TemporalAdjuster offsetForRace() {
                        return t -> t.with(firstDayOfYear())
                        .plus(3, ChronoUnit.MONTHS)
                        .with(dayOfWeekInMonth(2, DayOfWeek.SATURDAY));
                    }
                },
        KING_GEORGE_V("King George V") {
                    // KGV : Boxing Day
                    @Override
                    public TemporalAdjuster offsetForRace() {
                        return t -> t.with(firstDayOfYear())
                        .plus(11, ChronoUnit.MONTHS)
                        .plus(25, ChronoUnit.DAYS);
                    }
                };

        // Slightly simplified rules for when each race falls - we only have "year" 
        // data and need to generate day level data
        public abstract TemporalAdjuster offsetForRace();

        private final String name;
        public String getName() { return name; }
        
        private MajorEvent(final String s) {
            name = s;
        }
    }

    public static void main(final String[] args) throws IOException, URISyntaxException {
        final MakeSampleFiles msf = new MakeSampleFiles();
        final List<Event> events = msf.makeHistoricalEvents();
        msf.saveHistoricalData(events, args.length > 0? args[0] : "/tmp/historical_races.json");
    }

    public List<Event> makeHistoricalEvents() throws IOException, URISyntaxException {
        final List<String> courses = readLinesFromResource("courses.csv");

        final List<Event> goldCups = makeHistoricalEventsFromStaticData("cheltenham_simple_raw.csv", MajorEvent.CHELTENHAM);
        final List<Event> nationals = makeHistoricalEventsFromStaticData("grand_national_raw.csv", MajorEvent.GRAND_NATIONAL);
        final List<Event> kgvs = makeHistoricalEventsFromStaticData("king_george_v_raw.csv", MajorEvent.KING_GEORGE_V);
        final List<Event> out = new ArrayList<>();
        out.addAll(goldCups);
        out.addAll(nationals);
        out.addAll(kgvs);
        return out;
    }

    public void saveHistoricalData(final List<Event> events, final String fName) throws IOException {
        final Path p = Paths.get(fName);
        final List<String> serialized = 
                events.stream()
                      .map(e -> e.toJSONString())
                      .collect(Collectors.toList());
        Files.write(p, serialized);
    }
    
    public List<String> readLinesFromResource(final String rName) throws IOException, URISyntaxException {
        final Path p = Paths.get(getClass().getClassLoader().getResource(rName).toURI());
        return Files.readAllLines(p);
    }

    public List<Event> makeHistoricalEventsFromStaticData(final String rName, final MajorEvent ev) throws IOException, URISyntaxException {
        final List<Event> out = new ArrayList<>();
        final List<String> csvLines = readLinesFromResource(rName);

        for (final String line : csvLines) {
            out.add(makeSingleEvent(line, ev));
        }

        return out;
    }

    public Event makeSingleEvent(final String line, final MajorEvent ev) {
        final Matcher m = SIMPLE_CSV_PATTERN.matcher(line);
        if (m.find()) {
            final String rawYear = m.group(1);
            final LocalDate raceDay = makeRaceDay(rawYear, ev);
            final String winner = m.group(2);
            final Double odds = makeOdds(m.group(3));
//            System.out.println(winner + " won " + ev + " on " + raceDay + " at odds " + (odds == null ? " N/A " : odds));
            return makeEvent(ev, raceDay, winner, odds);
        } else {
            throw new IllegalArgumentException(line + " does not match, and it should");
        }

    }

    public LocalDate makeRaceDay(final String rawYear, final MajorEvent ev) {
        final Matcher m = SCRUBBED_YEAR_PATTERN.matcher(rawYear);
        if (m.find()) {
            final String year = m.group(1);
            final LocalDate newYearsDay = LocalDate.of(Integer.parseInt(year), Month.JANUARY, 1);
            return newYearsDay.with(ev.offsetForRace());
        } else {
            throw new IllegalArgumentException(ev + " in " + rawYear + " does not match, and it should");
        }
    }

    public Double makeOdds(final String rawOdds) {
        final Matcher m = SCRUBBED_ODDS_PATTERN.matcher(rawOdds);
        if (m.find()) {
            final int num = Integer.parseInt(m.group(1));
            final int denom = Integer.parseInt(m.group(2));
            return 1 + ((double) num) / denom;
        } else {
            return null;
        }
    }

    public Event makeEvent(final MajorEvent ev, final LocalDate raceDay, final String winner, final Double winningOdds) {
        if (allWinners.get(winner) == null) {
            allWinners.put(winner, CentralFactory.newHorse(winner));
            // Just to put another runner in the race
            final String dummy = DUMMY_PREFIX + winner;
            allWinners.put(dummy, CentralFactory.newHorse(dummy));
        }
        final Horse won = allWinners.get(winner);
        final Horse lost = allWinners.get(DUMMY_PREFIX + winner);
        final Map<Horse, Double> odds = new HashMap<>();
        if (winningOdds == null) {
            odds.put(won, 1.5);
            odds.put(lost, 20.0);
        } else {
            odds.put(won, winningOdds);
            odds.put(lost, 3.0); // dummy, unrealistic value that will usually produce an under book 
        }
        final Race r = Race.of(raceDay.atTime(12, 30), raceCounter.getAndIncrement(), odds);
        r.winner(won);
        // Cheat and keep the race & event IDs in step...
        final Event e = new Event(r.getID(), ev + " " + raceDay, raceDay);
        e.addRace(r);
        return e;
    }

}
