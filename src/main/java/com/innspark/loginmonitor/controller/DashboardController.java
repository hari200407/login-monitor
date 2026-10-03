package com.innspark.loginmonitor.controller;

import com.innspark.loginmonitor.entity.LoginAttempt;
import com.innspark.loginmonitor.entity.LoginStatus;
import com.innspark.loginmonitor.entity.SuspiciousActivity;
import com.innspark.loginmonitor.repository.LoginAttemptRepository;
import com.innspark.loginmonitor.repository.SuspiciousActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final LoginAttemptRepository loginAttemptRepository;
    private final SuspiciousActivityRepository suspiciousActivityRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public Map<String, Object> getStats() {

        List<LoginAttempt> attempts = loginAttemptRepository.findAll();

        long totalAttempts = attempts.size();

        long successfulAttempts = attempts.stream()
                .filter(a -> a.getStatus() == LoginStatus.SUCCESS)
                .count();

        long failedAttempts = attempts.stream()
                .filter(a -> a.getStatus() == LoginStatus.FAILURE)
                .count();

        long suspiciousActivities =
                suspiciousActivityRepository.count();

        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalAttempts", totalAttempts);
        stats.put("successfulAttempts", successfulAttempts);
        stats.put("failedAttempts", failedAttempts);
        stats.put("suspiciousActivities", suspiciousActivities);

        return stats;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public List<Map<String, Object>> getUserActivity() {

        List<LoginAttempt> attempts =
                loginAttemptRepository.findAll();

        return attempts.stream()
                .collect(Collectors.groupingBy(
                        LoginAttempt::getUsername,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> {

                    String username = entry.getKey();
                    List<LoginAttempt> userAttempts = entry.getValue();

                    long successCount = userAttempts.stream()
                            .filter(a ->
                                    a.getStatus() == LoginStatus.SUCCESS)
                            .count();

                    long failureCount = userAttempts.stream()
                            .filter(a ->
                                    a.getStatus() == LoginStatus.FAILURE)
                            .count();

                    Map<String, Object> result =
                            new LinkedHashMap<>();

                    result.put("username", username);
                    result.put("totalAttempts", userAttempts.size());
                    result.put("successfulAttempts", successCount);
                    result.put("failedAttempts", failureCount);

                    return result;
                })
                .sorted(Comparator.comparing(
                        m -> (String) m.get("username")
                ))
                .toList();
    }

    @GetMapping("/suspicious")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public List<SuspiciousActivity> getSuspiciousTimeline() {

        return suspiciousActivityRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                SuspiciousActivity::getTimestamp
                        ).reversed()
                )
                .toList();
    }
}