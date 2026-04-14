package com.smartCity.Web.news;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.shared.ApiDtoMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/news")
@CrossOrigin("*")
@RequiredArgsConstructor
public class NewsController {

  private final NewsService service;
  private final ApiDtoMapper apiDtoMapper;

  @PostMapping
  public NewsDtos.NewsResponse create(@RequestBody NewsDtos.NewsRequest news) {
    return apiDtoMapper.toNewsResponse(service.createNews(apiDtoMapper.toNews(news)));
  }

  @GetMapping
  public List<NewsDtos.NewsResponse> getAll() {
    return service.getAllNews().stream()
        .map(apiDtoMapper::toNewsResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public NewsDtos.NewsResponse getById(@PathVariable Long id) {
    return service
        .getNewsById(id)
        .map(apiDtoMapper::toNewsResponse)
        .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
  }

  @PutMapping("/{id}")
  public NewsDtos.NewsResponse update(
      @PathVariable Long id, @RequestBody NewsDtos.NewsRequest news) {
    return apiDtoMapper.toNewsResponse(service.updateNews(id, apiDtoMapper.toNews(news)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.deleteNews(id);
  }
}
