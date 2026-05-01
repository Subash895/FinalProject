package com.smartCity.Web.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock private ReviewRepository reviewRepository;
  @Mock private UserRepository userRepository;
  @Mock private BusinessRepository businessRepository;
  @Mock private PlaceRepository placeRepository;
  @Mock private CityRepository cityRepository;
  @Mock private NewsRepository newsRepository;
  @Mock private JavaMailSender javaMailSender;

  private ReviewService reviewService;
  private EmailNotificationService emailNotificationService;

  @BeforeEach
  void setUp() {
    emailNotificationService =
        new EmailNotificationService(new StaticObjectProvider<>(javaMailSender), "noreply@example.com");
    reviewService =
        new ReviewService(
            reviewRepository,
            userRepository,
            businessRepository,
            placeRepository,
            cityRepository,
            newsRepository,
            emailNotificationService);
  }

  @Test
  void getReviewsValidatesTargetBeforeLookup() {
    when(placeRepository.existsById(9L)).thenReturn(true);
    List<Review> reviews = List.of(new Review());
    when(reviewRepository.findByTargetTypeAndTargetIdOrderByUpdatedAtDesc(ReviewTargetType.PLACE, 9L))
        .thenReturn(reviews);

    List<Review> result = reviewService.getReviews(ReviewTargetType.PLACE, 9L);

    assertSame(reviews, result);
  }

  @Test
  void createReviewRejectsBlankComment() {
    User user = new User("User", "user@example.com", "secret", Role.USER);
    user.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    ReviewDtos.ReviewRequest request =
        new ReviewDtos.ReviewRequest(ReviewTargetType.BUSINESS, 2L, 4, "   ");

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> reviewService.createOrUpdateReview(request, auth(1L, Role.USER)));

    assertEquals(400, exception.getStatusCode().value());
    assertEquals("Comment is required", exception.getReason());
    verify(reviewRepository, never()).save(any(Review.class));
  }

  @Test
  void createReviewSavesTrimmedCommentAndSendsEmails() {
    User user = new User("User", "user@example.com", "secret", Role.USER);
    user.setId(1L);
    User owner = new User("Owner", "owner@example.com", "secret", Role.BUSINESS);
    owner.setId(2L);

    Business business = new Business();
    business.setId(5L);
    business.setName("Smart Cafe");
    business.setOwner(owner);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(businessRepository.existsById(5L)).thenReturn(true);
    when(reviewRepository.save(any(Review.class)))
        .thenAnswer(
            invocation -> {
              Review review = invocation.getArgument(0);
              review.setId(100L);
              return review;
            });
    when(businessRepository.findById(5L)).thenReturn(Optional.of(business));

    ReviewDtos.ReviewRequest request =
        new ReviewDtos.ReviewRequest(ReviewTargetType.BUSINESS, 5L, 5, "  Great service  ");

    Review saved = reviewService.createOrUpdateReview(request, auth(1L, Role.USER));

    assertEquals("Great service", saved.getComment());
    verify(javaMailSender, org.mockito.Mockito.times(2)).send(any(SimpleMailMessage.class));
  }

  @Test
  void updateReviewLimitsNonAdminToOwnedReview() {
    User user = new User("User", "user@example.com", "secret", Role.USER);
    user.setId(1L);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(reviewRepository.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());

    ReviewDtos.ReviewRequest request =
        new ReviewDtos.ReviewRequest(ReviewTargetType.CITY, 3L, 4, "Nice");

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> reviewService.updateReview(9L, request, auth(1L, Role.USER)));

    assertEquals(403, exception.getStatusCode().value());
    assertEquals("Review access denied", exception.getReason());
  }

  @Test
  void deleteReviewLetsAdminDeleteAnyReview() {
    User admin = new User("Admin", "admin@example.com", "secret", Role.ADMIN);
    admin.setId(7L);
    Review review = new Review();
    review.setId(10L);

    when(userRepository.findById(7L)).thenReturn(Optional.of(admin));
    when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

    reviewService.deleteReview(10L, auth(7L, Role.ADMIN));

    verify(reviewRepository).delete(review);
  }

  @Test
  void createReviewRejectsUnauthorizedPrincipal() {
    Authentication authentication = new UsernamePasswordAuthenticationToken("user", "pw");
    ReviewDtos.ReviewRequest request =
        new ReviewDtos.ReviewRequest(ReviewTargetType.NEWS, 1L, 3, "ok");

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> reviewService.createOrUpdateReview(request, authentication));

    assertEquals(401, exception.getStatusCode().value());
    assertEquals("Authentication required", exception.getReason());
  }

  private Authentication auth(Long userId, Role role) {
    JwtUserPrincipal principal =
        new JwtUserPrincipal(userId, "name", "email@example.com", role.name());
    return new UsernamePasswordAuthenticationToken(principal, null, List.of());
  }

  private static final class StaticObjectProvider<T> implements ObjectProvider<T> {
    private final T value;

    private StaticObjectProvider(T value) {
      this.value = value;
    }

    @Override
    public T getObject(Object... args) {
      return value;
    }

    @Override
    public T getIfAvailable() {
      return value;
    }

    @Override
    public T getIfUnique() {
      return value;
    }

    @Override
    public T getObject() {
      return value;
    }
  }
}
