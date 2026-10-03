package com.innspark.loginmonitor.repository;

import com.innspark.loginmonitor.entity.LoginAttempt;
import com.innspark.loginmonitor.entity.LoginStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginAttemptRepository
        extends JpaRepository<LoginAttempt, Long> {

    List<LoginAttempt> findByUsernameOrderByTimestampDesc(
            String username
    );

    List<LoginAttempt> findByIpAddressOrderByTimestampDesc(
            String ipAddress
    );

    List<LoginAttempt> findByStatusOrderByTimestampDesc(
            LoginStatus status
    );

    List<LoginAttempt> findByTimestampAfter(
            LocalDateTime timestamp
    );

    long countByIpAddressAndStatusAndTimestampAfter(
            String ipAddress,
            LoginStatus status,
            LocalDateTime timestamp
    );

    long countByUsernameAndStatusAndTimestampAfter(
            String username,
            LoginStatus status,
            LocalDateTime timestamp
    );
}