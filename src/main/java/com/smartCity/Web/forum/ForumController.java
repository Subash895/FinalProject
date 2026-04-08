package com.smartCity.Web.forum;

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
import com.smartCity.Web.forum.ForumDtos;
import com.smartCity.Web.comment.CommentDtos;

@RestController
@RequestMapping("/api/forum")
@CrossOrigin(origins = "*")
public class ForumController {
  private final ForumService forumService;
  private final ApiDtoMapper apiDtoMapper;

  public ForumController(ForumService forumService, ApiDtoMapper apiDtoMapper) {
    this.forumService = forumService;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping("/post")
  public ForumDtos.ForumPostResponse createPost(@RequestBody ForumDtos.ForumPostRequest post) {
    return apiDtoMapper.toForumPostResponse(
        forumService.createPost(apiDtoMapper.toForumPost(post)));
  }

  @GetMapping("/posts")
  public List<ForumDtos.ForumPostResponse> getAllPosts() {
    return forumService.getAllPosts().stream()
        .map(apiDtoMapper::toForumPostResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/posts/{id}")
  public ForumDtos.ForumPostResponse getPostById(@PathVariable Long id) {
    return apiDtoMapper.toForumPostResponse(forumService.getPostById(id));
  }

  @PutMapping("/posts/{id}")
  public ForumDtos.ForumPostResponse updatePost(
      @PathVariable Long id, @RequestBody ForumDtos.ForumPostRequest post) {
    return apiDtoMapper.toForumPostResponse(
        forumService.updatePost(id, apiDtoMapper.toForumPost(post)));
  }

  @PostMapping("/comment")
  public CommentDtos.CommentResponse addComment(@RequestBody CommentDtos.CommentRequest comment) {
    return apiDtoMapper.toCommentResponse(forumService.addComment(apiDtoMapper.toComment(comment)));
  }

  @GetMapping("/comments")
  public List<CommentDtos.CommentResponse> getAllComments() {
    return forumService.getAllComments().stream()
        .map(apiDtoMapper::toCommentResponse)
        .collect(Collectors.toList());
  }

  @DeleteMapping("/posts/{id}")
  public void deletePost(@PathVariable Long id) {
    forumService.deletePost(id);
  }
}
