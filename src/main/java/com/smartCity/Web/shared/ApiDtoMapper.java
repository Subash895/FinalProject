package com.smartCity.Web.shared;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.smartCity.Web.advertisement.Advertisement;
import com.smartCity.Web.advertisement.AdvertisementDtos;
import com.smartCity.Web.business.Business;
import com.smartCity.Web.business.BusinessRepository;
import com.smartCity.Web.business.BusinessDtos;
import com.smartCity.Web.city.City;
import com.smartCity.Web.city.CityRepository;
import com.smartCity.Web.city.CityDtos;
import com.smartCity.Web.cityhistory.CityHistory;
import com.smartCity.Web.cityhistory.CityHistoryDtos;
import com.smartCity.Web.comment.Comment;
import com.smartCity.Web.comment.CommentDtos;
import com.smartCity.Web.event.Event;
import com.smartCity.Web.event.EventDtos;
import com.smartCity.Web.forum.ForumPost;
import com.smartCity.Web.forum.ForumPostRepository;
import com.smartCity.Web.forum.ForumDtos;
import com.smartCity.Web.marketrate.MarketRate;
import com.smartCity.Web.marketrate.MarketRateDtos;
import com.smartCity.Web.news.News;
import com.smartCity.Web.news.NewsDtos;
import com.smartCity.Web.place.Place;
import com.smartCity.Web.place.PlaceDtos;
import com.smartCity.Web.subscription.Subscription;
import com.smartCity.Web.subscription.SubscriptionDtos;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;
import com.smartCity.Web.user.UserDtos;
import com.smartCity.Web.auth.AuthDtos;

@Component
public class ApiDtoMapper {

    private final CityRepository cityRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ForumPostRepository forumPostRepository;

    public ApiDtoMapper(
            CityRepository cityRepository,
            UserRepository userRepository,
            BusinessRepository businessRepository,
            ForumPostRepository forumPostRepository) {
        this.cityRepository = cityRepository;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.forumPostRepository = forumPostRepository;
    }

    public AuthDtos.AuthResponse toAuthResponse(String token, User user) {
        return new AuthDtos.AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public UserDtos.UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserDtos.UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public User toUser(UserDtos.UserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setRole(request.role());
        return user;
    }

    public User toUser(AuthDtos.RegisterRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setRole(request.role());
        return user;
    }

    public CityDtos.CityResponse toCityResponse(City city) {
        if (city == null) {
            return null;
        }
        return new CityDtos.CityResponse(city.getId(), city.getName(), city.getState(), city.getCountry());
    }

    public City toCity(CityDtos.CityRequest request) {
        City city = new City();
        city.setName(request.name());
        city.setState(request.state());
        city.setCountry(request.country());
        return city;
    }

    public BusinessDtos.BusinessResponse toBusinessResponse(Business business) {
        if (business == null) {
            return null;
        }
        return new BusinessDtos.BusinessResponse(
                business.getId(),
                toUserResponse(business.getOwner()),
                business.getName(),
                business.getDescription(),
                business.getAddress(),
                business.getIsFeatured());
    }

    public Business toBusiness(BusinessDtos.BusinessRequest request) {
        Business business = new Business();
        business.setOwner(resolveUser(request.ownerId(), request.owner() == null ? null : request.owner().id()));
        business.setName(request.name());
        business.setDescription(request.description());
        business.setAddress(request.address());
        business.setIsFeatured(Boolean.TRUE.equals(request.isFeatured()));
        return business;
    }

    public AdvertisementDtos.AdvertisementResponse toAdvertisementResponse(Advertisement advertisement) {
        return new AdvertisementDtos.AdvertisementResponse(
                advertisement.getId(),
                toBusinessResponse(advertisement.getBusiness()),
                advertisement.getTitle(),
                advertisement.getContent(),
                advertisement.getCost(),
                advertisement.getStartDate(),
                advertisement.getEndDate());
    }

    public Advertisement toAdvertisement(AdvertisementDtos.AdvertisementRequest request) {
        Advertisement advertisement = new Advertisement();
        advertisement.setBusiness(resolveBusiness(request.businessId(),
                request.business() == null ? null : request.business().id()));
        advertisement.setTitle(request.title());
        advertisement.setContent(request.content());
        advertisement.setCost(request.cost());
        advertisement.setStartDate(request.startDate());
        advertisement.setEndDate(request.endDate());
        return advertisement;
    }

    public CityHistoryDtos.CityHistoryResponse toCityHistoryResponse(CityHistory history) {
        return new CityHistoryDtos.CityHistoryResponse(
                history.getId(),
                toCityResponse(history.getCity()),
                history.getTitle(),
                history.getContent());
    }

    public CityHistory toCityHistory(CityHistoryDtos.CityHistoryRequest request) {
        CityHistory history = new CityHistory();
        history.setCity(resolveCity(request.cityId(), request.city() == null ? null : request.city().id()));
        history.setTitle(request.title());
        history.setContent(request.content());
        return history;
    }

    public EventDtos.EventResponse toEventResponse(Event event) {
        return new EventDtos.EventResponse(
                event.getId(),
                toCityResponse(event.getCity()),
                event.getTitle(),
                event.getDescription(),
                event.getEventDate());
    }

    public Event toEvent(EventDtos.EventRequest request) {
        Event event = new Event();
        event.setCity(resolveCity(request.cityId(), request.city() == null ? null : request.city().id()));
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setEventDate(request.eventDate());
        return event;
    }

    public NewsDtos.NewsResponse toNewsResponse(News news) {
        return new NewsDtos.NewsResponse(
                news.getId(),
                toCityResponse(news.getCity()),
                news.getTitle(),
                news.getContent(),
                news.getCreatedAt());
    }

    public News toNews(NewsDtos.NewsRequest request) {
        News news = new News();
        news.setCity(resolveCity(request.cityId(), request.city() == null ? null : request.city().id()));
        news.setTitle(request.title());
        news.setContent(request.content());
        if (request.createdAt() != null) {
            news.setCreatedAt(request.createdAt());
        }
        return news;
    }

    public PlaceDtos.PlaceResponse toPlaceResponse(Place place) {
        return new PlaceDtos.PlaceResponse(
                place.getId(),
                toCityResponse(place.getCity()),
                place.getName(),
                place.getDescription(),
                place.getCategory(),
                place.getLocation(),
                place.getLatitude(),
                place.getLongitude());
    }

    public Place toPlace(PlaceDtos.PlaceRequest request) {
        Place place = new Place();
        place.setCity(resolveCity(request.cityId(), request.city() == null ? null : request.city().id()));
        place.setName(request.name());
        place.setDescription(request.description());
        place.setCategory(request.category());
        place.setLocation(request.location());
        place.setLatitude(request.latitude());
        place.setLongitude(request.longitude());
        return place;
    }

    public ForumDtos.ForumPostResponse toForumPostResponse(ForumPost post) {
        return new ForumDtos.ForumPostResponse(post.getId(), post.getTitle(), post.getContent());
    }

    public ForumPost toForumPost(ForumDtos.ForumPostRequest request) {
        ForumPost post = new ForumPost();
        post.setTitle(request.title());
        post.setContent(request.content());
        return post;
    }

    public CommentDtos.CommentResponse toCommentResponse(Comment comment) {
        return new CommentDtos.CommentResponse(
                comment.getId(),
                toForumPostResponse(comment.getPost()),
                toUserResponse(comment.getUser()),
                comment.getContent(),
                comment.getCreatedAt());
    }

    public Comment toComment(CommentDtos.CommentRequest request) {
        Comment comment = new Comment();
        comment.setPost(resolveForumPost(request.postId(), request.post() == null ? null : request.post().id()));
        comment.setUser(resolveUser(request.userId(), request.user() == null ? null : request.user().id()));
        comment.setContent(request.content());
        if (request.createdAt() != null) {
            comment.setCreatedAt(request.createdAt());
        }
        return comment;
    }

    public MarketRateDtos.MarketRateResponse toMarketRateResponse(MarketRate rate) {
        return new MarketRateDtos.MarketRateResponse(
                rate.getId(),
                rate.getProductName(),
                rate.getPrice(),
                rate.getUnit(),
                rate.getPriceDate());
    }

    public MarketRate toMarketRate(MarketRateDtos.MarketRateRequest request) {
        MarketRate rate = new MarketRate();
        rate.setProductName(request.productName());
        rate.setPrice(request.price());
        rate.setUnit(request.unit());
        rate.setPriceDate(request.priceDate());
        return rate;
    }

    public SubscriptionDtos.SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        return new SubscriptionDtos.SubscriptionResponse(
                subscription.getId(),
                toUserResponse(subscription.getUser()),
                subscription.getEmail(),
                subscription.getType(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getPrice());
    }

    public Subscription toSubscription(SubscriptionDtos.SubscriptionRequest request) {
        Subscription subscription = new Subscription();
        subscription.setUser(resolveUser(request.userId(), request.user() == null ? null : request.user().id()));
        subscription.setEmail(request.email());
        subscription.setType(request.type());
        subscription.setStartDate(request.startDate());
        subscription.setEndDate(request.endDate());
        subscription.setPrice(request.price());
        return subscription;
    }

    private City resolveCity(Long directId, Long nestedId) {
        Long id = resolveId(directId, nestedId);
        return id == null ? null : cityRepository.getReferenceById(id);
    }

    private User resolveUser(Long directId, Long nestedId) {
        Long id = resolveId(directId, nestedId);
        return id == null ? null : userRepository.getReferenceById(id);
    }

    private Business resolveBusiness(Long directId, Long nestedId) {
        Long id = resolveId(directId, nestedId);
        return id == null ? null : businessRepository.getReferenceById(id);
    }

    private ForumPost resolveForumPost(Long directId, Long nestedId) {
        Long id = resolveId(directId, nestedId);
        return id == null ? null : forumPostRepository.getReferenceById(id);
    }

    private Long resolveId(Long directId, Long nestedId) {
        return directId != null ? directId : Optional.ofNullable(nestedId).orElse(null);
    }
}
