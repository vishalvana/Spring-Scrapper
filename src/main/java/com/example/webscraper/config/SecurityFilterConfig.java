package com.example.webscraper.config;

import com.example.webscraper.security.ApiKeyAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityFilterConfig {

    @Value("${scraper.security.enabled:false}")
    private boolean securityEnabled;

    @Value("${scraper.security.api-keys:}")
    private String apiKeysRaw;

    @Value("${scraper.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Bean
    public FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilter() {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(securityEnabled, apiKeysRaw, requestsPerMinute);

        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*"); // static UI at "/" stays unaffected
        registration.setOrder(1);
        return registration;
    }
}