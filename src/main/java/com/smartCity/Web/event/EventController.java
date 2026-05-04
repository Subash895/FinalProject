package com.smartCity.Web.event;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.smartCity.Web.auth.jwt.JwtUserPrincipal;
import com.smartCity.Web.shared.ApiDtoMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@CrossOrigin("*")
@RequiredArgsConstructor
public class EventController {
  private final EventService service;
  private final ApiDtoMapper apiDtoMapper;

  @PostMapping
  public EventDtos.EventResponse create(@RequestBody EventDtos.EventRequest entity) {
    return apiDtoMapper.toEventResponse(service.save(apiDtoMapper.toEvent(entity)));
  }

  @GetMapping
  public List<EventDtos.EventResponse> getAll() {
    return service.getAll().stream()
        .map(apiDtoMapper::toEventResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<EventDtos.EventResponse> getById(@PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toEventResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public EventDtos.EventResponse update(
      @PathVariable Long id, @RequestBody EventDtos.EventRequest entity) {
    return apiDtoMapper.toEventResponse(service.update(id, apiDtoMapper.toEvent(entity)));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }

  @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public EventDtos.EventResponse updateEventPhoto(
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
      return apiDtoMapper.toEventResponse(service.updateImage(id, bytes, safeContentType));
    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to upload event image.");
    }
  }
}
