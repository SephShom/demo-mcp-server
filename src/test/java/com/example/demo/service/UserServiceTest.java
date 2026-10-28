package com.example.demo.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.demo.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private final UserService userService = new UserService();

    @Test
    void getAllUserListReturnsEveryMockedUser() {
        List<User> users = userService.getAllUserList();

        assertThat(users).hasSize(8);
        assertThat(users).extracting(User::id).contains("U-1001", "U-1008");
    }

    @Test
    void getUsersMatchesDepartmentCaseInsensitively() {
        List<User> users = userService.getUsers("engineering");

        assertThat(users).extracting(User::department).containsOnly("Engineering");
        assertThat(users).hasSize(3);
    }

    @Test
    void getUsersReturnsEmptyListWhenNothingMatches() {
        assertThat(userService.getUsers("does-not-exist")).isEmpty();
    }

    @Test
    void getUserByIdReturnsTheMatchingUser() {
        assertThat(userService.getUserById("u-1004").lastName()).isEqualTo("Johnson");
    }

    @Test
    void getUserByIdFailsForUnknownId() {
        assertThatThrownBy(() -> userService.getUserById("U-9999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("U-9999");
    }
}
