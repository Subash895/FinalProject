package com.smartCity.Web.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartCity.Web.Model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
}