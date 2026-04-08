package com.smartCity.Web.news;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.news.NewsDtos;

@RestController
@RequestMapping("/api/news")
@CrossOrigin("*")
public class NewsController {

  private final NewsService service;
  private final ApiDtoMapper apiDtoMapper;

  public NewsController(NewsService service, ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.apiDtoMapper = apiDtoMapper;
  }

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
