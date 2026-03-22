package com.smartCity.Web.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "city_premium_listings")
public class CityPremiumListing extends BaseEntity {

	@Column(nullable = false)
	private String featureName;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(length = 500)
	private String imageUrl;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private BigDecimal quarterlyPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus paymentStatus = PaymentStatus.PENDING;

	@Column(nullable = false)
	private boolean isActive = true;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ListingTier tier = ListingTier.STANDARD;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnore
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "city_id", nullable = false)
	private City city;
}