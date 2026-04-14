package com.smartCity.Web.config;
//admin@smartcity.local
//user@smartcity.local
//business@smartcity.local

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartCity.Web.advertisement.Advertisement;
import com.smartCity.Web.advertisement.AdvertisementRepository;
import com.smartCity.Web.business.Business;
import com.smartCity.Web.business.BusinessRepository;
import com.smartCity.Web.city.City;
import com.smartCity.Web.city.CityRepository;
import com.smartCity.Web.cityhistory.CityHistory;
import com.smartCity.Web.cityhistory.CityHistoryRepository;
import com.smartCity.Web.comment.Comment;
import com.smartCity.Web.comment.CommentRepository;
import com.smartCity.Web.event.Event;
import com.smartCity.Web.event.EventRepository;
import com.smartCity.Web.forum.ForumPost;
import com.smartCity.Web.forum.ForumPostRepository;
import com.smartCity.Web.marketrate.MarketRate;
import com.smartCity.Web.marketrate.MarketRateRepository;
import com.smartCity.Web.news.News;
import com.smartCity.Web.news.NewsRepository;
import com.smartCity.Web.place.Place;
import com.smartCity.Web.place.PlaceRepository;
import com.smartCity.Web.shared.Category;
import com.smartCity.Web.shared.CategoryRepository;
import com.smartCity.Web.subscription.Subscription;
import com.smartCity.Web.subscription.SubscriptionRepository;
import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;

/**
 * Seeds baseline application data so local and fresh environments start with usable records.
 */
@Component
public class DataSeeder implements CommandLineRunner {

  private static final String DEFAULT_PASSWORD = "Subbu@895";

  private final UserRepository userRepository;
  private final CityRepository cityRepository;
  private final BusinessRepository businessRepository;
  private final AdvertisementRepository advertisementRepository;
  private final CityHistoryRepository cityHistoryRepository;
  private final EventRepository eventRepository;
  private final ForumPostRepository forumPostRepository;
  private final CommentRepository commentRepository;
  private final MarketRateRepository marketRateRepository;
  private final NewsRepository newsRepository;
  private final PlaceRepository placeRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final CategoryRepository categoryRepository;
  private final PasswordEncoder passwordEncoder;

  public DataSeeder(
      UserRepository userRepository,
      CityRepository cityRepository,
      BusinessRepository businessRepository,
      AdvertisementRepository advertisementRepository,
      CityHistoryRepository cityHistoryRepository,
      EventRepository eventRepository,
      ForumPostRepository forumPostRepository,
      CommentRepository commentRepository,
      MarketRateRepository marketRateRepository,
      NewsRepository newsRepository,
      PlaceRepository placeRepository,
      SubscriptionRepository subscriptionRepository,
      CategoryRepository categoryRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.cityRepository = cityRepository;
    this.businessRepository = businessRepository;
    this.advertisementRepository = advertisementRepository;
    this.cityHistoryRepository = cityHistoryRepository;
    this.eventRepository = eventRepository;
    this.forumPostRepository = forumPostRepository;
    this.commentRepository = commentRepository;
    this.marketRateRepository = marketRateRepository;
    this.newsRepository = newsRepository;
    this.placeRepository = placeRepository;
    this.subscriptionRepository = subscriptionRepository;
    this.categoryRepository = categoryRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(String... args) {
    //admin@smartcity.local
    //user@smartcity.local
    //business@smartcity.local
    User admin = ensureUser("Admin", "admin@smartcity.local", Role.ADMIN);
    User user = ensureUser("Demo User", "user@smartcity.local", Role.USER);
    User businessman = ensureUser("Demo Businessman", "business@smartcity.local", Role.BUSINESS);

    City city = ensureCity();
    ensureCategory();

    Business business = ensureBusiness(businessman);
    ensureAdvertisement(business);
    ensureCityHistory(city);
    ensureEvent(city);
    ForumPost forumPost = ensureForumPost();
    ensureComment(forumPost, user);
    ensureMarketRate();
    ensureNews(city);
    ensurePlace(city);
    ensureSubscription(user);

    // Keep references alive inside the transaction.
    admin.getId();
  }

  private User ensureUser(String name, String email, Role role) {
    return userRepository
        .findByEmail(email)
        .map(existing -> updateUser(existing, name, role))
        .orElseGet(
            () ->
                userRepository.save(
                    new User(name, email, passwordEncoder.encode(DEFAULT_PASSWORD), role)));
  }

  private User updateUser(User existing, String name, Role role) {
    boolean changed = false;

    if (!name.equals(existing.getName())) {
      existing.setName(name);
      changed = true;
    }
    if (existing.getRole() != role) {
      existing.setRole(role);
      changed = true;
    }
    if (existing.getPassword() == null
        || !passwordEncoder.matches(DEFAULT_PASSWORD, existing.getPassword())) {
      existing.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
      changed = true;
    }

    return changed ? userRepository.save(existing) : existing;
  }

  private City ensureCity() {
    if (cityRepository.count() > 0) {
      return cityRepository.findAll().get(0);
    }

    return cityRepository.save(new City("Mysuru", "Karnataka", "India", null, null));
  }

  private void ensureCategory() {
    if (categoryRepository.count() == 0) {
      categoryRepository.save(new Category("General Services"));
    }
  }

  private Business ensureBusiness(User owner) {
    if (businessRepository.count() > 0) {
      return businessRepository.findAll().get(0);
    }

    Business business =
        new Business(
            owner,
            "Subbu Smart Services",
            "Local verified city services for residents and visitors.",
            "MG Road, Mysuru");
    business.setIsFeatured(true);
    return businessRepository.save(business);
  }

  private void ensureAdvertisement(Business business) {
    if (advertisementRepository.count() > 0) {
      return;
    }

    Advertisement advertisement =
        new Advertisement(
            business, "Welcome Offer", "Seed advertisement for the Smart City platform.", 499.0);
    advertisement.setStartDate(LocalDateTime.now());
    advertisement.setEndDate(LocalDateTime.now().plusDays(30));
    advertisementRepository.save(advertisement);
  }

  private void ensureCityHistory(City city) {
    if (cityHistoryRepository.count() > 0) {
      return;
    }

    cityHistoryRepository.save(
        new CityHistory(
            city, "City Snapshot", "Mysuru is seeded as the default city for demo and testing."));
  }

  private void ensureEvent(City city) {
    if (eventRepository.count() > 0) {
      return;
    }

    eventRepository.save(
        new Event(
            city,
            "Smart City Expo",
            "Demo city event generated automatically at startup.",
            LocalDate.now().plusDays(10)));
  }

  private ForumPost ensureForumPost() {
    if (forumPostRepository.count() > 0) {
      return forumPostRepository.findAll().get(0);
    }

    ForumPost post = new ForumPost();
    post.setTitle("Welcome To The Forum");
    post.setContent("This sample forum post is created automatically.");
    return forumPostRepository.save(post);
  }

  private void ensureComment(ForumPost post, User user) {
    if (commentRepository.count() > 0) {
      return;
    }

    commentRepository.save(new Comment(post, user, "This is the first seeded comment."));
  }

  private void ensureMarketRate() {
    if (marketRateRepository.count() > 0) {
      return;
    }

    MarketRate marketRate = new MarketRate("Tomato", 32.0, "kg");
    marketRate.setPriceDate(LocalDate.now());
    marketRateRepository.save(marketRate);
  }

  private void ensureNews(City city) {
    if (newsRepository.count() > 0) {
      return;
    }

    newsRepository.save(
        new News(
            city,
            "Platform Seeded",
            "News sample inserted automatically for first-run environments."));
  }

  private void ensurePlace(City city) {
    if (placeRepository.count() > 0) {
      return;
    }

    placeRepository.save(
        new Place(
            city,
            "City Center",
            "Sample destination created automatically.",
            "Landmark",
            "Mysuru Central",
            12.2958,
            76.6394));
  }

  private void ensureSubscription(User user) {
    if (subscriptionRepository.count() > 0) {
      return;
    }

    subscriptionRepository.save(
        new Subscription(
            user,
            user.getEmail(),
            "PREMIUM",
            LocalDateTime.now(),
            LocalDateTime.now().plusMonths(1),
            199.0));
  }
}
