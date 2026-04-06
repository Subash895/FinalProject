package com.smartCity.Web.business;

import com.smartCity.Web.user.UserDtos.UserResponse;

public final class BusinessDtos {

    private BusinessDtos() {
    }

    public record OwnerRef(Long id) {
    }

    public record BusinessRequest(Long ownerId, OwnerRef owner, String name, String description, String address,
            Boolean isFeatured) {
    }

    public record BusinessResponse(Long id, UserResponse owner, String name, String description, String address,
            Boolean isFeatured) {
    }
}
