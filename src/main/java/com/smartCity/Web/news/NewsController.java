package com.smartCity.Web.news;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.smartCity.Web.auth.jwt.JwtUserPrincipal;
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

  @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public NewsDtos.NewsResponse updateNewsPhoto(
      @PathVariable Long id,
      @RequestParam("photo") MultipartFile photo,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null || !"ADMIN".equals(principal.role())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required.");
    }
    if (photo == null || photo.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an image.");
    }
    try {
      byte[] bytes = photo.getBytes();
      if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed.");
      }
      String contentType = photo.getContentType();
      String safeContentType =
          contentType != null && contentType.startsWith("image/") ? contentType : "image/png";
      return apiDtoMapper.toNewsResponse(service.updateImage(id, bytes, safeContentType));
    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to upload news image.");
    }
  }
}
