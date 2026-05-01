package com.smartCity.Web.news;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartCity.Web.city.City;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

  @Mock private NewsRepository newsRepository;

  private NewsService newsService;

  @BeforeEach
  void setUp() {
    newsService = new NewsService(newsRepository);
  }

  @Test
  void updateOnlyOverridesOptionalFieldsWhenPresent() {
    City existingCity = new City();
    existingCity.setName("Ahmedabad");
    News existing = new News(existingCity, "Old", "Old content");
    existing.setId(1L);
    existing.setCreatedAt(LocalDateTime.of(2026, 4, 1, 10, 0));

    News incoming = new News();
    incoming.setTitle("New");
    incoming.setContent("Updated content");
    incoming.setCreatedAt(null);

    when(newsRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(newsRepository.save(existing)).thenReturn(existing);

    News saved = newsService.updateNews(1L, incoming);

    assertSame(existing, saved);
    assertEquals("Ahmedabad", existing.getCity().getName());
    assertEquals(LocalDateTime.of(2026, 4, 1, 10, 0), existing.getCreatedAt());
  }

  @Test
  void deleteRejectsMissingRecord() {
    when(newsRepository.existsById(2L)).thenReturn(false);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> newsService.deleteNews(2L));

    assertEquals("News not found with id: 2", exception.getMessage());
  }

  @Test
  void deleteRemovesExistingRecord() {
    when(newsRepository.existsById(3L)).thenReturn(true);

    newsService.deleteNews(3L);

    verify(newsRepository).deleteById(3L);
  }
}
