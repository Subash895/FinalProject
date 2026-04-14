package com.smartCity.Web.chat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartCity.Web.business.Business;
import com.smartCity.Web.business.BusinessRepository;
import com.smartCity.Web.city.City;
import com.smartCity.Web.city.CityRepository;
import com.smartCity.Web.forum.ForumPost;
import com.smartCity.Web.forum.ForumPostRepository;
import com.smartCity.Web.news.News;
import com.smartCity.Web.news.NewsRepository;
import com.smartCity.Web.place.Place;
import com.smartCity.Web.place.PlaceRepository;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;

@Service
public class ChatService {

  private static final int HISTORY_LIMIT = 16;
  private static final int CONTEXT_LIMIT = 8;

  private final ChatMessageRepository chatMessageRepository;
  private final UserRepository userRepository;
  private final CityRepository cityRepository;
  private final PlaceRepository placeRepository;
  private final BusinessRepository businessRepository;
  private final NewsRepository newsRepository;
  private final ForumPostRepository forumPostRepository;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final String aiApiKey;
  private final String aiModel;
  private final String aiBaseUrl;

  public ChatService(
      ChatMessageRepository chatMessageRepository,
      UserRepository userRepository,
      CityRepository cityRepository,
      PlaceRepository placeRepository,
      BusinessRepository businessRepository,
      NewsRepository newsRepository,
      ForumPostRepository forumPostRepository,
      ObjectMapper objectMapper,
      @Value("${app.ai.api-key:}") String aiApiKey,
      @Value("${app.ai.model:gemini-2.5-flash}") String aiModel,
      @Value("${app.ai.base-url:https://generativelanguage.googleapis.com/v1beta/openai/}")
          String aiBaseUrl) {
    this.chatMessageRepository = chatMessageRepository;
    this.userRepository = userRepository;
    this.cityRepository = cityRepository;
    this.placeRepository = placeRepository;
    this.businessRepository = businessRepository;
    this.newsRepository = newsRepository;
    this.forumPostRepository = forumPostRepository;
    this.objectMapper = objectMapper;
    this.aiApiKey = aiApiKey;
    this.aiModel = aiModel;
    this.aiBaseUrl = aiBaseUrl;
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
  }

  @Transactional(readOnly = true)
  public ChatDtos.ChatHistoryResponse getHistory(Long userId) {
    return new ChatDtos.ChatHistoryResponse(userId, mapMessages(loadHistory(userId)));
  }

  @Transactional
  public ChatDtos.ChatReplyResponse sendMessage(Long userId, String message) {
    if (!StringUtils.hasText(message)) {
      throw new IllegalArgumentException("Message is required.");
    }

    User user =
        userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
    ChatMessage userMessage =
        chatMessageRepository.save(new ChatMessage(user, ChatMessageRole.USER, message.trim()));

    String reply = fetchAssistantReply(userId, userMessage.getContent());
    ChatMessage assistantMessage =
        chatMessageRepository.save(new ChatMessage(user, ChatMessageRole.ASSISTANT, reply));

    return new ChatDtos.ChatReplyResponse(
        userId,
        reply,
        mapMessages(loadHistory(userId)),
        assistantMessage.getCreatedAt());
  }

  @Transactional
  public ChatDtos.ChatHistoryResponse clearHistory(Long userId) {
    chatMessageRepository.deleteByUserId(userId);
    return new ChatDtos.ChatHistoryResponse(userId, List.of());
  }

  private List<ChatMessage> loadHistory(Long userId) {
    return chatMessageRepository.findByUserIdOrderByCreatedAtAscIdAsc(userId);
  }

  private List<ChatDtos.ChatMessageResponse> mapMessages(List<ChatMessage> messages) {
    return messages.stream()
        .map(
            message ->
                new ChatDtos.ChatMessageResponse(
                    message.getId(),
                    message.getRole().name().toLowerCase(),
                    message.getContent(),
                    message.getCreatedAt()))
        .toList();
  }

  private String fetchAssistantReply(Long userId, String prompt) {
    if (!StringUtils.hasText(aiApiKey)) {
      return "Chatbot is not configured yet. Add APP_AI_API_KEY in the .env file.";
    }

    try {
      String systemPrompt = buildSystemPrompt();
      String knowledgeContext = buildKnowledgeContext();
      List<Map<String, String>> messages = new ArrayList<>();
      messages.add(Map.of("role", "system", "content", systemPrompt + "\n\nProject data:\n" + knowledgeContext));

      List<ChatMessage> recentMessages =
          chatMessageRepository.findByUserIdOrderByCreatedAtDescIdDesc(
              userId, PageRequest.of(0, HISTORY_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
      List<ChatMessage> orderedRecent = recentMessages.stream().sorted(
              (left, right) -> {
                int createdAtComparison = left.getCreatedAt().compareTo(right.getCreatedAt());
                if (createdAtComparison != 0) {
                  return createdAtComparison;
                }
                return left.getId().compareTo(right.getId());
              })
          .toList();

      for (ChatMessage message : orderedRecent) {
        messages.add(
            Map.of(
                "role", message.getRole().name().toLowerCase(),
                "content", message.getContent()));
      }

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("model", aiModel);
      payload.put("temperature", 0.2);
      payload.put("messages", messages);

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(resolveChatCompletionsUrl()))
              .timeout(Duration.ofSeconds(60))
              .header("Authorization", "Bearer " + aiApiKey)
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        return mapProviderError(response);
      }

      JsonNode root = objectMapper.readTree(response.body());
      JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
      String content = contentNode.asText("").trim();
      if (StringUtils.hasText(content)) {
        return content;
      }
    } catch (IOException | InterruptedException ex) {
      if (ex instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
    }

    return "I can only answer with information available in this MySmartCity project, and I could not complete the request right now.";
  }

  private String resolveChatCompletionsUrl() {
    String normalizedBaseUrl = aiBaseUrl.endsWith("/") ? aiBaseUrl : aiBaseUrl + "/";
    return normalizedBaseUrl + "chat/completions";
  }

  private String mapProviderError(HttpResponse<String> response) {
    String providerMessage = extractProviderErrorMessage(response.body());

    return switch (response.statusCode()) {
      case 400 -> StringUtils.hasText(providerMessage)
          ? "The AI provider rejected the request: " + providerMessage
          : "The AI provider rejected the request.";
      case 401, 403 -> "The AI provider key is invalid or blocked. Check APP_AI_API_KEY.";
      case 404 -> "The AI provider endpoint or model is incorrect. Check APP_AI_BASE_URL and APP_AI_MODEL.";
      case 429 -> "The AI provider quota or rate limit has been reached. Check billing or usage limits.";
      default -> StringUtils.hasText(providerMessage)
          ? "The AI provider is unavailable: " + providerMessage
          : "The AI provider is temporarily unavailable. Please try again in a moment.";
    };
  }

  private String extractProviderErrorMessage(String responseBody) {
    if (!StringUtils.hasText(responseBody)) {
      return "";
    }

    try {
      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode errorNode = root.path("error");
      if (errorNode.isTextual()) {
        return errorNode.asText("").trim();
      }

      String message = errorNode.path("message").asText("").trim();
      if (StringUtils.hasText(message)) {
        return message;
      }

      String status = errorNode.path("status").asText("").trim();
      if (StringUtils.hasText(status)) {
        return status;
      }
    } catch (IOException ignored) {
      return "";
    }

    return "";
  }

  private String buildSystemPrompt() {
    return """
        You are the MySmartCity assistant for this project.
        Answer only with information found in the provided MySmartCity project data.
        Do not answer with outside knowledge, general facts, or made-up details.
        If the answer is not present in the provided project data, say that it is not available in the current project information.
        Keep replies concise, helpful, and focused on the project.
        """;
  }

  private String buildKnowledgeContext() {
    StringBuilder builder = new StringBuilder();
    builder.append("Cities count: ").append(cityRepository.count()).append('\n');
    appendCities(builder);
    builder.append('\n');
    builder.append("Places count: ").append(placeRepository.count()).append('\n');
    appendPlaces(builder);
    builder.append('\n');
    builder.append("Businesses count: ").append(businessRepository.count()).append('\n');
    appendBusinesses(builder);
    builder.append('\n');
    builder.append("News count: ").append(newsRepository.count()).append('\n');
    appendNews(builder);
    builder.append('\n');
    builder.append("Forum posts count: ").append(forumPostRepository.count()).append('\n');
    appendForumPosts(builder);
    builder.append('\n');
    builder.append("Current time: ").append(LocalDateTime.now()).append('\n');
    return builder.toString();
  }

  private void appendCities(StringBuilder builder) {
    List<City> cities =
        cityRepository.findAll(PageRequest.of(0, CONTEXT_LIMIT, Sort.by(Sort.Direction.DESC, "id")))
            .getContent();
    if (cities.isEmpty()) {
      builder.append("- No cities available.\n");
      return;
    }
    for (City city : cities) {
      builder
          .append("- [City #")
          .append(city.getId())
          .append("] ")
          .append(safe(city.getName()))
          .append(", ")
          .append(safe(city.getState()))
          .append(", ")
          .append(safe(city.getCountry()))
          .append('\n');
    }
  }

  private void appendPlaces(StringBuilder builder) {
    List<Place> places =
        placeRepository.findAll(PageRequest.of(0, CONTEXT_LIMIT, Sort.by(Sort.Direction.DESC, "id")))
            .getContent();
    if (places.isEmpty()) {
      builder.append("- No places available.\n");
      return;
    }
    for (Place place : places) {
      builder
          .append("- [Place #")
          .append(place.getId())
          .append("] ")
          .append(safe(place.getName()))
          .append(" | city: ")
          .append(place.getCity() != null ? safe(place.getCity().getName()) : "N/A")
          .append(" | category: ")
          .append(safe(place.getCategory()))
          .append(" | location: ")
          .append(safe(place.getLocation()))
          .append(" | description: ")
          .append(trim(place.getDescription()))
          .append('\n');
    }
  }

  private void appendBusinesses(StringBuilder builder) {
    List<Business> businesses =
        businessRepository.findAll(PageRequest.of(0, CONTEXT_LIMIT, Sort.by(Sort.Direction.DESC, "id")))
            .getContent();
    if (businesses.isEmpty()) {
      builder.append("- No businesses available.\n");
      return;
    }
    for (Business business : businesses) {
      builder
          .append("- [Business #")
          .append(business.getId())
          .append("] ")
          .append(safe(business.getName()))
          .append(" | address: ")
          .append(safe(business.getAddress()))
          .append(" | description: ")
          .append(trim(business.getDescription()))
          .append('\n');
    }
  }

  private void appendNews(StringBuilder builder) {
    List<News> newsItems =
        newsRepository.findAll(PageRequest.of(0, CONTEXT_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt")))
            .getContent();
    if (newsItems.isEmpty()) {
      builder.append("- No news available.\n");
      return;
    }
    for (News news : newsItems) {
      builder
          .append("- [News #")
          .append(news.getId())
          .append("] ")
          .append(safe(news.getTitle()))
          .append(" | city: ")
          .append(news.getCity() != null ? safe(news.getCity().getName()) : "N/A")
          .append(" | createdAt: ")
          .append(news.getCreatedAt())
          .append(" | content: ")
          .append(trim(news.getContent()))
          .append('\n');
    }
  }

  private void appendForumPosts(StringBuilder builder) {
    List<ForumPost> posts =
        forumPostRepository.findAll(PageRequest.of(0, CONTEXT_LIMIT, Sort.by(Sort.Direction.DESC, "id")))
            .getContent();
    if (posts.isEmpty()) {
      builder.append("- No forum posts available.\n");
      return;
    }
    for (ForumPost post : posts) {
      builder
          .append("- [Forum #")
          .append(post.getId())
          .append("] ")
          .append(safe(post.getTitle()))
          .append(" | content: ")
          .append(trim(post.getContent()))
          .append('\n');
    }
  }

  private String trim(String value) {
    String safeValue = safe(value);
    return safeValue.length() <= 260 ? safeValue : safeValue.substring(0, 257) + "...";
  }

  private String safe(String value) {
    return StringUtils.hasText(value) ? value.replaceAll("\\s+", " ").trim() : "N/A";
  }
}
