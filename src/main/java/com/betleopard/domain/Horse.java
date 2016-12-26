package com.betleopard.domain;

import com.betleopard.JSONSerializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A simple representation of a horse running in a {@code Race}. Like other 
 * domain objects, it is immutable and {@code JSONSerializable}
 * 
 * @author kittylyst
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Horse implements JSONSerializable {

    // A constant that is used to implement "Broken Object" pattern
    public final static Horse PALE = new Horse("DEATH", Long.MIN_VALUE);
    
    private final String name;
    private final long id;

    public Horse(final String name, final long id) {
        this.name = name;
        this.id = id;
    }

    @JsonProperty
    public long getID() {
        return id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Horse{" + "name=" + name + ", id=" + id + '}';
    }
    
}
