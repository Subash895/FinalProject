package com.smartCity.Web.Repository;

import com.smartCity.Web.Model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    List<ForumPost> findByCityId(Long cityId);

    List<ForumPost> findByIsPinnedTrue();
}