package com.smartCity.Web.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartCity.Web.city.City;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock private EventRepository eventRepository;

  private EventService eventService;

  @BeforeEach
  void setUp() {
    eventService = new EventService(eventRepository);
  }

  @Test
  void updatePreservesCityWhenIncomingCityIsNull() {
    City city = new City();
    city.setName("Vadodara");
    Event existing = new Event(city, "Expo", "Old", LocalDate.of(2026, 5, 1));
    existing.setId(2L);

    Event update = new Event();
    update.setTitle("Expo 2026");
    update.setDescription("New");
    update.setEventDate(LocalDate.of(2026, 5, 10));

    when(eventRepository.findById(2L)).thenReturn(Optional.of(existing));
    when(eventRepository.save(existing)).thenReturn(existing);

    Event saved = eventService.update(2L, update);

    assertSame(existing, saved);
    assertEquals("Vadodara", existing.getCity().getName());
    assertEquals("Expo 2026", existing.getTitle());
  }
}
