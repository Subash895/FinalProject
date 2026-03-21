package com.smartCity.Web.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.User;
import com.smartCity.Web.Repository.UserRepository;
import com.smartCity.Web.dto.request.UserRequest;
import com.smartCity.Web.dto.response.UserResponse;


@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	public UserResponse register(UserRequest request) {
		
		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("Email Already exists");
		}
		
		// 2. Convert DTO → Entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // (we’ll hash later)

        // 3. Save
        User savedUser = userRepository.save(user);

        // 4. Convert Entity → Response
        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());

		
		return response;
	}
}
