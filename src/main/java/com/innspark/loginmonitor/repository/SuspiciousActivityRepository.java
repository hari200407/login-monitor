package com.innspark.loginmonitor.repository;

import com.innspark.loginmonitor.entity.SuspiciousActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SuspiciousActivityRepository
        extends JpaRepository<SuspiciousActivity, Long> {

    List<SuspiciousActivity> findAllByOrderByTimestampDesc();

    boolean existsByIpAddressAndUsernameAndReasonAndTimestampAfter(
            String ipAddress,
            String username,
            String reason,
            LocalDateTime timestamp
    );
}