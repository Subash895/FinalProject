package com.smartCity.Web.place;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceGalleryImageRepository extends JpaRepository<PlaceGalleryImage, Long> {
  List<PlaceGalleryImage> findByPlaceIdOrderBySortOrderAscIdAsc(Long placeId);

  void deleteByPlaceId(Long placeId);
}
