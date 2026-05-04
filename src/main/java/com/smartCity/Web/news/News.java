package com.smartCity.Web.news;

import java.time.LocalDateTime;

import com.smartCity.Web.city.City;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the News part of the Smart City application.
 */
@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
public class News {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "city_id")
  private City city;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  private LocalDateTime createdAt = LocalDateTime.now();

  @Lob
  @Column(name = "image_data", columnDefinition = "LONGBLOB")
  private byte[] imageData;

  @Column(name = "image_content_type")
  private String imageContentType;

  public News(City city, String title, String content) {
    this.city = city;
    this.title = title;
    this.content = content;
  }
}
