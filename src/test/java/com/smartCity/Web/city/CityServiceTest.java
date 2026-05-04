package com.smartCity.Web.city;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

import com.smartCity.Web.cityhistory.CityHistoryRepository;
import com.smartCity.Web.event.EventRepository;
import com.smartCity.Web.news.News;
import com.smartCity.Web.news.NewsRepository;
import com.smartCity.Web.place.Place;
import com.smartCity.Web.place.PlaceRepository;
import com.smartCity.Web.review.ReviewRepository;
import com.smartCity.Web.review.ReviewTargetType;
import com.smartCity.Web.city.CityGalleryImageRepository;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

  @Mock private CityRepository cityRepository;
  @Mock private CityHistoryRepository cityHistoryRepository;
  @Mock private EventRepository eventRepository;
  @Mock private NewsRepository newsRepository;
  @Mock private PlaceRepository placeRepository;
  @Mock private ReviewRepository reviewRepository;
  @Mock private CityGalleryImageRepository cityGalleryImageRepository;

  private CityService cityService;

  @BeforeEach
  void setUp() {
    cityService =
        new CityService(
            cityRepository,
            cityHistoryRepository,
            eventRepository,
            newsRepository,
            placeRepository,
            reviewRepository,
            cityGalleryImageRepository);
  }

  @Test
  void updateCopiesEditableFields() {
    City existing = new City();
    existing.setId(1L);
    when(cityRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(cityRepository.save(existing)).thenReturn(existing);

    City incoming = new City();
    incoming.setName("Surat");
    incoming.setState("Gujarat");
    incoming.setCountry("India");
    incoming.setLatitude(21.17);
    incoming.setLongitude(72.83);

    City saved = cityService.update(1L, incoming);

    assertSame(existing, saved);
    assertEquals("Surat", existing.getName());
    assertEquals("Gujarat", existing.getState());
  }

  @Test
  void deleteCascadesToDependentRepositories() {
    Place place = new Place();
    place.setId(10L);
    News news = new News();
    news.setId(20L);
    when(placeRepository.findByCityId(3L)).thenReturn(List.of(place));
    when(newsRepository.findByCityId(3L)).thenReturn(List.of(news));

    cityService.delete(3L);

    verify(reviewRepository).deleteByTargetTypeAndTargetId(ReviewTargetType.CITY, 3L);
    verify(reviewRepository).deleteByTargetTypeAndTargetIdIn(ReviewTargetType.PLACE, List.of(10L));
    verify(reviewRepository).deleteByTargetTypeAndTargetIdIn(ReviewTargetType.NEWS, List.of(20L));
    verify(cityHistoryRepository).deleteByCityId(3L);
    verify(cityGalleryImageRepository).deleteByCityId(3L);
    verify(eventRepository).deleteByCityId(3L);
    verify(newsRepository).deleteByCityId(3L);
    verify(placeRepository).deleteByCityId(3L);
    verify(cityRepository).deleteById(3L);
  }

  @Test
  void deleteSkipsReviewCleanupForEmptyPlaceAndNewsLists() {
    when(placeRepository.findByCityId(4L)).thenReturn(List.of());
    when(newsRepository.findByCityId(4L)).thenReturn(List.of());

    cityService.delete(4L);

    verify(reviewRepository).deleteByTargetTypeAndTargetId(ReviewTargetType.CITY, 4L);
    verify(reviewRepository, never()).deleteByTargetTypeAndTargetIdIn(ReviewTargetType.PLACE, List.of());
    verify(reviewRepository, never()).deleteByTargetTypeAndTargetIdIn(ReviewTargetType.NEWS, List.of());
  }
}
