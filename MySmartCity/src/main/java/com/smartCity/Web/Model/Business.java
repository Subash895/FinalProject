package com.smartCity.Web.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "businesses")
public class Business extends BaseEntity {
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private String category;
	
	@Column(nullable = false)
	private String address;
	
	@ManyToOne
	@JoinColumn(name="user_id", nullable = false)
	@JsonIgnore
	private User user;
}
