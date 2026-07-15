package com.example.webscraper.model;

public class BatchScrapeItemResult {

    private String url;
    private boolean success;
    private ScrapeResult result; // null if failed
    private String error;        // null if succeeded

    public BatchScrapeItemResult() {
    }

    private BatchScrapeItemResult(String url, boolean success, ScrapeResult result, String error) {
        this.url = url;
        this.success = success;
        this.result = result;
        this.error = error;
    }

    public static BatchScrapeItemResult success(String url, ScrapeResult result) {
        return new BatchScrapeItemResult(url, true, result, null);
    }

    public static BatchScrapeItemResult failure(String url, String error) {
        return new BatchScrapeItemResult(url, false, null, error);
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public ScrapeResult getResult() { return result; }
    public void setResult(ScrapeResult result) { this.result = result; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}