package com.smartCity.Web.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.comment.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	void deleteByPostId(Long postId);
}

