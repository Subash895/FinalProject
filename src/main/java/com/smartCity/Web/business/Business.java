package com.smartCity.Web.business;

import com.smartCity.Web.user.User;

import jakarta.persistence.*;

@Entity
@Table(name = "businesses")
public class Business {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  private User owner;

  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String address;
  private Boolean isFeatured = false;

  public Business() {}

  public Business(User owner, String name, String description, String address) {
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.address = address;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Boolean getIsFeatured() {
    return isFeatured;
  }

  public void setIsFeatured(Boolean isFeatured) {
    this.isFeatured = isFeatured;
  }
}
