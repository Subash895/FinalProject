package com.smartCity.Web.forum;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartCity.Web.forum.ForumPost;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
}
