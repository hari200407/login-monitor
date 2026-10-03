package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.dto.LoginAttemptRequest;
import com.innspark.loginmonitor.dto.LoginAttemptResponse;
import com.innspark.loginmonitor.entity.LoginAttempt;
import com.innspark.loginmonitor.entity.LoginStatus;
import com.innspark.loginmonitor.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final DetectionService detectionService;
    private final SessionService sessionService;


    public LoginAttemptResponse recordAttempt(
            LoginAttemptRequest request
    ) {

        LoginAttempt attempt =
                new LoginAttempt();

        attempt.setUsername(
                request.username().trim()
        );

        attempt.setIpAddress(
                request.ipAddress().trim()
        );

        attempt.setStatus(
                request.status()
        );

        attempt.setTimestamp(
                LocalDateTime.now()
        );

        LoginAttempt saved = loginAttemptRepository.save(attempt);

        detectionService.analyze(saved);

        if (saved.getStatus() == LoginStatus.SUCCESS) {
            sessionService.createSession(saved.getUsername());
        }

        return toResponse(saved);
    }


    public List<LoginAttemptResponse> getAllAttempts() {

        return loginAttemptRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public List<LoginAttemptResponse> getByUsername(
            String username
    ) {

        return loginAttemptRepository
                .findByUsernameOrderByTimestampDesc(
                        username
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public List<LoginAttemptResponse> getByIpAddress(
            String ipAddress
    ) {

        return loginAttemptRepository
                .findByIpAddressOrderByTimestampDesc(
                        ipAddress
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public List<LoginAttemptResponse> getByStatus(
            LoginStatus status
    ) {

        return loginAttemptRepository
                .findByStatusOrderByTimestampDesc(
                        status
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }


    private LoginAttemptResponse toResponse(
            LoginAttempt attempt
    ) {

        return new LoginAttemptResponse(
                attempt.getId(),
                attempt.getUsername(),
                attempt.getIpAddress(),
                attempt.getTimestamp(),
                attempt.getStatus()
        );
    }
}