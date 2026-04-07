package com.smartCity.Web.review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTargetTypeAndTargetIdOrderByUpdatedAtDesc(ReviewTargetType targetType, Long targetId);

    Optional<Review> findByUserIdAndTargetTypeAndTargetId(Long userId, ReviewTargetType targetType, Long targetId);

    Optional<Review> findByIdAndUserId(Long id, Long userId);
}
