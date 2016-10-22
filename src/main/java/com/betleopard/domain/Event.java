package com.betleopard.domain;

import com.betleopard.JSONSerializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ben
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Event implements JSONSerializable {

    private final long id;
    private final String name;
    private final LocalDate date;
    private final List<Race> races = new ArrayList<>();

    public Event(final long eventID, final String raceName, final LocalDate raceDay) {
        id = eventID;
        name = raceName;
        date = raceDay;
    }

    @JsonProperty
    public long getID() {
        return id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public LocalDate getDate() {
        return date;
    }

    @JsonProperty
    public List<Race> getRaces() {
        return races;
    }

    public void addRace(final Race r) {
        races.add(r);
    }

    @Override
    public String toString() {
        return "Event{" + "id=" + id + ", name=" + name + ", date=" + date + ", races=" + races + '}';
    }

    public static Event parseBlob(final Map<String, ?> blob) {
        final long id = Long.parseLong("" + blob.get("id"));
        final String eventName = "" + blob.get("name");
        final Map<String, ?> dateBits = (Map<String, ?>)blob.get("date");
        final String year = ""+ dateBits.get("year");
        final String month = ""+ dateBits.get("monthValue");
        final String day = ""+ dateBits.get("dayOfMonth");
//        System.out.println(year+ "-"+ month +"-"+ day);
        
        final LocalDate eventDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
        final Event out = new Event(id, eventName, eventDate);
        final List<Map<String, ?>> racesBlob = (List<Map<String, ?>>)blob.get("races");
        for (final Map<String, ?> raceRaw : racesBlob) {
            out.addRace(Race.parseBlob(raceRaw));
        }
        
        return out;
    }

}
