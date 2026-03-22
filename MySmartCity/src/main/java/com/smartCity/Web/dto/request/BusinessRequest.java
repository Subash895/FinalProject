package com.smartCity.Web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessRequest {
	@NotBlank
	private String name;
	@NotBlank
	private String category;
	@NotBlank
	private String address;
	
	private Long userId;

	public Long getCityId() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
