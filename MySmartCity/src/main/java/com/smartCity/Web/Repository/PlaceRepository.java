package com.smartCity.Web.Repository;

import com.smartCity.Web.Model.Place;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Page<Place> findByCityId(Long cityId, Pageable pageable);

    List<Place> findByCategory(String category);
}