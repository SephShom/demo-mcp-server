package com.example.demo.service;

import java.time.Instant;
import java.util.DoubleSummaryStatistics;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Service;

import com.example.demo.model.DashboardSummary;
import com.example.demo.model.ServerInfo;

/**
 * Aggregated demo metrics exposed as MCP tools.
 */
@Service
public class DashboardService {

    static final int EXPOSED_TOOLS = 8;

    private final UserService userService;
    private final ProductService productService;

    public DashboardService(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @McpTool(
            name = "getDashboardSummary",
            description = "Returns aggregated demo figures: user counts, product counts, "
                    + "the number of categories and the total inventory value.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public DashboardSummary getDashboardSummary() {
        var users = userService.getAllUserList();
        var products = productService.getAllProducts();

        DoubleSummaryStatistics stats = products.stream()
                .mapToDouble(product -> product.price() * product.stock())
                .summaryStatistics();

        long categories = products.stream().map(product -> product.category()).distinct().count();

        return new DashboardSummary(
                users.size(),
                (int) users.stream().filter(user -> user.active()).count(),
                products.size(),
                (int) products.stream().filter(product -> product.stock() == 0).count(),
                (int) categories,
                Math.round(stats.getSum() * 100.0) / 100.0,
                Instant.now().toString());
    }

    @McpTool(
            name = "getServerInfo",
            description = "Returns technical information about this MCP server: name, version, "
                    + "Java version, supported transports and how many tools it exposes.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public ServerInfo getServerInfo() {
        return new ServerInfo(
                "demo-mcp-server",
                "1.0.0",
                System.getProperty("java.version"),
                "Streamable-HTTP (default), SSE (profile 'sse'), STDIO (profile 'stdio')",
                EXPOSED_TOOLS);
    }

}
