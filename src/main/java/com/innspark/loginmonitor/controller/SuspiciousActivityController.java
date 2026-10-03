package com.innspark.loginmonitor.controller;

import com.innspark.loginmonitor.dto.SuspiciousActivityResponse;
import com.innspark.loginmonitor.entity.SuspiciousActivity;
import com.innspark.loginmonitor.repository.SuspiciousActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suspicious")
@RequiredArgsConstructor
public class SuspiciousActivityController {

    private final SuspiciousActivityRepository suspiciousActivityRepository;

    @GetMapping
    public ResponseEntity<List<SuspiciousActivityResponse>> getSuspiciousActivities() {

        List<SuspiciousActivityResponse> response =
                suspiciousActivityRepository.findAllByOrderByTimestampDesc()
                        .stream()
                        .map(activity -> new SuspiciousActivityResponse(
                                activity.getId(),
                                activity.getIpAddress(),
                                activity.getUsername(),
                                activity.getReason(),
                                activity.getTimestamp()
                        ))
                        .toList();

        return ResponseEntity.ok(response);
    }
}