package com.smartCity.Web.user;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.smartCity.Web.auth.GoogleIdTokenVerifier;
import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;
import com.smartCity.Web.auth.GoogleIdTokenVerifier.VerifiedGoogleUser;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder, GoogleIdTokenVerifier googleIdTokenVerifier) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
    }

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

        if (entity.getPassword() != null && !entity.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(entity.getPassword()));
        }

        existing.setRole(entity.getRole());

        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public User register(User user) {
        Optional<User> existing = repo.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (!StringUtils.hasText(user.getName())) {
            throw new RuntimeException("Name is required");
        }

        if (!StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("Password is required");
        }

        if (user.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        user.setRole(normalizeRole(user.getRole()));
        user.setGoogleSubject(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return repo.save(user);
    }

    public User login(String email, String password) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("This account uses Google sign-in. Continue with Google.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    public User loginWithGoogle(String credential, Role requestedRole) {
        VerifiedGoogleUser googleUser = googleIdTokenVerifier.verify(credential);

        User user = repo.findByEmail(googleUser.email())
                .orElseGet(() -> createGoogleUser(googleUser, requestedRole));

        boolean changed = false;

        if (StringUtils.hasText(user.getGoogleSubject())
                && !user.getGoogleSubject().equals(googleUser.subject())) {
            throw new RuntimeException("This email is already linked to another Google account");
        }

        if (!StringUtils.hasText(user.getGoogleSubject())) {
            user.setGoogleSubject(googleUser.subject());
            changed = true;
        }

        if (!StringUtils.hasText(user.getName()) && StringUtils.hasText(googleUser.name())) {
            user.setName(googleUser.name());
            changed = true;
        }

        if (user.getRole() == null) {
            user.setRole(normalizeRole(requestedRole));
            changed = true;
        }

        return changed ? repo.save(user) : user;
    }

    public boolean isGoogleAuthEnabled() {
        return googleIdTokenVerifier.isConfigured();
    }

    public String getGoogleClientId() {
        return googleIdTokenVerifier.getClientId();
    }

    private User createGoogleUser(VerifiedGoogleUser googleUser, Role requestedRole) {
        User user = new User();
        user.setName(StringUtils.hasText(googleUser.name()) ? googleUser.name() : googleUser.email());
        user.setEmail(googleUser.email());
        user.setPassword(null);
        user.setRole(normalizeRole(requestedRole));
        user.setGoogleSubject(googleUser.subject());
        return repo.save(user);
    }

    private Role normalizeRole(Role role) {
        return role == Role.BUSINESS ? Role.BUSINESS : Role.USER;
    }
}

