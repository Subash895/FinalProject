package com.smartCity.Web.Repository;

import com.smartCity.Web.Model.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    List<ForumComment> findByPostId(Long postId);
}