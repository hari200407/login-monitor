package com.innspark.loginmonitor.controller;

import com.innspark.loginmonitor.dto.LoginAttemptRequest;
import com.innspark.loginmonitor.dto.LoginAttemptResponse;
import com.innspark.loginmonitor.entity.LoginStatus;
import com.innspark.loginmonitor.service.LoginAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginAttemptService loginAttemptService;

    @PostMapping
    public ResponseEntity<LoginAttemptResponse> recordLogin(
            @Valid @RequestBody LoginAttemptRequest request) {

        return ResponseEntity.ok(
                loginAttemptService.recordAttempt(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<LoginAttemptResponse>> getLoginAttempts(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) LoginStatus status,
            Authentication authentication) {

        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN")
                                        || authority.getAuthority().equals("ROLE_SUPERADMIN")
                        );

        // Regular USER can only see their own history
        if (!isAdmin) {

            String loggedInUsername =
                    authentication.getName();

            return ResponseEntity.ok(
                    loginAttemptService
                            .getByUsername(loggedInUsername)
            );
        }

        // ADMIN / SUPERADMIN can use filters
        if (username != null && !username.isBlank()) {

            return ResponseEntity.ok(
                    loginAttemptService
                            .getByUsername(username.trim())
            );
        }

        if (ip != null && !ip.isBlank()) {

            return ResponseEntity.ok(
                    loginAttemptService
                            .getByIpAddress(ip.trim())
            );
        }

        if (status != null) {

            return ResponseEntity.ok(
                    loginAttemptService
                            .getByStatus(status)
            );
        }

        return ResponseEntity.ok(
                loginAttemptService.getAllAttempts()
        );
    }
}