package com.smartCity.Web.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @Column(unique = true)
  private String email;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;

  @JsonIgnore
  @Column(unique = true)
  private String googleSubject;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Lob
  @Column(name = "profile_photo", columnDefinition = "LONGBLOB")
  private byte[] profilePhoto;

  @Column(name = "profile_photo_content_type")
  private String profilePhotoContentType;

  public User(String name, String email, String password, Role role) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.role = role;
  }
}
