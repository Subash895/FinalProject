package com.smartCity.Web.business;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessGalleryImageRepository extends JpaRepository<BusinessGalleryImage, Long> {
  List<BusinessGalleryImage> findByBusinessIdOrderBySortOrderAscIdAsc(Long businessId);
}

