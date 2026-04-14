package com.smartCity.Web.advertisement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.advertisement.Advertisement;
import com.smartCity.Web.advertisement.AdvertisementRepository;

/**
 * Coordinates the business rules for Advertisement features before data is stored or returned.
 */
@Service
public class AdvertisementService {

  @Autowired private AdvertisementRepository advertisementRepository;

  public Advertisement createAdvertisement(Advertisement ad) {
    return advertisementRepository.save(ad);
  }

  public List<Advertisement> getAllAdvertisements() {
    return advertisementRepository.findAll();
  }
}
