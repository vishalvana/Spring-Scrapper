package com.example.webscraper.model;

import java.time.Instant;

public class ScrapeHistoryItem {

    private Long id;
    private String url;
    private int statusCode;
    private String title;
    private Instant scrapedAt;

    public ScrapeHistoryItem() {
    }

    public ScrapeHistoryItem(Long id, String url, int statusCode, String title, Instant scrapedAt) {
        this.id = id;
        this.url = url;
        this.statusCode = statusCode;
        this.title = title;
        this.scrapedAt = scrapedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Instant getScrapedAt() { return scrapedAt; }
    public void setScrapedAt(Instant scrapedAt) { this.scrapedAt = scrapedAt; }
}