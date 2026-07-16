package com.example.webscraper.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "scrape_records")
public class ScrapeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    private int statusCode;

    @Column(length = 512)
    private String title;

    @Column(nullable = false)
    private Instant scrapedAt;

    /** Full ScrapeResult serialized as JSON, so we don't need a complex relational schema for it. */
    @Lob
    @Column(name = "result_json")
    private String resultJson;

    public ScrapeRecord() {
    }

    public ScrapeRecord(String url, int statusCode, String title, Instant scrapedAt, String resultJson) {
        this.url = url;
        this.statusCode = statusCode;
        this.title = title;
        this.scrapedAt = scrapedAt;
        this.resultJson = resultJson;
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

    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
}