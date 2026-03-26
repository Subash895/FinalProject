package com.smartCity.Web.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.Comment;
import com.smartCity.Web.Model.ForumPost;
import com.smartCity.Web.Repository.CommentRepository;
import com.smartCity.Web.Repository.ForumPostRepository;

@Service
public class ForumService {
    @Autowired
    private ForumPostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    public ForumPost createPost(ForumPost post) {
        return postRepository.save(post);
    }

    public List<ForumPost> getAllPosts() {
        return postRepository.findAll();
    }

    public ForumPost getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}