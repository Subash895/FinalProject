package com.smartCity.Web.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.Place;
import com.smartCity.Web.Repository.PlaceRepository;

@Service

public class PlaceService {
	private final PlaceRepository placeRepository;
	public PlaceService(PlaceRepository placeRepository) {
		this.placeRepository =placeRepository;
	}
	
	public List<Place> getAllPlaces(){
		return placeRepository.findAll();
	}
}
