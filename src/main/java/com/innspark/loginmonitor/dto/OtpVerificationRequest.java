package com.innspark.loginmonitor.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpVerificationRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "OTP is required")
        String otp
) {
}