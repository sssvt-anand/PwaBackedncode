package com.room.app.service;

import java.util.List;
import java.util.stream.Collectors;

import com.room.app.exception.UserEmailNotFound;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.room.app.dto.UserResponse;
import com.room.app.entity.User;
import com.room.app.exception.ResourceNotFoundException;
import com.room.app.repository.UserRepository;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// Use constructor injection instead of @Autowired
	public UserService(UserRepository userRepository,
					   PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}
	public void register(User user) throws Exception {
		if (user.getName() == null || user.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Name cannot be empty");
		}

		if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
			throw new IllegalArgumentException("Password cannot be empty");
		}

		if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
			throw new IllegalArgumentException("Email cannot be empty");
		}

		if (userRepository.existsByEmail(user.getEmail())) {
			throw new Exception("Email already registered");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userRepository.save(user);
	}

	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream().map(user -> new UserResponse(user.getId(), user.getName(),
				user.getEmail(), user.getRole().replace("ROLE_", ""))).collect(Collectors.toList());
	}

	public UserResponse updateUserRole(Long userId, String newRole) throws ResourceNotFoundException {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		String normalizedRole = newRole.startsWith("ROLE_") ? newRole : "ROLE_" + newRole;
		user.setRole(normalizedRole);

		User updatedUser = userRepository.save(user);

		return new UserResponse(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail(),
				normalizedRole.replace("ROLE_", ""));
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UserEmailNotFound("User not found with email: " + email));
	}

	public void updatePassword(String email, String newPassword) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}


}