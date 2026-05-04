package com.smartCity.Web.city;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityGalleryImageRepository extends JpaRepository<CityGalleryImage, Long> {
  List<CityGalleryImage> findByCityIdOrderBySortOrderAscIdAsc(Long cityId);

  void deleteByCityId(Long cityId);
}
