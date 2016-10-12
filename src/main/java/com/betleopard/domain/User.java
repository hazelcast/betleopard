package com.betleopard.domain;

/**
 *
 * @author kittylyst
 */
public class User {

    private final String firstName;
    private final String lastName;
    
    public User(final String first, final String last) {
        firstName = first;
        lastName = last;
    }

    @Override
    public String toString() {
        return "User{" + "firstName=" + firstName + ", lastName=" + lastName + '}';
    }
    
}
