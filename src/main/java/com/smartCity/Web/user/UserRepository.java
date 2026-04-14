package com.smartCity.Web.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides database access methods for User records.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);
}
