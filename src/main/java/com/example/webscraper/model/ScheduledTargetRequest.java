package com.example.webscraper.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ScheduledTargetRequest {

    @NotBlank(message = "url must not be blank")
    @Pattern(regexp = "^https?://.+", message = "url must start with http:// or https://")
    private String url;

    @Min(value = 1, message = "intervalMinutes must be at least 1")
    private int intervalMinutes = 60; // default: hourly

    public ScheduledTargetRequest() {
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(int intervalMinutes) { this.intervalMinutes = intervalMinutes; }
}