package com.innspark.loginmonitor.controller;

import com.innspark.loginmonitor.entity.UserSession;
import com.innspark.loginmonitor.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<UserSession> createSession(
            @RequestParam String username,
            Authentication authentication) {

        // Users can only create a session for themselves.
        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                sessionService.createSession(username)
        );
    }

    @GetMapping
    public ResponseEntity<List<UserSession>> getSessions(
            @RequestParam(required = false) String username,
            Authentication authentication) {

        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN")
                                        || authority.getAuthority().equals("ROLE_SUPERADMIN")
                        );

        if (isAdmin) {

            if (username != null && !username.isBlank()) {

                return ResponseEntity.ok(
                        sessionService
                                .getActiveSessionsByUsername(
                                        username.trim()
                                )
                );
            }

            return ResponseEntity.ok(
                    sessionService.getActiveSessions()
            );
        }

        // USER can only see their own sessions
        if (username != null &&
                !username.equals(authentication.getName())) {

            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(
                sessionService.getActiveSessionsByUsername(
                        authentication.getName()
                )
        );
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateSession(
            @RequestParam String sessionId) {

        boolean valid =
                sessionService
                        .validateAndUpdateSession(sessionId);

        if (!valid) {
            return ResponseEntity
                    .status(401)
                    .body("Session expired or invalid");
        }

        return ResponseEntity.ok(
                "Session is active"
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestParam String sessionId) {

        boolean loggedOut =
                sessionService.logout(sessionId);

        if (!loggedOut) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                "Logged out successfully"
        );
    }
}