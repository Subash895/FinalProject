package com.smartCity.Web.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartCity.Web.Model.ForumPost;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
}