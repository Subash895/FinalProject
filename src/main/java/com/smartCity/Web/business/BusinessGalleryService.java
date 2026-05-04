package com.smartCity.Web.business;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessGalleryService {
  private static final int MAX_GALLERY_IMAGES = 8;
  private static final int MAX_IMAGE_BYTES = 3_000_000;

  private final BusinessRepository businessRepository;
  private final BusinessGalleryImageRepository galleryRepository;

  public List<BusinessGalleryImage> listByBusiness(Long businessId) {
    requireBusiness(businessId);
    return galleryRepository.findByBusinessIdOrderBySortOrderAscIdAsc(businessId);
  }

  public BusinessGalleryImage addImage(
      Long businessId, byte[] imageData, String contentType, Long userId, String role) {
    Business business = requireBusiness(businessId);
    validateAccess(business, userId, role);
    if (imageData == null || imageData.length == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required.");
    }
    if (imageData.length > MAX_IMAGE_BYTES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is too large.");
    }
    List<BusinessGalleryImage> existing = listByBusiness(businessId);
    if (existing.size() >= MAX_GALLERY_IMAGES) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gallery supports up to 8 images.");
    }
    BusinessGalleryImage image = new BusinessGalleryImage();
    image.setBusiness(business);
    image.setSortOrder(existing.size());
    image.setImageData(imageData);
    image.setImageContentType(
        contentType != null && contentType.startsWith("image/") ? contentType : "image/png");
    return galleryRepository.save(image);
  }

  public void deleteImage(Long businessId, Long imageId, Long userId, String role) {
    Business business = requireBusiness(businessId);
    validateAccess(business, userId, role);
    BusinessGalleryImage image =
        galleryRepository
            .findById(imageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found."));
    if (image.getBusiness() == null || !businessId.equals(image.getBusiness().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image does not belong to business.");
    }
    galleryRepository.delete(image);
  }

  private Business requireBusiness(Long businessId) {
    return businessRepository
        .findById(businessId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found."));
  }

  private void validateAccess(Business business, Long userId, String role) {
    if ("ADMIN".equals(role)) {
      return;
    }
    if ("BUSINESS".equals(role)
        && userId != null
        && business.getOwner() != null
        && userId.equals(business.getOwner().getId())) {
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can access only your own business");
  }
}

