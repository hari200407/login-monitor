package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.entity.User;
import com.innspark.loginmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${notification.admin-email}")
    private String adminEmail;

    public void notifySuspiciousActivity(
            String ipAddress,
            String username,
            String reason
    ) {

        // Notify admin
        sendEmail(
                adminEmail,
                "Suspicious Login Activity Detected",
                buildMessage(ipAddress, username, reason)
        );

        // Notify affected user
        if (username != null && !username.isBlank()) {

            userRepository.findByUsername(username)
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isBlank())
                    .ifPresent(email ->
                            sendEmail(
                                    email,
                                    "Security Alert - Suspicious Login Activity",
                                    buildMessage(ipAddress, username, reason)
                            )
                    );
        }
    }

    private String buildMessage(
            String ipAddress,
            String username,
            String reason
    ) {

        return """
                Suspicious login activity was detected.

                Username: %s
                IP Address: %s
                Reason: %s

                Please review the login activity.
                """.formatted(
                username != null ? username : "Unknown",
                ipAddress,
                reason
        );
    }

    private void sendEmail(
            String to,
            String subject,
            String body
    ) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

        } catch (Exception e) {

            // Email failure must not break login monitoring
            System.err.println(
                    "Failed to send notification email: "
                            + e.getMessage()
            );
        }
    }
}