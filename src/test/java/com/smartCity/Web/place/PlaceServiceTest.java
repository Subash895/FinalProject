package com.smartCity.Web.place;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

  @Mock private PlaceRepository placeRepository;
  @Mock private PlaceGalleryImageRepository placeGalleryImageRepository;

  private PlaceService placeService;

  @BeforeEach
  void setUp() {
    placeService = new PlaceService(placeRepository, placeGalleryImageRepository);
  }

  @Test
  void getAllFiltersByCategoryAndLocation() {
    Place matching = new Place();
    matching.setLocation("Main Road");
    Place other = new Place();
    other.setLocation("River Side");

    when(placeRepository.findByCategoryContainingIgnoreCase("food"))
        .thenReturn(List.of(matching, other));

    List<Place> result = placeService.getAll(" food ", " road ");

    assertEquals(1, result.size());
    assertSame(matching, result.getFirst());
  }

  @Test
  void getAllFallsBackToFindAllWhenNoFiltersProvided() {
    List<Place> allPlaces = List.of(new Place(), new Place());
    when(placeRepository.findAll()).thenReturn(allPlaces);

    List<Place> result = placeService.getAll("   ", null);

    assertSame(allPlaces, result);
  }

  @Test
  void updateOnlyChangesOptionalFieldsWhenProvided() {
    Place existing = new Place();
    existing.setId(1L);
    existing.setDescription("Old description");
    existing.setLatitude(10.0);
    existing.setLongitude(20.0);

    Place incoming = new Place();
    incoming.setName("Garden");
    incoming.setCategory("Park");
    incoming.setLocation("Center");

    when(placeRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(placeRepository.save(existing)).thenReturn(existing);

    Place saved = placeService.update(1L, incoming);

    assertSame(existing, saved);
    assertEquals("Garden", existing.getName());
    assertEquals("Park", existing.getCategory());
    assertEquals("Center", existing.getLocation());
    assertEquals("Old description", existing.getDescription());
    assertEquals(10.0, existing.getLatitude());
    assertEquals(20.0, existing.getLongitude());
  }

  @Test
  void deleteDelegatesToRepository() {
    placeService.delete(3L);
    verify(placeGalleryImageRepository).deleteByPlaceId(3L);
    verify(placeRepository).deleteById(3L);
  }
}
