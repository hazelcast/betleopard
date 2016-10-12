package com.betleopard.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a betting slip, a top-level construct to hold 
 * 
 * @author kittylyst
 */
public class Slip {
    private final User bettor;
    private final LocalDateTime struckAt;
    // unique bet ID - use a ThreadLocal atomic?
    private final Set<Bet> bets = new HashSet<>();
    
    public Slip(final User user) {
        bettor = user;
        struckAt = LocalDateTime.now();
    }
}
