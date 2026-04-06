package com.smartCity.Web.news;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.news.News;
import com.smartCity.Web.news.NewsRepository;

@Service
public class NewsService {

    @Autowired
    private NewsRepository repository;

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
        News existing = repository.findById(id)
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
}

