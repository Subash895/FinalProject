package com.smartCity.Web.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.Model.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
}