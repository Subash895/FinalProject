package com.smartCity.Web.business;

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
@Table(name = "business_gallery_images")
@Getter
@Setter
@NoArgsConstructor
public class BusinessGalleryImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  @Column(name = "image_content_type")
  private String imageContentType;

  @Column(name = "image_data", columnDefinition = "LONGBLOB")
  private byte[] imageData;
}

