package com.example.demo.model;

/**
 * A product of the demo catalogue.
 */
public record Product(
        String id,
        String name,
        String category,
        double price,
        int stock) {
}
