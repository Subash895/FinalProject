package com.smartCity.Web.review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides database access methods for Review records.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

  List<Review> findByTargetTypeAndTargetIdOrderByUpdatedAtDesc(
      ReviewTargetType targetType, Long targetId);

  List<Review> findByTargetTypeAndTargetIdIn(
      ReviewTargetType targetType, List<Long> targetIds);

  Optional<Review> findByUserIdAndTargetTypeAndTargetId(
      Long userId, ReviewTargetType targetType, Long targetId);

  Optional<Review> findByIdAndUserId(Long id, Long userId);

  void deleteByTargetTypeAndTargetId(ReviewTargetType targetType, Long targetId);

  void deleteByTargetTypeAndTargetIdIn(ReviewTargetType targetType, List<Long> targetIds);
}
