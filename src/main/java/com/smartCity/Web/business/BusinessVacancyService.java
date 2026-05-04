package com.smartCity.Web.business;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessVacancyService {
  private final BusinessRepository businessRepository;
  private final BusinessVacancyRepository vacancyRepository;

  public List<BusinessVacancy> listByBusiness(Long businessId, Long userId, String role) {
    Business business = requireBusiness(businessId);
    validateAccess(business, userId, role);
    return vacancyRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
  }

  public List<BusinessVacancy> listPublicByBusiness(Long businessId) {
    requireBusiness(businessId);
    return vacancyRepository.findByBusinessIdAndActiveTrueOrderByCreatedAtDesc(businessId);
  }

  public BusinessVacancy create(Long businessId, BusinessVacancy vacancy, Long userId, String role) {
    Business business = requireBusiness(businessId);
    validateAccess(business, userId, role);
    vacancy.setId(null);
    vacancy.setBusiness(business);
    return vacancyRepository.save(vacancy);
  }

  public BusinessVacancy update(
      Long businessId, Long vacancyId, BusinessVacancy vacancy, Long userId, String role) {
    Business business = requireBusiness(businessId);
    validateAccess(business, userId, role);
    BusinessVacancy existing =
        vacancyRepository
            .findById(vacancyId)
            .orElseThrow(() -> new RuntimeException("Vacancy not found"));
    if (existing.getBusiness() == null || !businessId.equals(existing.getBusiness().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vacancy does not belong to business");
    }
    existing.setTitle(vacancy.getTitle());
    existing.setDescription(vacancy.getDescription());
    existing.setLocation(vacancy.getLocation());
    existing.setRequirements(vacancy.getRequirements());
    existing.setContactEmail(vacancy.getContactEmail());
    existing.setSalaryInfo(vacancy.getSalaryInfo());
    existing.setActive(vacancy.getActive() == null ? Boolean.TRUE : vacancy.getActive());
    return vacancyRepository.save(existing);
  }

  public void delete(Long businessId, Long vacancyId, Long userId, String role) {
    Business business = requireBusiness(businessId);
    validateAccess(business, userId, role);
    BusinessVacancy existing =
        vacancyRepository
            .findById(vacancyId)
            .orElseThrow(() -> new RuntimeException("Vacancy not found"));
    if (existing.getBusiness() == null || !businessId.equals(existing.getBusiness().getId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vacancy does not belong to business");
    }
    vacancyRepository.deleteById(vacancyId);
  }

  private Business requireBusiness(Long businessId) {
    return businessRepository
        .findById(businessId)
        .orElseThrow(() -> new RuntimeException("Business not found"));
  }

  private void validateAccess(Business business, Long userId, String role) {
    if ("ADMIN".equals(role)) {
      return;
    }
    if ("BUSINESS".equals(role)
        && userId != null
        && business.getOwner() != null
        && userId.equals(business.getOwner().getId())) {
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can access only your own business");
  }
}
