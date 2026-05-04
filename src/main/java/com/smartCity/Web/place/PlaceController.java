package com.smartCity.Web.place;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.smartCity.Web.auth.jwt.JwtUserPrincipal;
import com.smartCity.Web.shared.ApiDtoMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/places")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PlaceController {
  private final PlaceService service;
  private final PlaceGalleryService galleryService;
  private final ApiDtoMapper apiDtoMapper;

  @PostMapping
  public PlaceDtos.PlaceResponse create(@RequestBody PlaceDtos.PlaceRequest entity) {
    return apiDtoMapper.toPlaceResponse(service.save(apiDtoMapper.toPlace(entity)));
  }

  @GetMapping
  public List<PlaceDtos.PlaceResponse> getAll(
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String location) {
    return service.getAll(category, location).stream()
        .map(apiDtoMapper::toPlaceResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<PlaceDtos.PlaceResponse> getById(@PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toPlaceResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public PlaceDtos.PlaceResponse update(
      @PathVariable Long id, @RequestBody PlaceDtos.PlaceRequest entity) {
    return apiDtoMapper.toPlaceResponse(service.update(id, apiDtoMapper.toPlace(entity)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }

  @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public PlaceDtos.PlaceResponse updatePlacePhoto(
      @PathVariable Long id,
      @RequestParam("photo") MultipartFile photo,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null || !"ADMIN".equals(principal.role())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required.");
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
      return apiDtoMapper.toPlaceResponse(service.updateImage(id, bytes, safeContentType));
    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to upload place image.");
    }
  }

  @GetMapping("/{id}/gallery")
  public List<PlaceDtos.PlaceGalleryImageResponse> listGalleryImages(@PathVariable Long id) {
    return galleryService.listByPlace(id).stream()
        .map(apiDtoMapper::toPlaceGalleryImageResponse)
        .collect(Collectors.toList());
  }

  @PostMapping(value = "/{id}/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public PlaceDtos.PlaceGalleryImageResponse addGalleryImage(
      @PathVariable Long id,
      @RequestParam("photo") MultipartFile photo,
      @AuthenticationPrincipal JwtUserPrincipal principal) {
    if (principal == null || !"ADMIN".equals(principal.role())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required.");
    }
    if (photo == null || photo.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an image.");
    }
    try {
      byte[] bytes = photo.getBytes();
      if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed.");
      }
      return apiDtoMapper.toPlaceGalleryImageResponse(
          galleryService.addImage(id, bytes, photo.getContentType()));
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
    if (principal == null || !"ADMIN".equals(principal.role())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required.");
    }
    galleryService.deleteImage(id, imageId);
  }
}
