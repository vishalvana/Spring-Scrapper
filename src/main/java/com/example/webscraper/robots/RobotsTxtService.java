package com.example.webscraper.robots;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RobotsTxtService {

    private static final Logger log = LoggerFactory.getLogger(RobotsTxtService.class);

    @Value("${scraper.robots.enabled:true}")
    private boolean robotsCheckEnabled;

    @Value("${scraper.robots.user-agent:SpringBootWebScraper}")
    private String userAgentToken;

    @Value("${scraper.robots.timeout-ms:5000}")
    private int timeoutMs;

    private final Map<String, RobotsRules> cache = new ConcurrentHashMap<>();

    public boolean isRobotsCheckEnabled() {
        return robotsCheckEnabled;
    }

    public boolean isAllowed(String urlString) {
        if (!robotsCheckEnabled) {
            return true;
        }
        try {
            URI uri = URI.create(urlString);
            String host = uri.getScheme() + "://" + uri.getAuthority();
            String path = uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/" : uri.getRawPath();

            RobotsRules rules = cache.computeIfAbsent(host, this::fetchAndParse);
            return rules.isAllowed(path);

        } catch (Exception e) {
            log.warn("Could not evaluate robots.txt for {}: {}", urlString, e.getMessage());
            return true;
        }
    }

    private RobotsRules fetchAndParse(String host) {
        String robotsUrl = host + "/robots.txt";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(robotsUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgentToken);
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);
            connection.setInstanceFollowRedirects(true);

            int status = connection.getResponseCode();
            if (status >= 400) {
                log.debug("No robots.txt found at {} (HTTP {}) — allowing all paths.", robotsUrl, status);
                return RobotsRules.allowAll();
            }

            try (InputStream is = connection.getInputStream()) {
                return parse(is, userAgentToken);
            }

        } catch (IOException e) {
            log.debug("robots.txt unreachable at {} ({}) — allowing all paths.", robotsUrl, e.getMessage());
            return RobotsRules.allowAll();
        }
    }

    private RobotsRules parse(InputStream is, String agentToken) throws IOException {
        List<String> disallowForUs = new ArrayList<>();
        List<String> allowForUs = new ArrayList<>();
        List<String> disallowForAll = new ArrayList<>();
        List<String> allowForAll = new ArrayList<>();

        boolean inOurGroup = false;
        boolean inWildcardGroup = false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = stripComment(line).trim();
                if (line.isEmpty()) continue;

                int colon = line.indexOf(':');
                if (colon < 0) continue;

                String directive = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                String value = line.substring(colon + 1).trim();

                switch (directive) {
                    case "user-agent" -> {
                        inOurGroup = value.equalsIgnoreCase(agentToken);
                        inWildcardGroup = value.equals("*");
                    }
                    case "disallow" -> {
                        if (!value.isEmpty()) {
                            if (inOurGroup) disallowForUs.add(value);
                            else if (inWildcardGroup) disallowForAll.add(value);
                        }
                    }
                    case "allow" -> {
                        if (!value.isEmpty()) {
                            if (inOurGroup) allowForUs.add(value);
                            else if (inWildcardGroup) allowForAll.add(value);
                        }
                    }
                    default -> { /* ignore crawl-delay, sitemap, etc. */ }
                }
            }
        }

        if (!disallowForUs.isEmpty() || !allowForUs.isEmpty()) {
            return new RobotsRules(allowForUs, disallowForUs);
        }
        return new RobotsRules(allowForAll, disallowForAll);
    }

    private String stripComment(String line) {
        int hash = line.indexOf('#');
        return hash >= 0 ? line.substring(0, hash) : line;
    }

    public void clearCache() {
        cache.clear();
    }

    private static class RobotsRules {
        private final List<String> allow;
        private final List<String> disallow;

        RobotsRules(List<String> allow, List<String> disallow) {
            this.allow = allow;
            this.disallow = disallow;
        }

        static RobotsRules allowAll() {
            return new RobotsRules(List.of(), List.of());
        }

        boolean isAllowed(String path) {
            String bestAllowMatch = longestMatch(allow, path);
            String bestDisallowMatch = longestMatch(disallow, path);

            if (bestDisallowMatch == null) return true;
            if (bestAllowMatch == null) return false;
            return bestAllowMatch.length() >= bestDisallowMatch.length();
        }

        private String longestMatch(List<String> prefixes, String path) {
            String best = null;
            for (String prefix : prefixes) {
                if (path.startsWith(prefix) && (best == null || prefix.length() > best.length())) {
                    best = prefix;
                }
            }
            return best;
        }
    }
}