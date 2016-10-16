package com.betleopard.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author kittylyst
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private final long id;
    private final String firstName;
    private final String lastName;
    
    public User(final long ID, final String first, final String last) {
        id = ID;
        firstName = first;
        lastName = last;
    }

    @JsonProperty
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty
    public String getLastName() {
        return lastName;
    }

    @JsonProperty
    public long getID() {
        return id;
    }
    
    @Override
    public String toString() {
        return "User{" + "firstName=" + firstName + ", lastName=" + lastName + '}';
    }
    
}
