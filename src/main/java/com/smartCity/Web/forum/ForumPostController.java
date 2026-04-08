package com.smartCity.Web.forum;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.forum.ForumDtos;

@RestController
@RequestMapping("/api/forumposts")
@CrossOrigin(origins = "*")
public class ForumPostController {

  private final ForumPostService service;
  private final ApiDtoMapper apiDtoMapper;

  public ForumPostController(ForumPostService service, ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping
  public ResponseEntity<ForumDtos.ForumPostResponse> create(
      @RequestBody ForumDtos.ForumPostRequest post) {
    return ResponseEntity.ok(
        apiDtoMapper.toForumPostResponse(service.create(apiDtoMapper.toForumPost(post))));
  }

  @GetMapping
  public ResponseEntity<List<ForumDtos.ForumPostResponse>> getAll() {
    return ResponseEntity.ok(
        service.getAll().stream()
            .map(apiDtoMapper::toForumPostResponse)
            .collect(Collectors.toList()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ForumDtos.ForumPostResponse> getById(@PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toForumPostResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<ForumDtos.ForumPostResponse> update(
      @PathVariable Long id, @RequestBody ForumDtos.ForumPostRequest post) {
    return ResponseEntity.ok(
        apiDtoMapper.toForumPostResponse(service.update(id, apiDtoMapper.toForumPost(post))));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
