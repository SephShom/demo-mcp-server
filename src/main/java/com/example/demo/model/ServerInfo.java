package com.example.demo.model;

/**
 * Basic information about this MCP server.
 */
public record ServerInfo(
        String serverName,
        String serverVersion,
        String javaVersion,
        String availableTransports,
        int exposedTools) {
}
