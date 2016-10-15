package com.betleopard.domain;

/**
 *
 * @author ben
 */
public final class Horse {

    private final String name;
    private final long id;

    public static Horse of(final String name, final long id) {
        return new Horse(name, id);
    }

    public static Horse of(long runner) {
        // Look it up in Hazelcast
        return null;
    }

    private Horse(final String name, final long id) {
        this.name = name;
        this.id = id;
    }

    public long getID() {
        return id;
    }

    @Override
    public String toString() {
        return "Horse{" + "name=" + name + ", id=" + id + '}';
    }
}
