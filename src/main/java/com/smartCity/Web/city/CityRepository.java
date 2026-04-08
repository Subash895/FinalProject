package com.smartCity.Web.city;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.city.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {}
