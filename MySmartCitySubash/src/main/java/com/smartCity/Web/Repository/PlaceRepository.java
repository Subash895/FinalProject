package com.smartCity.Web.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.Model.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByCityId(Long cityId);
}