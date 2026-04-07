package com.smartCity.Web.business;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.business.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    List<Business> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String nameQuery,
            String descriptionQuery,
            String addressQuery);
}
