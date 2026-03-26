package com.smartCity.Web.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartCity.Web.Model.User;
import com.smartCity.Web.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ================= BASIC CRUD =================

    public User save(User entity) {
        return repo.save(entity);
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    public Optional<User> getById(Long id) {
        return repo.findById(id);
    }

    public User update(Long id, User entity) {

        User existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existing.setName(entity.getName());
        existing.setEmail(entity.getEmail());

        // update password only if provided
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(entity.getPassword()));
        }

        existing.setRole(entity.getRole());

        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    // ================= REGISTER =================

    public User register(User user) {

        // ✅ check duplicate email
        Optional<User> existing = repo.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // ✅ validate required fields
        if (user.getName() == null || user.getName().isEmpty()) {
            throw new RuntimeException("Name is required");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        if (user.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        // ✅ encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return repo.save(user);
    }
    public User login(String email, String password) {

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }
}