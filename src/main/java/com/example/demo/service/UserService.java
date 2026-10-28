package com.example.demo.service;

import java.util.List;
import java.util.Locale;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;

/**
 * Mocked user directory exposed as MCP tools.
 */
@Service
public class UserService {

    private static final List<User> USERS = List.of(
            new User("U-1001", "Ada", "Lovelace", "ada.lovelace@example.com", "Engineering", "Platform Engineer", true),
            new User("U-1002", "Grace", "Hopper", "grace.hopper@example.com", "Engineering", "Compiler Engineer", true),
            new User("U-1003", "Alan", "Turing", "alan.turing@example.com", "Research", "Research Scientist", true),
            new User("U-1004", "Katherine", "Johnson", "katherine.johnson@example.com", "Research", "Data Analyst", false),
            new User("U-1005", "Sofia", "Marino", "sofia.marino@example.com", "Sales", "Account Executive", true),
            new User("U-1006", "Diego", "Ramirez", "diego.ramirez@example.com", "Support", "Support Specialist", true),
            new User("U-1007", "Mei", "Chen", "mei.chen@example.com", "Engineering", "Frontend Engineer", true),
            new User("U-1008", "Omar", "Haddad", "omar.haddad@example.com", "Marketing", "Growth Marketer", false));

    @McpTool(
            name = "getAllUserList",
            description = "Returns every user of the demo directory as a JSON array. "
                    + "Each user has id, firstName, lastName, email, department, jobTitle and active fields.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public List<User> getAllUserList() {
        return USERS;
    }

    @McpTool(
            name = "getUsers",
            description = "Searches the demo user directory. The query is matched (case-insensitively) "
                    + "against first name, last name, full name, email, department and job title. "
                    + "Returns the matching users, possibly an empty array.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public List<User> getUsers(
            @McpToolParam(description = "Free text to search for, e.g. 'engineering', 'ada' or 'example.com'",
                    required = true) String query) {

        if (query == null || query.isBlank()) {
            return getAllUserList();
        }

        String needle = query.strip().toLowerCase(Locale.ROOT);
        return USERS.stream()
                .filter(user -> matches(user, needle))
                .toList();
    }

    @McpTool(
            name = "getUserById",
            description = "Returns a single user of the demo directory by its exact id (for example 'U-1001'). "
                    + "Fails with an error message when no user with that id exists.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public User getUserById(
            @McpToolParam(description = "User id, e.g. U-1001", required = true) String id) {

        return USERS.stream()
                .filter(user -> user.id().equalsIgnoreCase(id.strip()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No user found with id '%s'. Known ids: %s"
                        .formatted(id, USERS.stream().map(User::id).toList())));
    }

    private boolean matches(User user, String needle) {
        return user.firstName().toLowerCase(Locale.ROOT).contains(needle)
                || user.lastName().toLowerCase(Locale.ROOT).contains(needle)
                || user.fullName().toLowerCase(Locale.ROOT).contains(needle)
                || user.email().toLowerCase(Locale.ROOT).contains(needle)
                || user.department().toLowerCase(Locale.ROOT).contains(needle)
                || user.jobTitle().toLowerCase(Locale.ROOT).contains(needle);
    }

}
