package com.innspark.loginmonitor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, RequestCounter> requestCounters =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        boolean isLoginRequest =
                request.getMethod().equalsIgnoreCase("POST")
                        && (
                        request.getRequestURI().equals("/auth/login")
                                || request.getRequestURI().equals("/login")
                );

        if (!isLoginRequest) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);

        Instant now = Instant.now();

        RequestCounter counter = requestCounters.compute(
                clientIp,
                (ip, existing) -> {

                    if (existing == null
                            || now.getEpochSecond() - existing.startTime()
                            >= WINDOW_SECONDS) {

                        return new RequestCounter(
                                now.getEpochSecond(),
                                1
                        );
                    }

                    return new RequestCounter(
                            existing.startTime(),
                            existing.count() + 1
                    );
                }
        );

        if (counter.count() > MAX_REQUESTS) {

            response.setStatus(429);
            response.setContentType("application/json");

            response.getWriter().write("""
                    {
                        "error": "Too many requests",
                        "message": "Rate limit exceeded. Try again later."
                    }
                    """);

            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private record RequestCounter(
            long startTime,
            int count
    ) {
    }
}