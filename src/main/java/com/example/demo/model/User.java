package com.example.demo.model;

/**
 * A user of the demo directory.
 */
public record User(
        String id,
        String firstName,
        String lastName,
        String email,
        String department,
        String jobTitle,
        boolean active) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
