package com.smartCity.Web.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.Model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
