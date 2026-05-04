package com.smartCity.Web.business;

import java.time.LocalDateTime;

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
@Table(name = "business_vacancies")
@Getter
@Setter
@NoArgsConstructor
public class BusinessVacancy {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "business_id", nullable = false)
  private Business business;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String location;

  @Column(columnDefinition = "TEXT")
  private String requirements;

  private String contactEmail;
  private String salaryInfo;
  private Boolean active = true;
  private LocalDateTime createdAt = LocalDateTime.now();
}
