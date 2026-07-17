package com.example.webscraper.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "scheduled_targets")
public class ScheduledTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    /** How often (in minutes) this URL should be re-scraped. */
    @Column(nullable = false)
    private int intervalMinutes;

    @Column(nullable = false)
    private boolean active = true;

    private Instant lastRunAt;

    @Column(nullable = false)
    private Instant createdAt;

    public ScheduledTarget() {
    }

    public ScheduledTarget(String url, int intervalMinutes) {
        this.url = url;
        this.intervalMinutes = intervalMinutes;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(int intervalMinutes) { this.intervalMinutes = intervalMinutes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(Instant lastRunAt) { this.lastRunAt = lastRunAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    /** True if this target has never run, or enough time has passed since its last run. */
    public boolean isDue(Instant now) {
        if (!active) return false;
        if (lastRunAt == null) return true;
        return lastRunAt.plusSeconds(intervalMinutes * 60L).isBefore(now);
    }
}