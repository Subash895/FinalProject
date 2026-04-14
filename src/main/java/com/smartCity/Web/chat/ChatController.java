package com.smartCity.Web.chat;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.auth.JwtUserPrincipal;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @GetMapping("/history")
  public ResponseEntity<ChatDtos.ChatHistoryResponse> history(
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    return ResponseEntity.ok(chatService.getHistory(principal.id()));
  }

  @PostMapping("/message")
  public ResponseEntity<ChatDtos.ChatReplyResponse> message(
      @AuthenticationPrincipal JwtUserPrincipal principal,
      @RequestBody ChatDtos.ChatRequest request) {
    return ResponseEntity.ok(chatService.sendMessage(principal.id(), request.message()));
  }

  @DeleteMapping("/history")
  public ResponseEntity<ChatDtos.ChatHistoryResponse> clear(
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    return ResponseEntity.ok(chatService.clearHistory(principal.id()));
  }
}
