package com.smartCity.Web.comment;

import java.time.LocalDateTime;

import com.smartCity.Web.forum.ForumDtos.ForumPostResponse;
import com.smartCity.Web.user.UserDtos.UserResponse;

/**
 * Groups the request and response DTOs used by the Comment API.
 */
public final class CommentDtos {

  private CommentDtos() {}

  public record PostRef(Long id) {}

  public record UserRef(Long id) {}

  public record CommentRequest(
      Long postId,
      PostRef post,
      Long userId,
      UserRef user,
      String content,
      LocalDateTime createdAt) {}

  public record CommentResponse(
      Long id,
      ForumPostResponse post,
      UserResponse user,
      String content,
      LocalDateTime createdAt) {}
}
