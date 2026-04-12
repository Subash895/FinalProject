package com.smartCity.Web.forum;

/**
 * Groups the request and response DTOs used by the Forum API.
 */
public final class ForumDtos {

  private ForumDtos() {}

  public record ForumPostRequest(String title, String content) {}

  public record ForumPostResponse(Long id, String title, String content) {}
}
