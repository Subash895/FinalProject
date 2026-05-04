package com.smartCity.Web.city;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "city_gallery_images")
@Getter
@Setter
@NoArgsConstructor
public class CityGalleryImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "city_id", nullable = false)
  private City city;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  @Column(name = "image_content_type")
  private String imageContentType;

  @Column(name = "image_data", columnDefinition = "LONGBLOB")
  private byte[] imageData;
}
