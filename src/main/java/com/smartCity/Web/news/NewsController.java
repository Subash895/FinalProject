package com.smartCity.Web.news;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.news.News;
import com.smartCity.Web.news.NewsService;

@RestController
@RequestMapping("/api/news")
@CrossOrigin("*")
public class NewsController {

    @Autowired
    private NewsService service;

    @PostMapping
    public News create(@RequestBody News news) {
        return service.createNews(news);
    }

    @GetMapping
    public List<News> getAll() {
        return service.getAllNews();
    }

    @GetMapping("/{id}")
    public News getById(@PathVariable Long id) {
        return service.getNewsById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
    }

    @PutMapping("/{id}")
    public News update(@PathVariable Long id, @RequestBody News news) {
        return service.updateNews(id, news);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteNews(id);
    }
}

