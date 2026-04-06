package com.smartCity.Web.user;

import com.smartCity.Web.user.Role;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserRequest(String name, String email, String password, Role role) {
    }

    public record UserResponse(Long id, String name, String email, Role role) {
    }
}
