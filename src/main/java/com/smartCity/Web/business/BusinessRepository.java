package com.smartCity.Web.business;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.business.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
}
