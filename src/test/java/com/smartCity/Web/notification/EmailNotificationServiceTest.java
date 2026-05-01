package com.smartCity.Web.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

  @Mock private JavaMailSender javaMailSender;

  private EmailNotificationService emailNotificationService;
  private StaticObjectProvider<JavaMailSender> mailSenderProvider;

  @BeforeEach
  void setUp() {
    mailSenderProvider = new StaticObjectProvider<>(javaMailSender);
    emailNotificationService = new EmailNotificationService(mailSenderProvider, "noreply@example.com");
  }

  @Test
  void sendCommentThankYouSkipsBlankRecipient() {
    emailNotificationService.sendCommentThankYou("   ", "User", "the discussion");
    verify(javaMailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
  }

  @Test
  void sendBusinessCommentNotificationBuildsMailMessage() {
    emailNotificationService.sendBusinessCommentNotification(
        "owner@example.com", "Owner", "Smart Cafe", "Riya", "Nice place");

    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(javaMailSender).send(captor.capture());
    SimpleMailMessage message = captor.getValue();
    assertEquals("owner@example.com", message.getTo()[0]);
    assertEquals("New comment for your business", message.getSubject());
  }

  @Test
  void sendPasswordResetOtpFailsWhenMailSenderMissing() {
    emailNotificationService = new EmailNotificationService(new StaticObjectProvider<>(null), "noreply@example.com");

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> emailNotificationService.sendPasswordResetOtp("user@example.com", "User", "123456"));

    assertEquals("Email service is not configured on the server", exception.getMessage());
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
