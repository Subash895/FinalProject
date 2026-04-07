package com.smartCity.Web.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;

    public EmailNotificationService(ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:${spring.mail.username:}}") String fromAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
    }

    public void sendCommentThankYou(String recipientEmail, String recipientName, String targetLabel) {
        if (!StringUtils.hasText(recipientEmail)) {
            return;
        }

        sendEmail(recipientEmail,
                "Thank you for your comment",
                """
                        Hello %s,

                        Thank you for your comment on %s.

                        We appreciate your feedback.

                        Smart City Team
                        """.formatted(resolveName(recipientName), targetLabel));
    }

    public void sendBusinessCommentNotification(String ownerEmail, String ownerName, String businessName,
            String commenterName, String commentText) {
        if (!StringUtils.hasText(ownerEmail)) {
            return;
        }

        sendEmail(ownerEmail,
                "New comment for your business",
                """
                        Hello %s,

                        Your business "%s" received a new comment from %s.

                        Comment:
                        %s

                Smart City Team
                        """.formatted(resolveName(ownerName), businessName, resolveName(commenterName), commentText));
    }

    public void sendPasswordResetOtp(String recipientEmail, String recipientName, String otp) {
        if (!StringUtils.hasText(recipientEmail)) {
            return;
        }

        sendEmail(recipientEmail,
                "Your Smart City password reset OTP",
                """
                        Hello %s,

                        We received a password reset request for your Smart City account.

                        Your OTP is: %s

                        This OTP will expire in 10 minutes.
                        If you did not request this reset, you can ignore this email.

                        Smart City Team
                        """.formatted(resolveName(recipientName), otp));
    }

    private void sendEmail(String to, String subject, String text) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.debug("Skipping email because JavaMailSender is not configured. Subject: {}", subject);
            throw new IllegalStateException("Email service is not configured on the server");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (StringUtils.hasText(fromAddress)) {
                message.setFrom(fromAddress);
            }
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send email with subject '{}' to '{}': {}", subject, to, ex.getMessage());
            throw new RuntimeException("Unable to send email. Check SMTP configuration.", ex);
        }
    }

    private String resolveName(String name) {
        return StringUtils.hasText(name) ? name : "User";
    }
}
