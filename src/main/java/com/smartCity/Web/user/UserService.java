package com.smartCity.Web.user;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.smartCity.Web.auth.GoogleIdTokenVerifier;
import com.smartCity.Web.notification.EmailNotificationService;
import com.smartCity.Web.user.Role;
import com.smartCity.Web.user.User;
import com.smartCity.Web.user.UserRepository;
import com.smartCity.Web.auth.GoogleIdTokenVerifier.VerifiedGoogleUser;

@Service
public class UserService {

    private static final long RESET_OTP_TTL_MILLIS = 10 * 60 * 1000L;

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final EmailNotificationService emailNotificationService;
    private final ConcurrentMap<String, PasswordResetOtp> passwordResetOtps = new ConcurrentHashMap<>();

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder, GoogleIdTokenVerifier googleIdTokenVerifier,
            EmailNotificationService emailNotificationService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.emailNotificationService = emailNotificationService;
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

        return applyUserUpdates(existing, entity, true);
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

    public User getProfile(Long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(Long userId, User entity) {
        User existing = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return applyUserUpdates(existing, entity, false);
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

    public void sendPasswordResetOtp(String email) {
        if (!StringUtils.hasText(email)) {
            throw new RuntimeException("Email is required");
        }

        User user = repo.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!StringUtils.hasText(user.getPassword())) {
            throw new RuntimeException("This account uses Google sign-in. Continue with Google.");
        }

        String otp = generateOtp();
        passwordResetOtps.put(normalizeEmail(email), new PasswordResetOtp(passwordEncoder.encode(otp),
                System.currentTimeMillis() + RESET_OTP_TTL_MILLIS));
        emailNotificationService.sendPasswordResetOtp(user.getEmail(), user.getName(), otp);
    }

    public void resetPasswordWithOtp(String email, String otp, String newPassword) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(otp) || !StringUtils.hasText(newPassword)) {
            throw new RuntimeException("Email, OTP, and new password are required");
        }

        if (newPassword.trim().length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters");
        }

        String normalizedEmail = normalizeEmail(email);
        User user = repo.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetOtp storedOtp = passwordResetOtps.get(normalizedEmail);
        if (storedOtp == null || storedOtp.expiresAt() < System.currentTimeMillis()) {
            passwordResetOtps.remove(normalizedEmail);
            throw new RuntimeException("OTP expired. Request a new one.");
        }

        if (!passwordEncoder.matches(otp.trim(), storedOtp.hashedOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        repo.save(user);
        passwordResetOtps.remove(normalizedEmail);
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

    private User applyUserUpdates(User existing, User entity, boolean allowAnyRole) {
        if (!StringUtils.hasText(entity.getName())) {
            throw new RuntimeException("Name is required");
        }

        if (!StringUtils.hasText(entity.getEmail())) {
            throw new RuntimeException("Email is required");
        }

        repo.findByEmail(entity.getEmail())
                .filter(user -> !user.getId().equals(existing.getId()))
                .ifPresent(user -> {
                    throw new RuntimeException("Email already exists");
                });

        existing.setName(entity.getName());
        existing.setEmail(entity.getEmail());

        if (StringUtils.hasText(entity.getPassword())) {
            existing.setPassword(passwordEncoder.encode(entity.getPassword()));
        }

        if (entity.getRole() != null) {
            if (!allowAnyRole && existing.getRole() == Role.ADMIN) {
                existing.setRole(Role.ADMIN);
            } else {
                existing.setRole(allowAnyRole ? entity.getRole() : normalizeRole(entity.getRole()));
            }
        }

        return repo.save(existing);
    }

    private Role normalizeRole(Role role) {
        return role == Role.BUSINESS ? Role.BUSINESS : Role.USER;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim();
    }

    private String generateOtp() {
        int value = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(value);
    }

    private record PasswordResetOtp(String hashedOtp, long expiresAt) {
    }
}

