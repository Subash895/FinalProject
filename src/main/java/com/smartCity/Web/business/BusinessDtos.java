package com.smartCity.Web.business;

import com.smartCity.Web.user.UserDtos.UserResponse;

/**
 * Groups the request and response DTOs used by the Business API.
 */
public final class BusinessDtos {

  private BusinessDtos() {}

  public record OwnerRef(Long id) {}

  public record BusinessRequest(
      Long ownerId,
      OwnerRef owner,
      String name,
      String description,
      String address,
      Boolean isFeatured,
      String imageUrl) {}

  public record BusinessResponse(
      Long id,
      UserResponse owner,
      String name,
      String description,
      String address,
      Boolean isFeatured,
      String imageUrl) {}

  public record VacancyRequest(
      String title,
      String description,
      String location,
      String requirements,
      String contactEmail,
      String salaryInfo,
      Boolean active) {}

  public record VacancyResponse(
      Long id,
      Long businessId,
      String title,
      String description,
      String location,
      String requirements,
      String contactEmail,
      String salaryInfo,
      Boolean active,
      java.time.LocalDateTime createdAt) {}

  public record BusinessGalleryImageResponse(Long id, Long businessId, String imageUrl, Integer sortOrder) {}
}
