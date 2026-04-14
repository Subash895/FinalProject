package com.smartCity.Web.chat;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
  List<ChatMessage> findByUserIdOrderByCreatedAtAscIdAsc(Long userId);

  List<ChatMessage> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);

  void deleteByUserId(Long userId);
}
