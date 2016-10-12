package com.betleopard.domain;

/**
 *
 * @author ben
 */
public final class Horse {
    private final String name;
    private final long id;
    
    static Horse of(final String name, final long id) {
        return new Horse(name, id);
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
