package com.smartCity.Web.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartCity.Web.Model.Business;

public interface BusinessRepository extends JpaRepository<Business, Long> {

	Page<Business> findByCategoryContainingIgnoreCaseAndNameContainingIgnoreCase(String category, String name,
			Pageable pageable);
	  List<Business> findByCityId(Long cityId);

	    List<Business> findByUserId(Long userId);
}