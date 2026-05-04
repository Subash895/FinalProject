package com.smartCity.Web.news;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsService {
  private static final int MAX_NEWS_IMAGE_LENGTH = 3_000_000;

  private final NewsRepository repository;

  public News createNews(News news) {
    return repository.save(news);
  }

  public List<News> getAllNews() {
    return repository.findAll();
  }

  public Optional<News> getNewsById(Long id) {
    return repository.findById(id);
  }

  public News updateNews(Long id, News news) {
    News existing =
        repository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("News not found with id: " + id));

    existing.setTitle(news.getTitle());
    existing.setContent(news.getContent());

    if (news.getCity() != null) {
      existing.setCity(news.getCity());
    }
    if (news.getCreatedAt() != null) {
      existing.setCreatedAt(news.getCreatedAt());
    }

    return repository.save(existing);
  }

  public void deleteNews(Long id) {
    if (!repository.existsById(id)) {
      throw new RuntimeException("News not found with id: " + id);
    }
    repository.deleteById(id);
  }

  public News updateImage(Long id, byte[] imageData, String contentType) {
    News existing =
        repository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
    if (imageData == null || imageData.length == 0) {
      throw new RuntimeException("News image is required");
    }
    if (imageData.length > MAX_NEWS_IMAGE_LENGTH) {
      throw new RuntimeException("News image is too large");
    }
    existing.setImageData(imageData);
    existing.setImageContentType(contentType);
    return repository.save(existing);
  }
}
