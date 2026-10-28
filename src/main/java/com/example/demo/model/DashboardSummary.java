package com.example.demo.model;

/**
 * Aggregated figures across the mocked user directory and product catalogue.
 */
public record DashboardSummary(
        int totalUsers,
        int activeUsers,
        int totalProducts,
        int outOfStockProducts,
        int categories,
        double inventoryValue,
        String generatedAt) {
}
