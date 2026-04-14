package com.smartCity.Web.advertisement;

import java.time.LocalDateTime;

import com.smartCity.Web.business.Business;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the Advertisement part of the Smart City application.
 */
@Entity
@Table(name = "advertisements")
@Getter
@Setter
@NoArgsConstructor
public class Advertisement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "business_id")
  private Business business;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  private Double cost;
  private LocalDateTime startDate;
  private LocalDateTime endDate;

  public Advertisement(Business business, String title, String content, Double cost) {
    this.business = business;
    this.title = title;
    this.content = content;
    this.cost = cost;
  }
}
