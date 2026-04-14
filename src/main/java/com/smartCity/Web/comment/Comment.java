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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
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

  public Comment(ForumPost post, User user, String content) {
    this.post = post;
    this.user = user;
    this.content = content;
  }
}
