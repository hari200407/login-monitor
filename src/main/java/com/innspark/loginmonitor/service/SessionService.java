package com.innspark.loginmonitor.service;

import com.innspark.loginmonitor.entity.UserSession;
import com.innspark.loginmonitor.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final int SESSION_TIMEOUT_MINUTES = 15;

    private final UserSessionRepository userSessionRepository;

    public UserSession createSession(String username) {

        LocalDateTime now = LocalDateTime.now();

        UserSession session = new UserSession();

        session.setUsername(username);
        session.setSessionId(UUID.randomUUID().toString());
        session.setCreatedAt(now);
        session.setLastActivityAt(now);
        session.setActive(true);

        return userSessionRepository.save(session);
    }

    public boolean validateAndUpdateSession(String sessionId) {

        UserSession session = userSessionRepository
                .findBySessionId(sessionId)
                .orElse(null);

        if (session == null || !session.isActive()) {
            return false;
        }

        LocalDateTime expiryTime =
                session.getLastActivityAt()
                        .plusMinutes(SESSION_TIMEOUT_MINUTES);

        if (LocalDateTime.now().isAfter(expiryTime)) {

            session.setActive(false);
            userSessionRepository.save(session);

            return false;
        }

        session.setLastActivityAt(LocalDateTime.now());
        userSessionRepository.save(session);

        return true;
    }

    public List<UserSession> getActiveSessions() {

        expireInactiveSessions();

        return userSessionRepository.findByActiveTrue();
    }

    public List<UserSession> getActiveSessionsByUsername(
            String username) {

        expireInactiveSessions();

        return userSessionRepository
                .findByUsernameAndActiveTrue(username);
    }

    public boolean logout(String sessionId) {

        UserSession session = userSessionRepository
                .findBySessionId(sessionId)
                .orElse(null);

        if (session == null) {
            return false;
        }

        session.setActive(false);
        userSessionRepository.save(session);

        return true;
    }

    @Scheduled(fixedRate = 60000)
    public void expireInactiveSessions() {

        LocalDateTime expiryLimit =
                LocalDateTime.now()
                        .minusMinutes(SESSION_TIMEOUT_MINUTES);

        List<UserSession> activeSessions =
                userSessionRepository.findByActiveTrue();

        for (UserSession session : activeSessions) {

            if (session.getLastActivityAt()
                    .isBefore(expiryLimit)) {

                session.setActive(false);
                userSessionRepository.save(session);
            }
        }
    }
}