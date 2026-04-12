package com.smartCity.Web.city;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the City part of the Smart City application.
 */
@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
public class City {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String name;

  @Column(unique = true)
  private String state;

  @Column(unique = true)
  private String country;

  private Double latitude;

  private Double longitude;

  public City(String name, String state, String country, Double latitude, Double longitude) {
    this.name = name;
    this.state = state;
    this.country = country;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
