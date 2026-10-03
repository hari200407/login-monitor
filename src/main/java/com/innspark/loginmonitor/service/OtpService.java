package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.entity.OtpVerification;
import com.innspark.loginmonitor.entity.User;
import com.innspark.loginmonitor.repository.OtpVerificationRepository;
import com.innspark.loginmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public void generateAndSendOtp(String username) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        if (user.getEmail() == null ||
                user.getEmail().isBlank()) {

            throw new IllegalStateException(
                    "User does not have an email address"
            );
        }

        String otp = String.format(
                "%06d",
                new Random().nextInt(1_000_000)
        );

        OtpVerification verification =
                new OtpVerification();

        verification.setUsername(username);
        verification.setOtp(otp);
        verification.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );
        verification.setUsed(false);

        otpRepository.save(verification);

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(user.getEmail());
        message.setSubject(
                "Login Verification OTP"
        );

        message.setText(
                "Your login verification OTP is: "
                        + otp
                        + "\n\n"
                        + "This OTP expires in 5 minutes."
        );

        mailSender.send(message);
    }

    public boolean verifyOtp(
            String username,
            String otp) {

        OtpVerification verification =
                otpRepository
                        .findTopByUsernameAndUsedFalseOrderByIdDesc(
                                username
                        )
                        .orElse(null);

        if (verification == null) {
            return false;
        }

        if (LocalDateTime.now()
                .isAfter(verification.getExpiresAt())) {

            return false;
        }

        if (!verification.getOtp().equals(otp)) {
            return false;
        }

        verification.setUsed(true);
        otpRepository.save(verification);

        return true;
    }
}