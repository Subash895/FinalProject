package com.smartCity.Web.comment;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.comment.Comment;
import com.smartCity.Web.comment.CommentRepository;

@Service
public class CommentService {
  @Autowired private CommentRepository repo;

  public Comment save(Comment entity) {
    return repo.save(entity);
  }

  public List<Comment> getAll() {
    return repo.findAll();
  }

  public Optional<Comment> getById(Long id) {
    return repo.findById(id);
  }

  public Comment update(Long id, Comment entity) {
    entity.setId(id);
    return repo.save(entity);
  }

  public void delete(Long id) {
    repo.deleteById(id);
  }
}
