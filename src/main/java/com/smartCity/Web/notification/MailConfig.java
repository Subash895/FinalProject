package com.smartCity.Web.notification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

/**
 * Registers the mail-related beans used to send application emails.
 */
@Configuration
@EnableAsync
public class MailConfig {

  @Bean
  public JavaMailSender javaMailSender() {
    String host = resolve("spring.mail.host", "SPRING_MAIL_HOST");
    String username = resolve("spring.mail.username", "SPRING_MAIL_USERNAME");
    String password = resolve("spring.mail.password", "SPRING_MAIL_PASSWORD");
    String portValue = resolve("spring.mail.port", "SPRING_MAIL_PORT");
    String smtpAuth =
        resolve("spring.mail.properties.mail.smtp.auth", "SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH");
    String startTls =
        resolve(
            "spring.mail.properties.mail.smtp.starttls.enable",
            "SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE");

    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(parsePort(portValue));
    mailSender.setUsername(username);
    mailSender.setPassword(password);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.smtp.auth", StringUtils.hasText(smtpAuth) ? smtpAuth : "true");
    props.put("mail.smtp.starttls.enable", StringUtils.hasText(startTls) ? startTls : "true");
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.debug", "false");

    return mailSender;
  }

  private String resolve(String propertyKey, String envKey) {
    String value = System.getProperty(propertyKey);
    if (StringUtils.hasText(value)) {
      return value.trim();
    }

    value = System.getenv(envKey);
    if (StringUtils.hasText(value)) {
      return value.trim();
    }

    value = readFromDotEnv(envKey);
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private int parsePort(String portValue) {
    if (!StringUtils.hasText(portValue)) {
      return 587;
    }

    try {
      return Integer.parseInt(portValue.trim());
    } catch (NumberFormatException ex) {
      return 587;
    }
  }

  private String readFromDotEnv(String key) {
    Path path = Path.of(".env");
    if (!Files.exists(path)) {
      return null;
    }

    try {
      List<String> lines = Files.readAllLines(path);
      for (String line : lines) {
        String trimmed = line.trim();
        if (!StringUtils.hasText(trimmed) || trimmed.startsWith("#") || !trimmed.contains("=")) {
          continue;
        }

        int separatorIndex = trimmed.indexOf('=');
        String currentKey = trimmed.substring(0, separatorIndex).trim();
        if (key.equals(currentKey)) {
          return trimmed.substring(separatorIndex + 1).trim();
        }
      }
    } catch (IOException ignored) {
      return null;
    }

    return null;
  }
}
