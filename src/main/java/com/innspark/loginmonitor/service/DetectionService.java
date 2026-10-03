package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.entity.LoginAttempt;
import com.innspark.loginmonitor.entity.LoginStatus;
import com.innspark.loginmonitor.entity.SuspiciousActivity;
import com.innspark.loginmonitor.repository.LoginAttemptRepository;
import com.innspark.loginmonitor.repository.SuspiciousActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DetectionService {

    private static final int FAILURE_THRESHOLD = 5;
    private static final int WINDOW_MINUTES = 10;

    private final LoginAttemptRepository loginAttemptRepository;
    private final SuspiciousActivityRepository suspiciousActivityRepository;
    private final NotificationService notificationService;

    public void analyze(LoginAttempt attempt) {

        LocalDateTime windowStart =
                attempt.getTimestamp()
                        .minusMinutes(WINDOW_MINUTES);

        /*
         * Rule 1:
         * Multiple failures from the same IP
         * within 10 minutes.
         */
        long ipFailures =
                loginAttemptRepository
                        .countByIpAddressAndStatusAndTimestampAfter(
                                attempt.getIpAddress(),
                                LoginStatus.FAILURE,
                                windowStart
                        );

        if (ipFailures >= FAILURE_THRESHOLD) {

            createSuspiciousActivity(
                    attempt,
                    null,
                    "5 failed logins from the same IP in 10 minutes"
            );
        }

        /*
         * Rule 2:
         * Multiple failures for the same username
         * within 10 minutes.
         */
        long userFailures =
                loginAttemptRepository
                        .countByUsernameAndStatusAndTimestampAfter(
                                attempt.getUsername(),
                                LoginStatus.FAILURE,
                                windowStart
                        );

        if (userFailures >= FAILURE_THRESHOLD) {

            createSuspiciousActivity(
                    attempt,
                    attempt.getUsername(),
                    "5 failed logins for the same user in 10 minutes"
            );
        }

        /*
         * Rule 3:
         * Successful login after multiple failures.
         */
        if (attempt.getStatus() == LoginStatus.SUCCESS) {

            long recentFailures =
                    loginAttemptRepository
                            .countByUsernameAndStatusAndTimestampAfter(
                                    attempt.getUsername(),
                                    LoginStatus.FAILURE,
                                    windowStart
                            );

            if (recentFailures >= 3) {

                createSuspiciousActivity(
                        attempt,
                        attempt.getUsername(),
                        "Successful login after multiple failed attempts"
                );
            }
        }
    }

    private void createSuspiciousActivity(
            LoginAttempt attempt,
            String username,
            String reason) {

        LocalDateTime cooldownStart =
                attempt.getTimestamp()
                        .minusMinutes(WINDOW_MINUTES);

        boolean alreadyExists =
                suspiciousActivityRepository
                        .existsByIpAddressAndUsernameAndReasonAndTimestampAfter(
                                attempt.getIpAddress(),
                                username,
                                reason,
                                cooldownStart
                        );

        if (alreadyExists) {
            return;
        }

        SuspiciousActivity activity = new SuspiciousActivity();

        activity.setIpAddress(attempt.getIpAddress());
        activity.setUsername(username);
        activity.setReason(reason);
        activity.setTimestamp(LocalDateTime.now());

        suspiciousActivityRepository.save(activity);

        notificationService.notifySuspiciousActivity(
                attempt.getIpAddress(),
                username,
                reason
        );
    }
}