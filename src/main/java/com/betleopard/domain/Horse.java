package com.betleopard.domain;

import com.betleopard.JSONSerializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author ben
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Horse implements JSONSerializable {

    private final String name;
    private final long id;

    public static Horse of(final String name, final long id) {
        return new Horse(name, id);
    }

    private Horse(final String name, final long id) {
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
