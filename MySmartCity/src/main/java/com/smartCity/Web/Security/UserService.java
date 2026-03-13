package com.smartCity.Web.Security;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.User;
import com.smartCity.Web.Repository.UserRepository;

import lombok.Data;

@Data
@Service
public class UserService {
	private final UserRepository repo;
	private final PasswordEncoder passwordEncoder;

	public void saveUser(User user) {
		String encoderPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encoderPassword);
		repo.save(user);
	}

	public boolean validataUser(String email, String password) {
		Optional<User> user = repo.findByEmail(email);
		if (user.isPresent()) {
			return user.get().getPassword().equals(password);
		}
		return false;
	}
}
