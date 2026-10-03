package com.innspark.loginmonitor.controller;

import com.innspark.loginmonitor.dto.LoginRequest;
import com.innspark.loginmonitor.dto.OtpVerificationRequest;
import com.innspark.loginmonitor.entity.LoginStatus;
import com.innspark.loginmonitor.repository.LoginAttemptRepository;
import com.innspark.loginmonitor.service.LoginAttemptService;
import com.innspark.loginmonitor.service.OtpService;
import com.innspark.loginmonitor.security.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int CAPTCHA_THRESHOLD = 3;

    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final CaptchaService captchaService;
    private final LoginAttemptRepository loginAttemptRepository;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String username =
                request.username().trim();

        String ipAddress =
                getClientIp(httpRequest);

        LocalDateTime windowStart =
                LocalDateTime.now().minusMinutes(10);

        long recentFailures =
                loginAttemptRepository
                        .countByUsernameAndStatusAndTimestampAfter(
                                username,
                                LoginStatus.FAILURE,
                                windowStart
                        );

        // CAPTCHA becomes mandatory after several failures
        if (recentFailures >= CAPTCHA_THRESHOLD) {

            if (request.captchaToken() == null ||
                    request.captchaToken().isBlank()) {

                return ResponseEntity
                        .status(429)
                        .body(
                                "CAPTCHA required after multiple failed login attempts"
                        );
            }

            if (!captchaService.verify(
                    request.captchaToken())) {

                return ResponseEntity
                        .status(403)
                        .body(
                                "CAPTCHA verification failed"
                        );
            }
        }

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            request.password()
                    )
            );

            // Record successful login
            loginAttemptService.recordAttempt(
                    new com.innspark.loginmonitor.dto.LoginAttemptRequest(
                            username,
                            ipAddress,
                            LoginStatus.SUCCESS
                    )
            );

            otpService.generateAndSendOtp(username);

            return ResponseEntity.ok(
                    "Username and password verified. OTP sent to registered email."
            );

        } catch (AuthenticationException ex) {

            // Record failed login
            loginAttemptService.recordAttempt(
                    new com.innspark.loginmonitor.dto.LoginAttemptRequest(
                            username,
                            ipAddress,
                            LoginStatus.FAILURE
                    )
            );

            return ResponseEntity
                    .status(401)
                    .body("Invalid username or password");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody OtpVerificationRequest request) {

        boolean valid =
                otpService.verifyOtp(
                        request.username().trim(),
                        request.otp().trim()
                );

        if (!valid) {

            return ResponseEntity
                    .status(401)
                    .body("Invalid or expired OTP");
        }

        return ResponseEntity.ok(
                "Two-factor authentication successful"
        );
    }

    private String getClientIp(
            HttpServletRequest request) {

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null &&
                !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request.getRemoteAddr();
    }
}