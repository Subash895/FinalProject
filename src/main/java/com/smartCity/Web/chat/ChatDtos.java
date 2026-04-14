package com.smartCity.Web.chat;

import java.time.LocalDateTime;
import java.util.List;

public final class ChatDtos {

  private ChatDtos() {}

  public record ChatRequest(String message) {}

  public record ChatMessageResponse(Long id, String role, String content, LocalDateTime createdAt) {}

  public record ChatHistoryResponse(Long userId, List<ChatMessageResponse> messages) {}

  public record ChatReplyResponse(
      Long userId, String reply, List<ChatMessageResponse> messages, LocalDateTime repliedAt) {}
}
