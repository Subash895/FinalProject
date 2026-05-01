package com.smartCity.Web.cityhistory;

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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CityHistoryServiceTest {

  @Mock private CityHistoryRepository cityHistoryRepository;

  private CityHistoryService cityHistoryService;

  @BeforeEach
  void setUp() {
    cityHistoryService = new CityHistoryService();
    ReflectionTestUtils.setField(cityHistoryService, "repository", cityHistoryRepository);
  }

  @Test
  void getCityHistoryByCityDelegatesToRepository() {
    List<CityHistory> history = List.of(new CityHistory());
    when(cityHistoryRepository.findByCityIdAndCityNameIgnoreCaseOrderByIdAsc(1L, "Surat"))
        .thenReturn(history);

    List<CityHistory> result = cityHistoryService.getCityHistoryByCity(1L, "Surat");

    assertSame(history, result);
  }

  @Test
  void updateCityHistoryCopiesEditableFields() {
    CityHistory existing = new CityHistory();
    existing.setId(4L);
    when(cityHistoryRepository.findById(4L)).thenReturn(Optional.of(existing));
    when(cityHistoryRepository.save(existing)).thenReturn(existing);

    CityHistory update = new CityHistory();
    update.setTitle("Updated");
    update.setContent("New content");

    CityHistory saved = cityHistoryService.updateCityHistory(4L, update);

    assertSame(existing, saved);
    assertEquals("Updated", existing.getTitle());
    assertEquals("New content", existing.getContent());
  }

  @Test
  void deleteCityHistoryRemovesEntityById() {
    cityHistoryService.deleteCityHistory(5L);
    verify(cityHistoryRepository).deleteById(5L);
  }
}
