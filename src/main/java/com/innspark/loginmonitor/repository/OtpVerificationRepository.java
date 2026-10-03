package com.innspark.loginmonitor.repository;

import com.innspark.loginmonitor.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification>
    findTopByUsernameAndUsedFalseOrderByIdDesc(
            String username
    );
}