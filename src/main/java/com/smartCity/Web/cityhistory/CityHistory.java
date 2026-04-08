package com.smartCity.Web.cityhistory;

import com.smartCity.Web.city.City;

import jakarta.persistence.*;

@Entity
@Table(name = "city_histories")
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

  public CityHistory() {}

  public CityHistory(City city, String title, String content) {
    this.city = city;
    this.title = title;
    this.content = content;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public City getCity() {
    return city;
  }

  public void setCity(City city) {
    this.city = city;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
