package com.smartCity.Web.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.Model.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
}