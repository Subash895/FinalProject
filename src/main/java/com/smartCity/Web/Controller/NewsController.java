package com.smartCity.Web.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.Model.News;
import com.smartCity.Web.Service.NewsService;

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
}