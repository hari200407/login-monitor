package com.innspark.loginmonitor.dto;

import com.innspark.loginmonitor.entity.LoginStatus;

import java.time.LocalDateTime;

public record LoginAttemptResponse(

        Long id,

        String username,

        String ipAddress,

        LocalDateTime timestamp,

        LoginStatus status
) {
}