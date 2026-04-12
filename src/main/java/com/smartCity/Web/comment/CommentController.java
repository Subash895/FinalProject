package com.smartCity.Web.comment;

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
import com.smartCity.Web.comment.CommentDtos;

/**
 * Exposes REST endpoints for Comment operations.
 */
@RestController
@RequestMapping("/api/comments")
@CrossOrigin("*")
public class CommentController {
  private final CommentService service;
  private final ApiDtoMapper apiDtoMapper;

  public CommentController(CommentService service, ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping
  public CommentDtos.CommentResponse create(@RequestBody CommentDtos.CommentRequest entity) {
    return apiDtoMapper.toCommentResponse(service.save(apiDtoMapper.toComment(entity)));
  }

  @GetMapping
  public List<CommentDtos.CommentResponse> getAll() {
    return service.getAll().stream()
        .map(apiDtoMapper::toCommentResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public org.springframework.http.ResponseEntity<CommentDtos.CommentResponse> getById(
      @PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toCommentResponse)
        .map(org.springframework.http.ResponseEntity::ok)
        .orElse(org.springframework.http.ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public CommentDtos.CommentResponse update(
      @PathVariable Long id, @RequestBody CommentDtos.CommentRequest entity) {
    return apiDtoMapper.toCommentResponse(service.update(id, apiDtoMapper.toComment(entity)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
