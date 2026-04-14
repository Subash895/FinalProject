package com.smartCity.Web.subscription;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartCity.Web.subscription.Subscription;

/**
 * Provides database access methods for Subscription records.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
  List<Subscription> findByEmail(String email);

  List<Subscription> findByUserIdOrderByUpdatedAtDesc(Long userId);

  Optional<Subscription> findByProviderSubscriptionId(String providerSubscriptionId);

  Optional<Subscription> findByProviderOrderId(String providerOrderId);
}
