package com.smartCity.Web.Service;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Exception.ResourceNotFoundException;
import com.smartCity.Web.Model.Business;
import com.smartCity.Web.Model.City;
import com.smartCity.Web.Model.User;
import com.smartCity.Web.Repository.BusinessRepository;
import com.smartCity.Web.Repository.CityRepository;
import com.smartCity.Web.Repository.UserRepository;
import com.smartCity.Web.dto.request.BusinessRequest;
import com.smartCity.Web.dto.response.BusinessResponse;
import com.smartCity.Web.dto.response.PagedResponse;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;

    public BusinessService(BusinessRepository businessRepository,
                           UserRepository userRepository,
                           CityRepository cityRepository) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
    }

    public BusinessResponse createBusiness(BusinessRequest request) {

        // ✅ GET EMAIL FROM JWT
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // ✅ FETCH USER
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ⚠️ VALIDATE CITY ID
        if (request.getCityId() == null) {
            throw new RuntimeException("City ID is required");
        }

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));

        // ✅ CREATE BUSINESS
        Business business = new Business();
        business.setName(request.getName());
        business.setCategory(request.getCategory());
        business.setAddress(request.getAddress());
        business.setCity(city);
        business.setUser(user);

        Business saved = businessRepository.save(business);

        return mapToResponse(saved);
    }

    public PagedResponse<BusinessResponse> getBusinesses(int page, int size, String category, String name,
                                                        String sortBy, String order) {

        sortBy = (sortBy == null || sortBy.isEmpty()) ? "id" : sortBy;
        order = (order == null || order.isEmpty()) ? "asc" : order;

        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        String categoryFilter = (category == null) ? "" : category;
        String nameFilter = (name == null) ? "" : name;

        Page<Business> pageData =
                businessRepository.findByCategoryContainingIgnoreCaseAndNameContainingIgnoreCase(
                        categoryFilter, nameFilter, pageable
                );

        List<BusinessResponse> items =
                pageData.getContent().stream().map(this::mapToResponse).toList();

        return new PagedResponse<>(
                items,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }

    private BusinessResponse mapToResponse(Business b) {
        BusinessResponse res = new BusinessResponse();
        res.setId(b.getId());
        res.setName(b.getName());
        res.setCategory(b.getCategory());
        res.setAddress(b.getAddress());
        return res;
    }
}