package com.smartCity.Web.city;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CityGalleryService {
  private static final int MAX_GALLERY_IMAGES = 12;
  private static final int MAX_IMAGE_BYTES = 3_000_000;

  private final CityRepository cityRepository;
  private final CityGalleryImageRepository galleryRepository;

  public List<CityGalleryImage> listByCity(Long cityId) {
    requireCity(cityId);
    return galleryRepository.findByCityIdOrderBySortOrderAscIdAsc(cityId);
  }

  public CityGalleryImage addImage(Long cityId, byte[] imageData, String contentType) {
    City city = requireCity(cityId);
    if (imageData == null || imageData.length == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required.");
    }
    if (imageData.length > MAX_IMAGE_BYTES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is too large.");
    }
    List<CityGalleryImage> existing = listByCity(cityId);
    if (existing.size() >= MAX_GALLERY_IMAGES) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "City gallery supports up to 12 images.");
    }
    CityGalleryImage image = new CityGalleryImage();
    image.setCity(city);
    image.setSortOrder(existing.size());
    image.setImageData(imageData);
    image.setImageContentType(
        contentType != null && contentType.startsWith("image/") ? contentType : "image/png");
    return galleryRepository.save(image);
  }

  public void deleteImage(Long cityId, Long imageId) {
    requireCity(cityId);
    CityGalleryImage image =
        galleryRepository
            .findById(imageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found."));
    if (image.getCity() == null || !cityId.equals(image.getCity().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image does not belong to city.");
    }
    galleryRepository.delete(image);
  }

  private City requireCity(Long cityId) {
    return cityRepository
        .findById(cityId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found."));
  }
}
