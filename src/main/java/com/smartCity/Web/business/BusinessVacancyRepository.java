package com.smartCity.Web.business;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessVacancyRepository extends JpaRepository<BusinessVacancy, Long> {
  List<BusinessVacancy> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
  List<BusinessVacancy> findByBusinessIdAndActiveTrueOrderByCreatedAtDesc(Long businessId);
}
