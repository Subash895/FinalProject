package com.smartCity.Web.place;

import com.smartCity.Web.city.City;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "places")
@Getter
@Setter
@NoArgsConstructor
public class Place {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "city_id")
  private City city;

  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String category;
  private String location;
  private Double latitude;
  private Double longitude;

  public Place(
      City city,
      String name,
      String description,
      String category,
      String location,
      Double latitude,
      Double longitude) {
    this.city = city;
    this.name = name;
    this.description = description;
    this.category = category;
    this.location = location;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
