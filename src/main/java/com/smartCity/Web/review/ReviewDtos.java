package com.smartCity.Web.review;

import java.time.LocalDateTime;

import com.smartCity.Web.user.UserDtos.UserResponse;

/**
 * Groups the request and response DTOs used by the Review API.
 */
public final class ReviewDtos {

  private ReviewDtos() {}

  public record ReviewRequest(
      ReviewTargetType targetType, Long targetId, Integer rating, String comment) {}

  public record ReviewResponse(
      Long id,
      UserResponse user,
      ReviewTargetType targetType,
      Long targetId,
      Integer rating,
      String comment,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {}
}
