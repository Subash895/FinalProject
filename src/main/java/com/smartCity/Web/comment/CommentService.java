package com.smartCity.Web.comment;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.smartCity.Web.notification.EmailNotificationService;
import lombok.RequiredArgsConstructor;

/**
 * Coordinates the business rules for Comment features before data is stored or returned.
 */
@Service
@RequiredArgsConstructor
public class CommentService {
  private final CommentRepository repo;
  private final EmailNotificationService emailNotificationService;

  public Comment save(Comment entity) {
    Comment savedComment = repo.save(entity);
    if (savedComment.getUser() != null) {
      emailNotificationService.sendCommentThankYou(
          savedComment.getUser().getEmail(),
          savedComment.getUser().getName(),
          resolveTargetLabel(savedComment));
    }
    return savedComment;
  }

  public List<Comment> getAll() {
    return repo.findAll();
  }

  public Optional<Comment> getById(Long id) {
    return repo.findById(id);
  }

  public Comment update(Long id, Comment entity) {
    entity.setId(id);
    return repo.save(entity);
  }

  public void delete(Long id) {
    repo.deleteById(id);
  }

  private String resolveTargetLabel(Comment comment) {
    if (comment.getPost() != null && comment.getPost().getTitle() != null) {
      return "forum post \"" + comment.getPost().getTitle() + "\"";
    }
    return "the discussion";
  }
}
