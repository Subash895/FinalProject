package com.smartCity.Web.forum;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for Forum Post records.
 */
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {}
