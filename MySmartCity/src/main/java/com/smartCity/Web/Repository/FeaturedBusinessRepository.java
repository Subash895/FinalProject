package com.smartCity.Web.Repository;

import com.smartCity.Web.Model.FeaturedBusiness;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeaturedBusinessRepository extends JpaRepository<FeaturedBusiness, Long> {

    List<FeaturedBusiness> findByCityId(Long cityId);

    List<FeaturedBusiness> findByIsActiveTrueOrderByPositionAsc();
}