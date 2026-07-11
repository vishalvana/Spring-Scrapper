package com.example.webscraper.model;

import java.util.List;

public class ScrapeResult {

    private String url;
    private int statusCode;
    private String title;
    private String metaDescription;
    private String metaKeywords;
    private String language;

    private List<String> headings;      // all h1/h2/h3 text
    private List<LinkItem> links;       // hyperlinks found on the page
    private List<String> images;        // absolute image URLs
    private String textPreview;         // first N characters of visible body text
    private int wordCount;

    private List<String> customSelectorMatches; // results of the optional CSS selector, if provided

    public ScrapeResult() {
    }

    private ScrapeResult(Builder b) {
        this.url = b.url;
        this.statusCode = b.statusCode;
        this.title = b.title;
        this.metaDescription = b.metaDescription;
        this.metaKeywords = b.metaKeywords;
        this.language = b.language;
        this.headings = b.headings;
        this.links = b.links;
        this.images = b.images;
        this.textPreview = b.textPreview;
        this.wordCount = b.wordCount;
        this.customSelectorMatches = b.customSelectorMatches;
    }

    public static Builder builder() {
        return new Builder();
    }

    // --- getters / setters ---

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMetaDescription() { return metaDescription; }
    public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }

    public String getMetaKeywords() { return metaKeywords; }
    public void setMetaKeywords(String metaKeywords) { this.metaKeywords = metaKeywords; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<String> getHeadings() { return headings; }
    public void setHeadings(List<String> headings) { this.headings = headings; }

    public List<LinkItem> getLinks() { return links; }
    public void setLinks(List<LinkItem> links) { this.links = links; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getTextPreview() { return textPreview; }
    public void setTextPreview(String textPreview) { this.textPreview = textPreview; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public List<String> getCustomSelectorMatches() { return customSelectorMatches; }
    public void setCustomSelectorMatches(List<String> customSelectorMatches) { this.customSelectorMatches = customSelectorMatches; }

    public static class Builder {
        private String url;
        private int statusCode;
        private String title;
        private String metaDescription;
        private String metaKeywords;
        private String language;
        private List<String> headings;
        private List<LinkItem> links;
        private List<String> images;
        private String textPreview;
        private int wordCount;
        private List<String> customSelectorMatches;

        public Builder url(String url) { this.url = url; return this; }
        public Builder statusCode(int statusCode) { this.statusCode = statusCode; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder metaDescription(String metaDescription) { this.metaDescription = metaDescription; return this; }
        public Builder metaKeywords(String metaKeywords) { this.metaKeywords = metaKeywords; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder headings(List<String> headings) { this.headings = headings; return this; }
        public Builder links(List<LinkItem> links) { this.links = links; return this; }
        public Builder images(List<String> images) { this.images = images; return this; }
        public Builder textPreview(String textPreview) { this.textPreview = textPreview; return this; }
        public Builder wordCount(int wordCount) { this.wordCount = wordCount; return this; }
        public Builder customSelectorMatches(List<String> customSelectorMatches) { this.customSelectorMatches = customSelectorMatches; return this; }

        public ScrapeResult build() {
            return new ScrapeResult(this);
        }
    }

    public static class LinkItem {
        private String text;
        private String href;

        public LinkItem() {
        }

        private LinkItem(Builder b) {
            this.text = b.text;
            this.href = b.href;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }

        public static class Builder {
            private String text;
            private String href;

            public Builder text(String text) { this.text = text; return this; }
            public Builder href(String href) { this.href = href; return this; }

            public LinkItem build() {
                return new LinkItem(this);
            }
        }
    }
}
