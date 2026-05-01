package com.smartCity.Web.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

  @Mock private BusinessRepository businessRepository;
  @Mock private UserRepository userRepository;

  private BusinessService businessService;

  @BeforeEach
  void setUp() {
    businessService = new BusinessService(businessRepository, userRepository);
  }

  @Test
  void saveAssignsOwnerAndClearsIncomingId() {
    User owner = new User("Owner", "owner@example.com", "secret", Role.BUSINESS);
    owner.setId(11L);

    Business incoming = new Business();
    incoming.setId(99L);
    incoming.setName("Fresh Market");

    when(userRepository.findById(11L)).thenReturn(Optional.of(owner));
    when(businessRepository.save(incoming)).thenReturn(incoming);

    Business saved = businessService.save(incoming, 11L);

    assertSame(incoming, saved);
    assertNull(incoming.getId());
    assertSame(owner, incoming.getOwner());
  }

  @Test
  void saveRejectsUsersWithoutBusinessPrivileges() {
    User owner = new User("Viewer", "viewer@example.com", "secret", Role.USER);
    owner.setId(12L);
    when(userRepository.findById(12L)).thenReturn(Optional.of(owner));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> businessService.save(new Business(), 12L));

    assertEquals("Only business users or admins can add businesses", exception.getMessage());
    verify(businessRepository, never()).save(any(Business.class));
  }

  @Test
  void getAllReturnsOnlyOwnedBusinessesForBusinessRoleWithoutQuery() {
    List<Business> ownedBusinesses = List.of(new Business(), new Business());
    when(businessRepository.findByOwnerId(5L)).thenReturn(ownedBusinesses);

    List<Business> result = businessService.getAll(null, 5L, Role.BUSINESS.name());

    assertSame(ownedBusinesses, result);
    verify(businessRepository).findByOwnerId(5L);
  }

  @Test
  void getAllTrimsQueryForBusinessOwnerSearch() {
    List<Business> ownedBusinesses = List.of(new Business());
    when(
            businessRepository
                .findByOwnerIdAndNameContainingIgnoreCaseOrOwnerIdAndDescriptionContainingIgnoreCaseOrOwnerIdAndAddressContainingIgnoreCase(
                    5L, "cafe", 5L, "cafe", 5L, "cafe"))
        .thenReturn(ownedBusinesses);

    List<Business> result = businessService.getAll("  cafe  ", 5L, Role.BUSINESS.name());

    assertSame(ownedBusinesses, result);
  }

  @Test
  void getAllUsesPublicSearchForNonBusinessRole() {
    List<Business> publicResults = List.of(new Business());
    when(
            businessRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrAddressContainingIgnoreCase(
                    "shop", "shop", "shop"))
        .thenReturn(publicResults);

    List<Business> result = businessService.getAll(" shop ", 1L, Role.USER.name());

    assertSame(publicResults, result);
  }

  @Test
  void updatePreventsBusinessUserFromEditingAnotherOwnersRecord() {
    User owner = new User("Owner", "owner@example.com", "secret", Role.BUSINESS);
    owner.setId(30L);

    Business existing = new Business();
    existing.setId(8L);
    existing.setOwner(owner);

    when(businessRepository.findById(8L)).thenReturn(Optional.of(existing));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> businessService.update(8L, new Business(), 31L, Role.BUSINESS.name()));

    assertEquals(403, exception.getStatusCode().value());
    assertEquals("You can access only your own business", exception.getReason());
    verify(businessRepository, never()).save(any(Business.class));
  }

  @Test
  void updateAllowsAdminToChangeFeaturedFlag() {
    User owner = new User("Owner", "owner@example.com", "secret", Role.BUSINESS);
    owner.setId(40L);

    Business existing = new Business();
    existing.setId(9L);
    existing.setOwner(owner);
    existing.setIsFeatured(false);

    Business update = new Business();
    update.setName("Updated");
    update.setDescription("New description");
    update.setAddress("New address");
    update.setIsFeatured(true);

    when(businessRepository.findById(9L)).thenReturn(Optional.of(existing));
    when(businessRepository.save(existing)).thenReturn(existing);

    Business saved = businessService.update(9L, update, 1L, Role.ADMIN.name());

    assertSame(existing, saved);
    assertEquals("Updated", existing.getName());
    assertEquals("New description", existing.getDescription());
    assertEquals("New address", existing.getAddress());
    assertEquals(Boolean.TRUE, existing.getIsFeatured());
  }
}
