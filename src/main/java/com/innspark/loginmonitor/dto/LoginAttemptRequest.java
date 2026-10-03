package com.innspark.loginmonitor.dto;

import com.innspark.loginmonitor.entity.LoginStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginAttemptRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "IP address is required")
        String ipAddress,

        @NotNull(message = "Status is required")
        LoginStatus status
) {
}