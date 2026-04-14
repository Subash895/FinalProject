package com.smartCity.Web.business;

import com.smartCity.Web.user.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@NoArgsConstructor
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

  public Business(User owner, String name, String description, String address) {
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.address = address;
  }
}
