package com.smartCity.Web.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "news")
public class News extends BaseEntity {

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Column(nullable = false)
	private String category;

	@Column(length = 500)
	private String imageUrl;

	// 🔥 AUTHOR (CORRECT WAY)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", nullable = false)
	@JsonIgnore
	private User author;

	@Column(nullable = false)
	private boolean isGovernmentNotice = false;

	@Column(nullable = false)
	private LocalDateTime publishedDate;

	// 🔥 CITY RELATION (CORRECT)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "city_id", nullable = false)
	private City city;

	@PrePersist
	protected void onCreate() {
		if (publishedDate == null) {
			publishedDate = LocalDateTime.now();
		}
	}
}