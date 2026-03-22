package com.smartCity.Web.Security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Repository.UserRepository;
import com.smartCity.Web.Model.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
				.password(user.getPassword()).authorities("USER") // simple role for now
				.build();
	}
}