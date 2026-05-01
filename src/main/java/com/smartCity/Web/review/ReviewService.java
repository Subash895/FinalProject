package com.smartCity.Web.review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.smartCity.Web.auth.jwt.JwtUserPrincipal;
import com.smartCity.Web.business.Business;
import com.smartCity.Web.business.BusinessRepository;
import com.smartCity.Web.city.CityRepository;
import com.smartCity.Web.news.NewsRepository;
import com.smartCity.Web.notification.EmailNotificationService;
import com.smartCity.Web.place.PlaceRepository;
import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;

/**
 * Coordinates the business rules for Review features before data is stored or returned.
 */
@Service
public class ReviewService {

  private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final BusinessRepository businessRepository;
  private final PlaceRepository placeRepository;
  private final CityRepository cityRepository;
  private final NewsRepository newsRepository;
  private final EmailNotificationService emailNotificationService;

  public ReviewService(
      ReviewRepository reviewRepository,
      UserRepository userRepository,
      BusinessRepository businessRepository,
      PlaceRepository placeRepository,
      CityRepository cityRepository,
      NewsRepository newsRepository,
      EmailNotificationService emailNotificationService) {
    this.reviewRepository = reviewRepository;
    this.userRepository = userRepository;
    this.businessRepository = businessRepository;
    this.placeRepository = placeRepository;
    this.cityRepository = cityRepository;
    this.newsRepository = newsRepository;
    this.emailNotificationService = emailNotificationService;
  }

  public List<Review> getReviews(ReviewTargetType targetType, Long targetId) {
    validateTarget(targetType, targetId);
    return reviewRepository.findByTargetTypeAndTargetIdOrderByUpdatedAtDesc(targetType, targetId);
  }

  public Map<Long, List<Review>> getReviewsByTargetIds(
      ReviewTargetType targetType, List<Long> targetIds) {
    if (targetType == null || targetIds == null || targetIds.isEmpty()) {
      return Map.of();
    }

    List<Long> cleanTargetIds =
        targetIds.stream().filter(id -> id != null && id > 0).distinct().toList();
    if (cleanTargetIds.isEmpty()) {
      return Map.of();
    }

    return reviewRepository
        .findByTargetTypeAndTargetIdInOrderByUpdatedAtDesc(targetType, cleanTargetIds)
        .stream()
        .collect(Collectors.groupingBy(Review::getTargetId));
  }

  public Review createOrUpdateReview(
      ReviewDtos.ReviewRequest request, Authentication authentication) {
    JwtUserPrincipal principal = extractPrincipal(authentication);
    User user =
        userRepository
            .findById(principal.id())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (!canSubmitReview(user)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account cannot submit reviews");
    }

    validateRequest(request);
    validateTarget(request.targetType(), request.targetId());

    Review review = new Review();
    review.setUser(user);
    review.setTargetType(request.targetType());
    review.setTargetId(request.targetId());
    review.setCreatedAt(LocalDateTime.now());
    review.setRating(request.rating());
    review.setComment(request.comment().trim());
    review.setUpdatedAt(LocalDateTime.now());
    Review savedReview = reviewRepository.save(review);
    sendReviewEmails(savedReview);
    return savedReview;
  }

  public Review updateReview(
      Long reviewId, ReviewDtos.ReviewRequest request, Authentication authentication) {
    JwtUserPrincipal principal = extractPrincipal(authentication);
    User user =
        userRepository
            .findById(principal.id())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    Review review = findManagedReview(reviewId, user);
    validateRequest(request);
    validateTarget(request.targetType(), request.targetId());

    review.setTargetType(request.targetType());
    review.setTargetId(request.targetId());
    review.setRating(request.rating());
    review.setComment(request.comment().trim());
    review.setUpdatedAt(LocalDateTime.now());
    Review savedReview = reviewRepository.save(review);
    sendReviewEmails(savedReview);
    return savedReview;
  }

  public void deleteReview(Long reviewId, Authentication authentication) {
    JwtUserPrincipal principal = extractPrincipal(authentication);
    User user =
        userRepository
            .findById(principal.id())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    Review review = findManagedReview(reviewId, user);
    reviewRepository.delete(review);
  }

  private JwtUserPrincipal extractPrincipal(Authentication authentication) {
    if (authentication == null
        || !(authentication.getPrincipal() instanceof JwtUserPrincipal principal)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }
    return principal;
  }

  private void validateRequest(ReviewDtos.ReviewRequest request) {
    if (request == null || request.targetType() == null || request.targetId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review target is required");
    }
    if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
    }
    if (request.comment() == null || request.comment().trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment is required");
    }
  }

  private void validateTarget(ReviewTargetType targetType, Long targetId) {
    boolean exists =
        switch (targetType) {
          case BUSINESS -> businessRepository.existsById(targetId);
          case PLACE -> placeRepository.existsById(targetId);
          case CITY -> cityRepository.existsById(targetId);
          case NEWS -> newsRepository.existsById(targetId);
        };

    if (!exists) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target item not found");
    }
  }

  private boolean canSubmitReview(User user) {
    return user.getRole() == Role.USER
        || user.getRole() == Role.BUSINESS
        || user.getRole() == Role.ADMIN;
  }

  private Review findManagedReview(Long reviewId, User user) {
    if (user.getRole() == Role.ADMIN) {
      return reviewRepository
          .findById(reviewId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    return reviewRepository
        .findByIdAndUserId(reviewId, user.getId())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Review access denied"));
  }

  private void sendReviewEmails(Review review) {
    try {
      User reviewer = review.getUser();
      if (reviewer != null) {
        emailNotificationService.sendCommentThankYou(
            reviewer.getEmail(), reviewer.getName(), describeTarget(review));
      }

      if (review.getTargetType() != ReviewTargetType.BUSINESS) {
        return;
      }

      Business business = businessRepository.findById(review.getTargetId()).orElse(null);
      if (business == null || business.getOwner() == null || reviewer == null) {
        return;
      }

      emailNotificationService.sendBusinessCommentNotification(
          business.getOwner().getEmail(),
          business.getOwner().getName(),
          business.getName(),
          reviewer.getName(),
          review.getComment());
    } catch (Exception ex) {
      log.warn("Review notification email skipped: {}", ex.getMessage());
    }
  }

  private String describeTarget(Review review) {
    return switch (review.getTargetType()) {
      case BUSINESS ->
          businessRepository
              .findById(review.getTargetId())
              .map(business -> "business \"" + business.getName() + "\"")
              .orElse("the business");
      case PLACE -> "the place";
      case CITY -> "the city";
      case NEWS -> "the news item";
    };
  }
}
