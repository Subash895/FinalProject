package com.smartCity.Web.city;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartCity.Web.cityhistory.CityHistoryRepository;
import com.smartCity.Web.event.EventRepository;
import com.smartCity.Web.news.News;
import com.smartCity.Web.news.NewsRepository;
import com.smartCity.Web.place.PlaceRepository;
import com.smartCity.Web.place.Place;
import com.smartCity.Web.review.ReviewRepository;
import com.smartCity.Web.review.ReviewTargetType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CityService {
  private final CityRepository repo;
  private final CityHistoryRepository cityHistoryRepository;
  private final EventRepository eventRepository;
  private final NewsRepository newsRepository;
  private final PlaceRepository placeRepository;
  private final ReviewRepository reviewRepository;

  public City save(City entity) {
    return repo.save(entity);
  }

  public List<City> getAll() {
    return repo.findAll();
  }

  public Optional<City> getById(Long id) {
    return repo.findById(id);
  }

  public City update(Long id, City entity) {
    entity.setId(id);
    return repo.save(entity);
  }

  @Transactional
  public void delete(Long id) {
    List<Long> placeIds = placeRepository.findByCityId(id).stream()
        .map(Place::getId)
        .collect(Collectors.toList());
    List<Long> newsIds = newsRepository.findByCityId(id).stream()
        .map(News::getId)
        .collect(Collectors.toList());

    reviewRepository.deleteByTargetTypeAndTargetId(ReviewTargetType.CITY, id);
    if (!placeIds.isEmpty()) {
      reviewRepository.deleteByTargetTypeAndTargetIdIn(ReviewTargetType.PLACE, placeIds);
    }
    if (!newsIds.isEmpty()) {
      reviewRepository.deleteByTargetTypeAndTargetIdIn(ReviewTargetType.NEWS, newsIds);
    }

    cityHistoryRepository.deleteByCityId(id);
    eventRepository.deleteByCityId(id);
    newsRepository.deleteByCityId(id);
    placeRepository.deleteByCityId(id);
    repo.deleteById(id);
  }
}
