package com.smartCity.Web.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.News;
import com.smartCity.Web.Repository.NewsRepository;

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
}