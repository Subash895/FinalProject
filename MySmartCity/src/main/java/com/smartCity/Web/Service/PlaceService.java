package com.smartCity.Web.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.Place;
import com.smartCity.Web.Repository.PlaceRepository;
import com.smartCity.Web.dto.response.PagedResponse;
import com.smartCity.Web.dto.response.PlaceResponse;

@Service
public class PlaceService {

	private final PlaceRepository placeRepository;

	public PlaceService(PlaceRepository placeRepository) {
		this.placeRepository = placeRepository;
	}

	public PagedResponse<PlaceResponse> getPlacesByCity(Long cityId, Pageable pageable) {

		// 🔥 LIMIT PAGE SIZE (security + performance)
		if (pageable.getPageSize() > 50) {
			pageable = Pageable.ofSize(50).withPage(pageable.getPageNumber());
		}

		Page<Place> pageData = placeRepository.findByCityId(cityId, pageable);

		List<PlaceResponse> items = pageData.getContent().stream().map(this::mapToResponse).toList();

		return new PagedResponse<>(items, pageData.getNumber(), pageData.getSize(), pageData.getTotalElements(),
				pageData.getTotalPages());
	}

	private PlaceResponse mapToResponse(Place p) {

		PlaceResponse res = new PlaceResponse();

		res.setId(p.getId());
		res.setName(p.getName());
		res.setCategory(p.getCategory());
		res.setDescription(p.getDescription());
		res.setLatitude(p.getLatitude());
		res.setLongitude(p.getLongitude());
		res.setImageUrl(p.getImageUrl());
		res.setAddress(p.getAddress());
		res.setWebsite(p.getWebsite());
		res.setOperatingHours(p.getOperatingHours());

		if (p.getCity() != null) {
			res.setCityId(p.getCity().getId());
		}

		return res;
	}
}