package com.smartCity.Web.marketrate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MarketRateServiceTest {

  @Mock private MarketRateRepository marketRateRepository;

  private MarketRateService marketRateService;

  @BeforeEach
  void setUp() {
    marketRateService = new MarketRateService();
    ReflectionTestUtils.setField(marketRateService, "repository", marketRateRepository);
  }

  @Test
  void updateMarketRatePreservesPriceDateWhenMissingInRequest() {
    MarketRate existing = new MarketRate("Rice", 10.0, "kg");
    existing.setId(1);
    existing.setPriceDate(LocalDate.of(2026, 4, 1));
    when(marketRateRepository.findById(1)).thenReturn(Optional.of(existing));
    when(marketRateRepository.save(existing)).thenReturn(existing);

    MarketRate update = new MarketRate();
    update.setProductName("Wheat");
    update.setPrice(20.0);
    update.setUnit("kg");

    MarketRate saved = marketRateService.updateMarketRate(1, update);

    assertSame(existing, saved);
    assertEquals("Wheat", existing.getProductName());
    assertEquals(LocalDate.of(2026, 4, 1), existing.getPriceDate());
  }
}
