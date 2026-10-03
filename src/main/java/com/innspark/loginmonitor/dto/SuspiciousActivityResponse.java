package com.innspark.loginmonitor.dto;

import java.time.LocalDateTime;

public record SuspiciousActivityResponse(

        Long id,

        String ipAddress,

        String username,

        String reason,

        LocalDateTime timestamp
) {
}