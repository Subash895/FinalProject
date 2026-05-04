package com.smartCity.Web.event;

import java.time.LocalDate;

import com.smartCity.Web.city.City;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the Event part of the Smart City application.
 */
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "city_id", nullable = false)
  private City city;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private LocalDate eventDate;

  @Lob
  @Column(name = "image_data", columnDefinition = "LONGBLOB")
  private byte[] imageData;

  @Column(name = "image_content_type")
  private String imageContentType;

  public Event(City city, String title, String description, LocalDate eventDate) {
    this.city = city;
    this.title = title;
    this.description = description;
    this.eventDate = eventDate;
  }
}
