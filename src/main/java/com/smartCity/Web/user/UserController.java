package com.smartCity.Web.user;

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

import com.smartCity.Web.auth.AuthDtos;
import com.smartCity.Web.auth.jwt.JwtService;
import com.smartCity.Web.auth.jwt.JwtUserPrincipal;
import com.smartCity.Web.shared.ApiDtoMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

  private final UserService service;
  private final ApiDtoMapper apiDtoMapper;
  private final JwtService jwtService;

  @PostMapping
  public UserDtos.UserResponse create(@RequestBody UserDtos.UserRequest user) {
    return apiDtoMapper.toUserResponse(service.register(apiDtoMapper.toUser(user)));
  }

  @GetMapping
  public List<UserDtos.UserResponse> getAll() {
    return service.getAll().stream().map(apiDtoMapper::toUserResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDtos.UserResponse> getById(@PathVariable Long id) {
    return service
        .getById(id)
        .map(apiDtoMapper::toUserResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/me")
  public UserDtos.UserResponse getProfile(@AuthenticationPrincipal JwtUserPrincipal principal) {
    return apiDtoMapper.toUserResponse(service.getProfile(principal.id()));
  }

  @PutMapping("/{id}")
  public UserDtos.UserResponse update(
      @PathVariable Long id, @RequestBody UserDtos.UserRequest user) {
    return apiDtoMapper.toUserResponse(service.update(id, apiDtoMapper.toUser(user)));
  }

  @PutMapping("/me")
  public AuthDtos.AuthResponse updateProfile(
      @AuthenticationPrincipal JwtUserPrincipal principal,
      @RequestBody UserDtos.ProfileUpdateRequest request) {
    var savedUser = service.updateProfile(principal.id(), apiDtoMapper.toUser(request));
    return apiDtoMapper.toAuthResponse(jwtService.generateToken(savedUser), savedUser);
  }

  @PutMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UserDtos.UserResponse updateProfilePhoto(
      @AuthenticationPrincipal JwtUserPrincipal principal,
      @RequestParam("photo") MultipartFile photo) {
    if (photo == null || photo.isEmpty()) {
      throw new RuntimeException("Please select a photo.");
    }
    try {
      byte[] bytes = photo.getBytes();
      var bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
      if (bufferedImage == null) {
        throw new RuntimeException("Only image files are allowed.");
      }

      String contentType = photo.getContentType();
      String safeContentType = contentType != null && contentType.startsWith("image/")
          ? contentType
          : "image/png";
      return apiDtoMapper.toUserResponse(
          service.updateProfilePhoto(principal.id(), bytes, safeContentType));
    } catch (Exception ex) {
      if ("Only image files are allowed.".equals(ex.getMessage())) {
        throw new RuntimeException(ex.getMessage());
      }
      throw new RuntimeException("Failed to upload profile photo.");
    }
  }

  @DeleteMapping("/{id}")
  public String delete(@PathVariable Long id) {
    service.delete(id);
    return "User deleted successfully";
  }
}
