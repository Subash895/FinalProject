package com.smartCity.Web.place;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.place.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByCityId(Long cityId);
    List<Place> findByCategoryContainingIgnoreCase(String category);
    List<Place> findByLocationContainingIgnoreCase(String location);
}

