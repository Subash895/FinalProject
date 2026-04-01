package com.smartCity.Web.Controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.Model.Comment;
import com.smartCity.Web.Model.ForumPost;
import com.smartCity.Web.Service.ForumService;

@RestController
@RequestMapping("/api/forum")
@CrossOrigin(origins = "*")
public class ForumController {
    @Autowired
    private ForumService forumService;

    @PostMapping("/post")
    public ForumPost createPost(@RequestBody ForumPost post) {
        return forumService.createPost(post);
    }

    @GetMapping("/posts")
    public List<ForumPost> getAllPosts() {
        return forumService.getAllPosts();
    }

    @GetMapping("/posts/{id}")
    public ForumPost getPostById(@PathVariable Long id) {
        return forumService.getPostById(id);
    }

    @PutMapping("/posts/{id}")
    public ForumPost updatePost(@PathVariable Long id, @RequestBody ForumPost post) {
        return forumService.updatePost(id, post);
    }

    @PostMapping("/comment")
    public Comment addComment(@RequestBody Comment comment) {
        return forumService.addComment(comment);
    }

    @GetMapping("/comments")
    public List<Comment> getAllComments() {
        return forumService.getAllComments();
    }

    @DeleteMapping("/posts/{id}")
    public void deletePost(@PathVariable Long id) {
        forumService.deletePost(id);
    }
}
