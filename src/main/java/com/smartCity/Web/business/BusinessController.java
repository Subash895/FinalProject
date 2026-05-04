package com.smartCity.Web.business;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.smartCity.Web.auth.jwt.JwtUserPrincipal;
import com.smartCity.Web.shared.ApiDtoMapper;
import com.smartCity.Web.business.BusinessDtos;
import org.springframework.http.HttpStatus;

/**
 * Exposes REST endpoints for Business operations.
 */
@RestController
@RequestMapping("/api/businesses")
@CrossOrigin("*")
public class BusinessController {
  private final BusinessService service;
  private final BusinessVacancyService vacancyService;
  private final BusinessGalleryService galleryService;
  private final ApiDtoMapper apiDtoMapper;

  public BusinessController(
      BusinessService service,
      BusinessVacancyService vacancyService,
      BusinessGalleryService galleryService,
      ApiDtoMapper apiDtoMapper) {
    this.service = service;
    this.vacancyService = vacancyService;
    this.galleryService = galleryService;
    this.apiDtoMapper = apiDtoMapper;
  }

  @PostMapping
  public BusinessDtos.BusinessResponse create(
      @RequestBody BusinessDtos.BusinessRequest entity,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }

    try {
      return apiDtoMapper.toBusinessResponse(
          service.save(apiDtoMapper.toBusiness(entity), principal.id()));
    } catch (RuntimeException ex) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage(), ex);
    }
  }

  @GetMapping
  public List<BusinessDtos.BusinessResponse> getAll(
      @RequestParam(required = false) String q,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    return service
        .getAll(q, principal == null ? null : principal.id(), principal == null ? null : principal.role())
        .stream()
        .map(apiDtoMapper::toBusinessResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<BusinessDtos.BusinessResponse> getById(
      @PathVariable Long id, @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal != null && "BUSINESS".equals(principal.role())) {
      return ResponseEntity.ok(
          apiDtoMapper.toBusinessResponse(
              service.getByIdForUser(id, principal.id(), principal.role())));
    }

    return service
        .getById(id)
        .map(apiDtoMapper::toBusinessResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public BusinessDtos.BusinessResponse update(
      @PathVariable Long id,
      @RequestBody BusinessDtos.BusinessRequest entity,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }

    return apiDtoMapper.toBusinessResponse(
        service.update(id, apiDtoMapper.toBusiness(entity), principal.id(), principal.role()));
  }

  @DeleteMapping("/{id}")
  public void delete(
      @PathVariable Long id, @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }

    service.delete(id, principal.id(), principal.role());
  }

  @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public BusinessDtos.BusinessResponse updateBusinessPhoto(
      @PathVariable Long id,
      @RequestParam("photo") MultipartFile photo,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }
    if (photo == null || photo.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an image.");
    }
    try {
      byte[] bytes = photo.getBytes();
      if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed.");
      }

      String contentType = photo.getContentType();
      String safeContentType =
          contentType != null && contentType.startsWith("image/") ? contentType : "image/png";
      return apiDtoMapper.toBusinessResponse(
          service.updateImage(id, bytes, safeContentType, principal.id(), principal.role()));
    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to upload image.");
    }
  }

  @GetMapping("/{id}/vacancies")
  public List<BusinessDtos.VacancyResponse> listVacancies(
      @PathVariable Long id, @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }
    return vacancyService.listByBusiness(id, principal.id(), principal.role()).stream()
        .map(apiDtoMapper::toBusinessVacancyResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}/vacancies/public")
  public List<BusinessDtos.VacancyResponse> listPublicVacancies(@PathVariable Long id) {
    return vacancyService.listPublicByBusiness(id).stream()
        .map(apiDtoMapper::toBusinessVacancyResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}/gallery")
  public List<BusinessDtos.BusinessGalleryImageResponse> listGalleryImages(@PathVariable Long id) {
    return galleryService.listByBusiness(id).stream()
        .map(apiDtoMapper::toBusinessGalleryImageResponse)
        .collect(Collectors.toList());
  }

  @PostMapping(value = "/{id}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public BusinessDtos.BusinessGalleryImageResponse addGalleryImage(
      @PathVariable Long id,
      @RequestParam("photo") MultipartFile photo,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }
    if (photo == null || photo.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an image.");
    }
    try {
      byte[] bytes = photo.getBytes();
      if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed.");
      }
      return apiDtoMapper.toBusinessGalleryImageResponse(
          galleryService.addImage(id, bytes, photo.getContentType(), principal.id(), principal.role()));
    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to upload image.");
    }
  }

  @DeleteMapping("/{id}/gallery/{imageId}")
  public void deleteGalleryImage(
      @PathVariable Long id,
      @PathVariable Long imageId,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }
    galleryService.deleteImage(id, imageId, principal.id(), principal.role());
  }

  @PostMapping("/{id}/vacancies")
  public BusinessDtos.VacancyResponse createVacancy(
      @PathVariable Long id,
      @RequestBody BusinessDtos.VacancyRequest request,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }
    return apiDtoMapper.toBusinessVacancyResponse(
        vacancyService.create(id, apiDtoMapper.toBusinessVacancy(request), principal.id(), principal.role()));
  }

  @PutMapping("/{id}/vacancies/{vacancyId}")
  public BusinessDtos.VacancyResponse updateVacancy(
      @PathVariable Long id,
      @PathVariable Long vacancyId,
      @RequestBody BusinessDtos.VacancyRequest request,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }
    return apiDtoMapper.toBusinessVacancyResponse(
        vacancyService.update(
            id, vacancyId, apiDtoMapper.toBusinessVacancy(request), principal.id(), principal.role()));
  }

  @DeleteMapping("/{id}/vacancies/{vacancyId}")
  public void deleteVacancy(
      @PathVariable Long id,
      @PathVariable Long vacancyId,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
    }
    vacancyService.delete(id, vacancyId, principal.id(), principal.role());
  }
}
