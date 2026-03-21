package com.smartCity.Web.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name= "places")
public class Place extends BaseEntity {
	@Column(nullable =false)
	private String name;
	
	@Column(nullable =false)
	private String category;
	
	@Column(nullable =false)
	private double latitude;
	
	@Column(nullable =false)
	private double longitude;
}
