package com.smartCity.Web.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.Model.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}