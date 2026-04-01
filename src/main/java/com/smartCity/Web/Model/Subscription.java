package com.smartCity.Web.Model;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "email")
    private String email;
    private String type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double price;
 
    public Subscription() {}

    public Subscription(User user, String email, String type, LocalDateTime start, LocalDateTime end, Double price) {
        this.user = user;
        this.email = email;
        this.type = type;
        this.startDate = start;
        this.endDate = end;
        this.price = price;
    }
 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
