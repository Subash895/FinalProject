package com.smartCity.Web.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartCity.Web.business.BusinessRepository;
import com.smartCity.Web.city.CityRepository;
import com.smartCity.Web.forum.ForumPostRepository;
import com.smartCity.Web.news.NewsRepository;
import com.smartCity.Web.place.PlaceRepository;
import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock private ChatMessageRepository chatMessageRepository;
  @Mock private UserRepository userRepository;
  @Mock private CityRepository cityRepository;
  @Mock private PlaceRepository placeRepository;
  @Mock private BusinessRepository businessRepository;
  @Mock private NewsRepository newsRepository;
  @Mock private ForumPostRepository forumPostRepository;

  private ChatService chatService;

  @BeforeEach
  void setUp() {
    chatService =
        new ChatService(
            chatMessageRepository,
            userRepository,
            cityRepository,
            placeRepository,
            businessRepository,
            newsRepository,
            forumPostRepository,
            new ObjectMapper(),
            "",
            "test-model",
            "https://example.com");
  }

  @Test
  void getHistoryMapsStoredMessages() {
    User user = new User("Name", "user@example.com", "secret", Role.USER);
    ChatMessage message = new ChatMessage(user, ChatMessageRole.USER, "Hello");
    message.setId(1L);
    message.setCreatedAt(LocalDateTime.of(2026, 4, 30, 12, 0));
    when(chatMessageRepository.findByUserIdOrderByCreatedAtAscIdAsc(5L)).thenReturn(List.of(message));

    ChatDtos.ChatHistoryResponse response = chatService.getHistory(5L);

    assertEquals(5L, response.userId());
    assertEquals(1, response.messages().size());
    assertEquals("user", response.messages().getFirst().role());
    assertEquals("Hello", response.messages().getFirst().content());
  }

  @Test
  void sendMessageRejectsBlankInput() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(1L, "   "));

    assertEquals("Message is required.", exception.getMessage());
  }

  @Test
  void sendMessageUsesFallbackWhenAiKeyIsMissing() {
    User user = new User("Name", "user@example.com", "secret", Role.USER);
    user.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(chatMessageRepository.save(any(ChatMessage.class)))
        .thenAnswer(
            invocation -> {
              ChatMessage message = invocation.getArgument(0);
              if (message.getCreatedAt() == null) {
                message.setCreatedAt(LocalDateTime.now());
              }
              return message;
            });
    when(chatMessageRepository.findByUserIdOrderByCreatedAtAscIdAsc(1L))
        .thenReturn(List.of(new ChatMessage(user, ChatMessageRole.USER, "Hello")));

    ChatDtos.ChatReplyResponse response = chatService.sendMessage(1L, "  Hello  ");

    assertEquals(
        "Chatbot is not configured yet. Add APP_AI_API_KEY in the .env file.", response.reply());
    verify(chatMessageRepository)
        .save(argThat(message -> message.getRole() == ChatMessageRole.USER && "Hello".equals(message.getContent())));
    verify(chatMessageRepository)
        .save(
            argThat(
                message ->
                    message.getRole() == ChatMessageRole.ASSISTANT
                        && response.reply().equals(message.getContent())));
  }

  @Test
  void clearHistoryDeletesMessagesForUser() {
    ChatDtos.ChatHistoryResponse response = chatService.clearHistory(9L);

    assertEquals(9L, response.userId());
    assertEquals(0, response.messages().size());
    verify(chatMessageRepository).deleteByUserId(9L);
  }
}
