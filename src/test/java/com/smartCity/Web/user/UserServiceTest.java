package com.smartCity.Web.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartCity.Web.auth.google.GoogleIdTokenVerifier;
import com.smartCity.Web.auth.google.GoogleIdTokenVerifier.VerifiedGoogleUser;
import com.smartCity.Web.auth.google.GoogleAuthProperties;
import com.smartCity.Web.notification.EmailNotificationService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JavaMailSender javaMailSender;

  private UserService userService;
  private FakeGoogleIdTokenVerifier googleIdTokenVerifier;
  private EmailNotificationService emailNotificationService;

  @BeforeEach
  void setUp() {
    googleIdTokenVerifier = new FakeGoogleIdTokenVerifier();
    emailNotificationService =
        new EmailNotificationService(new StaticObjectProvider<>(javaMailSender), "noreply@example.com");
    userService =
        new UserService(
            userRepository, passwordEncoder, googleIdTokenVerifier, emailNotificationService);
  }

  @Test
  void registerNormalizesRoleAndHashesPassword() {
    User user = new User("Riya", "riya@example.com", "password123", Role.ADMIN);
    when(userRepository.findByEmail("riya@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("password123")).thenReturn("encoded");
    when(userRepository.save(user)).thenReturn(user);

    User saved = userService.register(user);

    assertSame(user, saved);
    assertEquals(Role.USER, user.getRole());
    assertEquals("encoded", user.getPassword());
    assertNull(user.getGoogleSubject());
  }

  @Test
  void loginRejectsWrongPassword() {
    User user = new User("Riya", "riya@example.com", "encoded", Role.USER);
    when(userRepository.findByEmail("riya@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> userService.login("riya@example.com", "wrong"));

    assertEquals("Invalid password", exception.getMessage());
  }

  @Test
  void updateProfileKeepsAdminRoleForAdminUser() {
    User existing = new User("Admin", "admin@example.com", "old", Role.ADMIN);
    existing.setId(1L);
    User incoming = new User("Admin Updated", "admin2@example.com", "", Role.USER);

    when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(userRepository.findByEmail("admin2@example.com")).thenReturn(Optional.empty());
    when(userRepository.save(existing)).thenReturn(existing);

    User saved = userService.updateProfile(1L, incoming);

    assertSame(existing, saved);
    assertEquals(Role.ADMIN, existing.getRole());
    assertEquals("admin2@example.com", existing.getEmail());
  }

  @Test
  void sendPasswordResetOtpRejectsGoogleOnlyAccount() {
    User user = new User("Google User", "google@example.com", null, Role.USER);
    when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(user));

    RuntimeException exception =
        assertThrows(
            RuntimeException.class, () -> userService.sendPasswordResetOtp("google@example.com"));

    assertEquals("This account uses Google sign-in. Continue with Google.", exception.getMessage());
  }

  @Test
  void resetPasswordWithOtpRejectsUnknownOtp() {
    User user = new User("Riya", "riya@example.com", "encoded", Role.USER);
    when(userRepository.findByEmail("riya@example.com")).thenReturn(Optional.of(user));

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> userService.resetPasswordWithOtp("riya@example.com", "123456", "password123"));

    assertEquals("OTP expired. Request a new one.", exception.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void loginWithGoogleCreatesNewUserUsingRequestedBusinessRole() {
    VerifiedGoogleUser verifiedGoogleUser =
        new VerifiedGoogleUser("sub-1", "biz@example.com", "Business Owner");
    googleIdTokenVerifier.verifiedGoogleUser = verifiedGoogleUser;
    when(userRepository.findByEmail("biz@example.com")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    User saved = userService.loginWithGoogle("credential", Role.BUSINESS);

    assertEquals("biz@example.com", saved.getEmail());
    assertEquals(Role.BUSINESS, saved.getRole());
    assertEquals("sub-1", saved.getGoogleSubject());
  }

  @Test
  void sendPasswordResetOtpStoresOtpAndEmailsUser() {
    User user = new User("Riya", "riya@example.com", "encoded", Role.USER);
    when(userRepository.findByEmail("riya@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(any(String.class))).thenReturn("hashed-otp");

    userService.sendPasswordResetOtp("riya@example.com");

    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(javaMailSender).send(captor.capture());
    SimpleMailMessage message = captor.getValue();
    assertEquals("riya@example.com", message.getTo()[0]);
    assertEquals("Your Smart City password reset OTP", message.getSubject());
  }

  private static final class FakeGoogleIdTokenVerifier extends GoogleIdTokenVerifier {
    private VerifiedGoogleUser verifiedGoogleUser;

    private FakeGoogleIdTokenVerifier() {
      super(new GoogleAuthProperties());
    }

    @Override
    public VerifiedGoogleUser verify(String credential) {
      return verifiedGoogleUser;
    }

    @Override
    public boolean isConfigured() {
      return true;
    }

    @Override
    public String getClientId() {
      return "client-id";
    }
  }

  private static final class StaticObjectProvider<T> implements ObjectProvider<T> {
    private final T value;

    private StaticObjectProvider(T value) {
      this.value = value;
    }

    @Override
    public T getObject(Object... args) {
      return value;
    }

    @Override
    public T getIfAvailable() {
      return value;
    }

    @Override
    public T getIfUnique() {
      return value;
    }

    @Override
    public T getObject() {
      return value;
    }
  }
}
