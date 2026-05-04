package com.smartCity.Web.user;

import com.smartCity.Web.user.Role;

/**
 * Groups the request and response DTOs used by the User API.
 */
public final class UserDtos {

  private UserDtos() {}

  public record UserRequest(String name, String email, String password, Role role) {}

  public record ProfileUpdateRequest(String name, String email, String password, Role role) {}

  public record UserResponse(Long id, String name, String email, Role role, String profilePhoto) {}
}
