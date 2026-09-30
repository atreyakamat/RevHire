package com.revhire.notificationservice.service;

import com.revhire.notificationservice.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@revhire.com}")
    private String fromEmail;

    public EmailService(@org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send notification email
     * Note: This is a simplified version. In production, use HTML templates.
     */
    public void sendNotificationEmail(Notification notification) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Skipping email delivery for notification ID: {}", notification.getId());
            return;
        }
        try {
            // In production, you would fetch the user's email from User Service
            // For now, we'll use a placeholder
            String recipientEmail = getRecipientEmail(notification.getRecipientId());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(notification.getTitle());
            message.setText(notification.getMessage());

            mailSender.send(message);

            log.info("Email sent successfully to: {} for notification ID: {}",
                    recipientEmail, notification.getId());
        } catch (Exception e) {
            log.error("Failed to send email for notification ID: {}", notification.getId(), e);
            throw new RuntimeException("Email delivery failed", e);
        }
    }

    /**
     * Get recipient email address.
     * Architectural note: Currently using generated user email format. When UserServiceClient
     * is integrated, this should look up the recipient's primary email from User Service.
     */
    private String getRecipientEmail(Long recipientId) {
        // Fallback default: format recipient email until UserClient lookup is wired
        return "user" + recipientId + "@revhire.com";
    }
}