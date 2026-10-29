package com.example.demo.service;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import com.example.demo.model.Product;

/**
 * Demo product catalogue exposed as MCP tools. The catalogue is seeded with mocked
 * products and mutated in-memory by {@link #addProduct}, so changes are lost on restart.
 */
@Service
public class ProductService {

    private final List<Product> products = new CopyOnWriteArrayList<>(List.of(
            new Product("P-2001", "Aurora Laptop 14", "Computing", 1299.00, 24),
            new Product("P-2002", "Nova Laptop 16", "Computing", 1799.00, 12),
            new Product("P-2003", "Pulse Phone X", "Phones", 899.00, 40),
            new Product("P-2004", "Pulse Phone Lite", "Phones", 499.00, 65),
            new Product("P-2005", "Horizon Wireless Buds", "Accessories", 149.00, 120),
            new Product("P-2006", "Titan Mechanical Keyboard", "Accessories", 129.00, 0)));

    @McpTool(
            name = "addProduct",
            description = "Adds a product to the demo catalogue and returns it with its generated id. "
                    + "The name must not be blank, the price must be zero or more (rounded to two decimals) "
                    + "and the stock must be zero or more. Duplicate names are allowed. "
                    + "The change is kept in memory only and is lost when the server restarts.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = false, destructiveHint = false,
                    idempotentHint = false, openWorldHint = false))
    public synchronized Product addProduct(
            @McpToolParam(description = "Product name, e.g. 'Orion Tablet 11'", required = true) String name,
            @McpToolParam(description = "Product category, e.g. 'Computing', 'Phones' or 'Accessories'",
                    required = true) String category,
            @McpToolParam(description = "Unit price, e.g. 399.90", required = true) double price,
            @McpToolParam(description = "Initial stock units, e.g. 15", required = true) int stock) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Product category must not be blank");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Product price must be zero or more, got %s".formatted(price));
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Product stock must be zero or more, got %s".formatted(stock));
        }

        Product product = new Product(nextProductId(), name.strip(), category.strip(),
                Math.round(price * 100.0) / 100.0, stock);
        products.add(product);
        return product;
    }

    @McpTool(
            name = "getProductById",
            description = "Returns a single demo product by its exact id (for example 'P-2001'). "
                    + "Fails with an error message when no product with that id exists.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, openWorldHint = false))
    public Product getProductById(
            @McpToolParam(description = "Product id, e.g. P-2001", required = true) String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product id must not be blank");
        }

        return products.stream()
                .filter(product -> product.id().equalsIgnoreCase(id.strip()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No product found with id '%s'. Known ids: %s"
                        .formatted(id, products.stream().map(Product::id).toList())));
    }

    @McpTool(
            name = "getAllProducts",
            description = "Returns the complete demo product catalogue as a JSON array. "
                    + "Each product has id, name, category, price and stock fields.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public List<Product> getAllProducts() {
        return products;
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
        return products.stream()
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

        return products.stream()
                .filter(product -> product.stock() <= threshold)
                .sorted((left, right) -> Integer.compare(left.stock(), right.stock()))
                .toList();
    }

    /**
     * Returns the next free id, e.g. {@code P-2007}, derived from the highest numeric
     * suffix currently present in the catalogue rather than from a separate counter.
     */
    private String nextProductId() {
        int highest = products.stream()
                .map(Product::id)
                .filter(id -> id != null && id.startsWith("P-"))
                .mapToInt(ProductService::suffixOf)
                .max()
                .orElse(0);
        return "P-" + (highest + 1);
    }

    private static int suffixOf(String id) {
        try {
            return Integer.parseInt(id.substring(2));
        }
        catch (NumberFormatException ex) {
            return 0;
        }
    }

}
