package com.smartCity.Web.comment;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.smartCity.Web.forum.ForumPost;
import com.smartCity.Web.notification.EmailNotificationService;
import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock private CommentRepository commentRepository;
  @Mock private JavaMailSender javaMailSender;

  private CommentService commentService;
  private EmailNotificationService emailNotificationService;

  @BeforeEach
  void setUp() {
    emailNotificationService =
        new EmailNotificationService(new StaticObjectProvider<>(javaMailSender), "noreply@example.com");
    commentService = new CommentService(commentRepository, emailNotificationService);
  }

  @Test
  void saveSendsThankYouEmailForCommentAuthor() {
    User user = new User("Asha", "asha@example.com", "secret", Role.USER);
    ForumPost post = new ForumPost();
    post.setTitle("Smart Cities");
    Comment comment = new Comment(post, user, "Nice");
    when(commentRepository.save(comment)).thenReturn(comment);

    Comment saved = commentService.save(comment);

    assertSame(comment, saved);
    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void saveSkipsEmailWhenCommentHasNoUser() {
    Comment comment = new Comment();
    when(commentRepository.save(comment)).thenReturn(comment);

    Comment saved = commentService.save(comment);

    assertSame(comment, saved);
    verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
  }

  private static final class StaticObjectProvider<T> implements ObjectProvider<T> {
    private final T value;

    private StaticObjectProvider(T value) {
      this.value = value;
    }

    @Override
    public T getObject(Object... args) {
      return value;
    }

    @Override
    public T getIfAvailable() {
      return value;
    }

    @Override
    public T getIfUnique() {
      return value;
    }

    @Override
    public T getObject() {
      return value;
    }
  }
}
