package com.smartCity.Web.cityhistory;

import com.smartCity.Web.city.City;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "city_histories")
@Getter
@Setter
@NoArgsConstructor
public class CityHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "city_id")
  private City city;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  public CityHistory(City city, String title, String content) {
    this.city = city;
    this.title = title;
    this.content = content;
  }
}
