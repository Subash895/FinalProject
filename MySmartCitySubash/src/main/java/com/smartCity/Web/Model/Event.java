package com.smartCity.Web.Model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Relationship with City
	@ManyToOne
	@JoinColumn(name = "city_id", nullable = false)
	private City city;

	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	private LocalDate eventDate;

	public Event() {
	}

	public Event(City city, String title, String description, LocalDate eventDate) {
		this.city = city;
		this.title = title;
		this.description = description;
		this.eventDate = eventDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}
}