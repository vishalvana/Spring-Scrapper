package com.example.webscraper.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BatchScrapeRequest {

    @NotEmpty(message = "urls must not be empty")
    @Size(max = 20, message = "a maximum of 20 URLs can be scraped per batch request")
    private List<String> urls;

    private String selector;
    private Integer linkLimit;
    private Integer imageLimit;

    /** Delay in milliseconds between each request in the batch. Defaults to 1000ms if not supplied. */
    private Integer delayMs;

    public BatchScrapeRequest() {
    }

    public List<String> getUrls() { return urls; }
    public void setUrls(List<String> urls) { this.urls = urls; }

    public String getSelector() { return selector; }
    public void setSelector(String selector) { this.selector = selector; }

    public Integer getLinkLimit() { return linkLimit; }
    public void setLinkLimit(Integer linkLimit) { this.linkLimit = linkLimit; }

    public Integer getImageLimit() { return imageLimit; }
    public void setImageLimit(Integer imageLimit) { this.imageLimit = imageLimit; }

    public Integer getDelayMs() { return delayMs; }
    public void setDelayMs(Integer delayMs) { this.delayMs = delayMs; }
}