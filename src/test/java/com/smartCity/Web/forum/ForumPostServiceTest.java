package com.smartCity.Web.forum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartCity.Web.comment.CommentRepository;

@ExtendWith(MockitoExtension.class)
class ForumPostServiceTest {

  @Mock private ForumPostRepository forumPostRepository;
  @Mock private CommentRepository commentRepository;

  private ForumPostService forumPostService;

  @BeforeEach
  void setUp() {
    forumPostService = new ForumPostService(forumPostRepository, commentRepository);
  }

  @Test
  void createTrimsFieldsBeforeSaving() {
    ForumPost post = new ForumPost();
    post.setTitle("  Hello  ");
    post.setContent("  World  ");

    when(forumPostRepository.save(post)).thenReturn(post);

    ForumPost saved = forumPostService.create(post);

    assertSame(post, saved);
    assertEquals("Hello", post.getTitle());
    assertEquals("World", post.getContent());
  }

  @Test
  void createRejectsBlankTitle() {
    ForumPost post = new ForumPost();
    post.setTitle("   ");
    post.setContent("content");

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> forumPostService.create(post));

    assertEquals("Title required", exception.getMessage());
  }

  @Test
  void updateRejectsBlankContent() {
    ForumPost post = new ForumPost();
    post.setTitle("Title");
    post.setContent("   ");

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> forumPostService.update(1L, post));

    assertEquals("Content required", exception.getMessage());
  }

  @Test
  void deleteRemovesCommentsBeforePost() {
    when(forumPostRepository.existsById(8L)).thenReturn(true);

    forumPostService.delete(8L);

    verify(commentRepository).deleteByPostId(8L);
    verify(forumPostRepository).deleteById(8L);
  }

  @Test
  void updateTrimsNewValues() {
    ForumPost existing = new ForumPost();
    existing.setId(2L);
    when(forumPostRepository.findById(2L)).thenReturn(Optional.of(existing));
    when(forumPostRepository.save(existing)).thenReturn(existing);

    ForumPost update = new ForumPost();
    update.setTitle("  Updated  ");
    update.setContent("  Body  ");

    ForumPost saved = forumPostService.update(2L, update);

    assertSame(existing, saved);
    assertEquals("Updated", existing.getTitle());
    assertEquals("Body", existing.getContent());
    verify(forumPostRepository, never()).save(update);
  }
}
