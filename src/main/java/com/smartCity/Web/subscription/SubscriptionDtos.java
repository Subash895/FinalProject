package com.smartCity.Web.subscription;

import java.time.LocalDateTime;

import com.smartCity.Web.user.UserDtos.UserResponse;

public final class SubscriptionDtos {

    private SubscriptionDtos() {
    }

    public record UserRef(Long id) {
    }

    public record SubscriptionRequest(Long userId, UserRef user, String email, String type, LocalDateTime startDate,
            LocalDateTime endDate, Double price) {
    }

    public record SubscriptionResponse(Long id, UserResponse user, String email, String type, LocalDateTime startDate,
            LocalDateTime endDate, Double price) {
    }
}
