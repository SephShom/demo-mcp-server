package com.example.demo.service;

import java.util.List;
import java.util.Locale;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import com.example.demo.model.Product;

/**
 * Mocked product catalogue exposed as MCP tools.
 */
@Service
public class ProductService {

    private static final List<Product> PRODUCTS = List.of(
            new Product("P-2001", "Aurora Laptop 14", "Computing", 1299.00, 24),
            new Product("P-2002", "Nova Laptop 16", "Computing", 1799.00, 12),
            new Product("P-2003", "Pulse Phone X", "Phones", 899.00, 40),
            new Product("P-2004", "Pulse Phone Lite", "Phones", 499.00, 65),
            new Product("P-2005", "Horizon Wireless Buds", "Accessories", 149.00, 120),
            new Product("P-2006", "Titan Mechanical Keyboard", "Accessories", 129.00, 0));

    @McpTool(
            name = "getAllProducts",
            description = "Returns the complete demo product catalogue as a JSON array. "
                    + "Each product has id, name, category, price and stock fields.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public List<Product> getAllProducts() {
        return PRODUCTS;
    }

    @McpTool(
            name = "getProductsByCategory",
            description = "Returns the demo products belonging to one category. "
                    + "Matching is case-insensitive and accepts partial names, "
                    + "for example 'phone' matches the category 'Phones'.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public List<Product> getProductsByCategory(
            @McpToolParam(description = "Product category, e.g. 'Computing', 'Phones' or 'Accessories'",
                    required = true) String category) {

        String needle = category.strip().toLowerCase(Locale.ROOT);
        return PRODUCTS.stream()
                .filter(product -> product.category().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    @McpTool(
            name = "getLowStockProducts",
            description = "Returns the demo products whose stock is at or below the given threshold, "
                    + "sorted from lowest to highest stock. Useful for restocking questions.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public List<Product> getLowStockProducts(
            @McpToolParam(description = "Maximum stock level to report, e.g. 25", required = true) int threshold) {

        return PRODUCTS.stream()
                .filter(product -> product.stock() <= threshold)
                .sorted((left, right) -> Integer.compare(left.stock(), right.stock()))
                .toList();
    }

}
