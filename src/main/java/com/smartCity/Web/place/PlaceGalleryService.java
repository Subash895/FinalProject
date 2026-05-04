package com.smartCity.Web.place;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceGalleryService {
  private static final int MAX_GALLERY_IMAGES = 12;
  private static final int MAX_IMAGE_BYTES = 3_000_000;

  private final PlaceRepository placeRepository;
  private final PlaceGalleryImageRepository galleryRepository;

  public List<PlaceGalleryImage> listByPlace(Long placeId) {
    requirePlace(placeId);
    return galleryRepository.findByPlaceIdOrderBySortOrderAscIdAsc(placeId);
  }

  public PlaceGalleryImage addImage(Long placeId, byte[] imageData, String contentType) {
    Place place = requirePlace(placeId);
    if (imageData == null || imageData.length == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required.");
    }
    if (imageData.length > MAX_IMAGE_BYTES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is too large.");
    }
    List<PlaceGalleryImage> existing = listByPlace(placeId);
    if (existing.size() >= MAX_GALLERY_IMAGES) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Place gallery supports up to 12 images.");
    }
    PlaceGalleryImage image = new PlaceGalleryImage();
    image.setPlace(place);
    image.setSortOrder(existing.size());
    image.setImageData(imageData);
    image.setImageContentType(
        contentType != null && contentType.startsWith("image/") ? contentType : "image/png");
    return galleryRepository.save(image);
  }

  public void deleteImage(Long placeId, Long imageId) {
    requirePlace(placeId);
    PlaceGalleryImage image =
        galleryRepository
            .findById(imageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found."));
    if (image.getPlace() == null || !placeId.equals(image.getPlace().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image does not belong to place.");
    }
    galleryRepository.delete(image);
  }

  private Place requirePlace(Long placeId) {
    return placeRepository
        .findById(placeId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found."));
  }
}
