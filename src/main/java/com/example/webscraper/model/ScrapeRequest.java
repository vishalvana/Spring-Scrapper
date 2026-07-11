package com.example.webscraper.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ScrapeRequest {

    @NotBlank(message = "url must not be blank")
    @Pattern(regexp = "^https?://.+", message = "url must start with http:// or https://")
    private String url;

    /** CSS selector to run against the page in addition to the standard extraction. Optional. */
    private String selector;

    /** Max number of links to return. Defaults to 50 if not supplied. */
    private Integer linkLimit;

    /** Max number of images to return. Defaults to 50 if not supplied. */
    private Integer imageLimit;

    public ScrapeRequest() {
    }

    public ScrapeRequest(String url, String selector, Integer linkLimit, Integer imageLimit) {
        this.url = url;
        this.selector = selector;
        this.linkLimit = linkLimit;
        this.imageLimit = imageLimit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Integer getLinkLimit() {
        return linkLimit;
    }

    public void setLinkLimit(Integer linkLimit) {
        this.linkLimit = linkLimit;
    }

    public Integer getImageLimit() {
        return imageLimit;
    }

    public void setImageLimit(Integer imageLimit) {
        this.imageLimit = imageLimit;
    }
}
