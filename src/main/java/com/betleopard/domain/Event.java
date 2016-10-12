package com.betleopard.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ben
 */
public class Event {
    private LocalDateTime start;
    private LocalDateTime finish;
    private final List<Race.RaceBody> races = new ArrayList<>();
    
}
