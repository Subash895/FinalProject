package com.smartCity.Web.comment;

import java.time.LocalDateTime;

import com.smartCity.Web.forum.ForumPost;
import com.smartCity.Web.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "post_id")
  private ForumPost post;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(columnDefinition = "TEXT")
  private String content;

  private LocalDateTime createdAt = LocalDateTime.now();

  public Comment() {}

  public Comment(ForumPost post, User user, String content) {
    this.post = post;
    this.user = user;
    this.content = content;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ForumPost getPost() {
    return post;
  }

  public void setPost(ForumPost post) {
    this.post = post;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
