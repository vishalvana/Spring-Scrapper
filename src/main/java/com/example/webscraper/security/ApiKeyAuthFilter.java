package com.example.webscraper.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ApiKeyAuthFilter implements Filter {

    private final boolean securityEnabled;
    private final Set<String> validKeys;
    private final int requestsPerMinute;

    private final ConcurrentHashMap<String, RateWindow> windows = new ConcurrentHashMap<>();

    public ApiKeyAuthFilter(boolean securityEnabled, String apiKeysRaw, int requestsPerMinute) {
        this.securityEnabled = securityEnabled;
        this.requestsPerMinute = requestsPerMinute;
        this.validKeys = (apiKeysRaw == null || apiKeysRaw.isBlank())
                ? Set.of()
                : Arrays.stream(apiKeysRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();

        if (!securityEnabled || path.endsWith("/api/health")) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || !validKeys.contains(apiKey)) {
            writeError(response, 401, "Missing or invalid API key. Include a valid X-API-Key header.");
            return;
        }

        if (!allowRequest(apiKey)) {
            response.setHeader("Retry-After", "60");
            writeError(response, 429, "Rate limit exceeded (" + requestsPerMinute + " requests/minute). Try again shortly.");
            return;
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    private boolean allowRequest(String apiKey) {
        long nowMinuteBucket = Instant.now().getEpochSecond() / 60;
        RateWindow window = windows.computeIfAbsent(apiKey, k -> new RateWindow(nowMinuteBucket));

        synchronized (window) {
            if (window.minuteBucket != nowMinuteBucket) {
                window.minuteBucket = nowMinuteBucket;
                window.count.set(0);
            }
            return window.count.incrementAndGet() <= requestsPerMinute;
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        String reason = status == 401 ? "Unauthorized" : "Too Many Requests";
        response.getWriter().write(
                "{\"status\":" + status + ",\"error\":\"" + reason + "\",\"message\":\"" + message.replace("\"", "'") + "\"}");
    }

    private static class RateWindow {
        volatile long minuteBucket;
        final AtomicInteger count = new AtomicInteger(0);

        RateWindow(long minuteBucket) {
            this.minuteBucket = minuteBucket;
        }
    }
}