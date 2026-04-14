package com.smartCity.Web.news;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides database access methods for News records.
 */
@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
  List<News> findByCityId(Long cityId);

  void deleteByCityId(Long cityId);
}
