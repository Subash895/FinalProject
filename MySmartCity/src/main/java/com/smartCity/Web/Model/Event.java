package com.smartCity.Web.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "events")
public class Event extends BaseEntity {

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String description;

	@Column(nullable = false)
	private String category;

	@Column(length = 500)
	private String imageUrl;

	@Column(nullable = false)
	private LocalDateTime startDate;

	@Column(nullable = false)
	private LocalDateTime endDate;

	@Column(columnDefinition = "TEXT")
	private String location;

	@Column(length = 20)
	private String contactPhone;

	@Column(length = 100)
	private String contactEmail;

	// 🔥 CORRECT ORGANIZER (linked to system user)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organizer_id", nullable = false)
	@JsonIgnore
	private User organizer;

	@Column(nullable = false)
	private boolean isFeatured = false;

	// 🔥 CITY RELATION
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "city_id", nullable = false)
	private City city;

	@PrePersist
	protected void onCreate() {
		if (isFeatured == false) {
			isFeatured = false;
		}
	}
}