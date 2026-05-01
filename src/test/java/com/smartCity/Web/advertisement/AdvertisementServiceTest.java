package com.smartCity.Web.advertisement;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {

  @Mock private AdvertisementRepository advertisementRepository;

  private AdvertisementService advertisementService;

  @BeforeEach
  void setUp() {
    advertisementService = new AdvertisementService();
    ReflectionTestUtils.setField(
        advertisementService, "advertisementRepository", advertisementRepository);
  }

  @Test
  void createAdvertisementSavesEntity() {
    Advertisement advertisement = new Advertisement();
    when(advertisementRepository.save(advertisement)).thenReturn(advertisement);

    Advertisement saved = advertisementService.createAdvertisement(advertisement);

    assertSame(advertisement, saved);
  }

  @Test
  void getAllAdvertisementsReturnsRepositoryResults() {
    List<Advertisement> advertisements = List.of(new Advertisement());
    when(advertisementRepository.findAll()).thenReturn(advertisements);

    List<Advertisement> result = advertisementService.getAllAdvertisements();

    assertSame(advertisements, result);
    verify(advertisementRepository).findAll();
  }
}
