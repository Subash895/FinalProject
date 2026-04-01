package com.smartCity.Web.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ForumPost updatePost(Long id, ForumPost post) {
        ForumPost existing = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Forum post not found with id: " + id));

        existing.setTitle(post.getTitle());
        existing.setContent(post.getContent());
        return postRepository.save(existing);
    }

    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Forum post not found with id: " + id);
        }
        commentRepository.deleteByPostId(id);
        postRepository.deleteById(id);
    }
}
