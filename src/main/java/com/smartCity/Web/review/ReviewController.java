package com.smartCity.Web.review;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartCity.Web.shared.ApiDtoMapper;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin("*")
public class ReviewController {

  private final ReviewService reviewService;
  private final ApiDtoMapper apiDtoMapper;

  public ReviewController(ReviewService reviewService, ApiDtoMapper apiDtoMapper) {
    this.reviewService = reviewService;
    this.apiDtoMapper = apiDtoMapper;
  }

  @GetMapping
  public List<ReviewDtos.ReviewResponse> getReviews(
      @RequestParam ReviewTargetType targetType, @RequestParam Long targetId) {
    return reviewService.getReviews(targetType, targetId).stream()
        .map(apiDtoMapper::toReviewResponse)
        .toList();
  }

  @PostMapping
  public ReviewDtos.ReviewResponse createOrUpdate(
      @RequestBody ReviewDtos.ReviewRequest request, Authentication authentication) {
    return apiDtoMapper.toReviewResponse(
        reviewService.createOrUpdateReview(request, authentication));
  }

  @PutMapping("/{id}")
  public ReviewDtos.ReviewResponse update(
      @PathVariable Long id,
      @RequestBody ReviewDtos.ReviewRequest request,
      Authentication authentication) {
    return apiDtoMapper.toReviewResponse(reviewService.updateReview(id, request, authentication));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id, Authentication authentication) {
    reviewService.deleteReview(id, authentication);
  }
}
