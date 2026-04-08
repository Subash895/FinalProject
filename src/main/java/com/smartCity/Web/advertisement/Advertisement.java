package com.smartCity.Web.advertisement;

import java.time.LocalDateTime;

import com.smartCity.Web.business.Business;

import jakarta.persistence.*;

@Entity
@Table(name = "advertisements")
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

  public Advertisement() {}

  public Advertisement(Business business, String title, String content, Double cost) {
    this.business = business;
    this.title = title;
    this.content = content;
    this.cost = cost;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Business getBusiness() {
    return business;
  }

  public void setBusiness(Business business) {
    this.business = business;
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

  public Double getCost() {
    return cost;
  }

  public void setCost(Double cost) {
    this.cost = cost;
  }

  public LocalDateTime getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDateTime startDate) {
    this.startDate = startDate;
  }

  public LocalDateTime getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDateTime endDate) {
    this.endDate = endDate;
  }
}
