package com.smartCity.Web.shared;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides database access helpers for category records.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {}
