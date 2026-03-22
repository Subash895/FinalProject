package com.smartCity.Web.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Exception.ResourceNotFoundException;
import com.smartCity.Web.Model.Business;
import com.smartCity.Web.Model.User;
import com.smartCity.Web.Repository.BusinessRepository;
import com.smartCity.Web.Repository.UserRepository;
import com.smartCity.Web.dto.request.BusinessRequest;
import com.smartCity.Web.dto.response.BusinessResponse;
import com.smartCity.Web.dto.response.PagedResponse;

@Service
public class BusinessService {

	private final BusinessRepository businessRepository;
	private final UserRepository userRepository;

	public BusinessService(BusinessRepository businessRepository, UserRepository userRepository) {
		this.businessRepository = businessRepository;
		this.userRepository = userRepository;
	}

	// CREATE BUSINESS
	public BusinessResponse createBusiness(BusinessRequest request) {

		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Business business = new Business();
		business.setName(request.getName());
		business.setCategory(request.getCategory());
		business.setAddress(request.getAddress());
		business.setUser(user);

		Business saved = businessRepository.save(business);

		BusinessResponse response = new BusinessResponse();
		response.setId(saved.getId());
		response.setName(saved.getName());
		response.setCategory(saved.getCategory());
		response.setAddress(saved.getAddress());

		return response;
	}

	public PagedResponse<BusinessResponse> getBusinesses(int page, int size, String category, String name,
			String sortBy, String order) {

		// 🔥 DEFAULT VALUES
		sortBy = (sortBy == null || sortBy.isEmpty()) ? "id" : sortBy;
		order = (order == null || order.isEmpty()) ? "asc" : order;

		Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		String categoryFilter = (category == null) ? "" : category;
		String nameFilter = (name == null) ? "" : name;

		Page<Business> businessPage = businessRepository
				.findByCategoryContainingIgnoreCaseAndNameContainingIgnoreCase(categoryFilter, nameFilter, pageable);

		List<BusinessResponse> items = businessPage.getContent().stream().map(b -> {
			BusinessResponse res = new BusinessResponse();
			res.setId(b.getId());
			res.setName(b.getName());
			res.setCategory(b.getCategory());
			res.setAddress(b.getAddress());
			return res;
		}).toList();

		return new PagedResponse<>(items, businessPage.getNumber(), businessPage.getSize(),
				businessPage.getTotalElements(), businessPage.getTotalPages());
	}
}