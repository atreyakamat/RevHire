package com.revhire.notificationservice.service;

import com.revhire.notificationservice.entity.Notification;
import com.revhire.notificationservice.enums.NotificationChannel;
import com.revhire.notificationservice.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void testSendNotificationEmail_Success() {
        EmailService emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@revhire.com");

        Notification notification = Notification.builder()
                .id(1L)
                .recipientId(42L)
                .type(NotificationType.APPLICATION_SHORTLISTED)
                .channel(NotificationChannel.EMAIL)
                .title("Congratulations!")
                .message("You have been shortlisted.")
                .build();

        emailService.sendNotificationEmail(notification);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals("test@revhire.com", sentMessage.getFrom());
        assertNotNull(sentMessage.getTo());
        assertEquals("user42@revhire.com", sentMessage.getTo()[0]);
        assertEquals("Congratulations!", sentMessage.getSubject());
        assertEquals("You have been shortlisted.", sentMessage.getText());
    }

    @Test
    void testSendNotificationEmail_WhenMailSenderNull_SkipsGracefully() {
        EmailService emailService = new EmailService(null);
        Notification notification = Notification.builder()
                .id(2L)
                .recipientId(10L)
                .title("No sender")
                .message("Message")
                .build();

        assertDoesNotThrow(() -> emailService.sendNotificationEmail(notification));
    }

    @Test
    void testSendNotificationEmail_WhenMailSenderFails_ThrowsRuntimeException() {
        EmailService emailService = new EmailService(mailSender);
        Notification notification = Notification.builder()
                .id(3L)
                .recipientId(10L)
                .title("Failure test")
                .message("Message")
                .build();

        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> emailService.sendNotificationEmail(notification));
        assertTrue(thrown.getMessage().contains("Email delivery failed"));
    }
}
