package com.betleopard.domain;

/**
 *
 * @author ben
 */
public class BetFactory {
    private static final ThreadLocal<Long> betId = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return 0L;
        }
    };

    public static Bet newBet() {
        return null;
    }
}
