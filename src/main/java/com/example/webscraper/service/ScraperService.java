package com.example.webscraper.service;

import com.example.webscraper.exception.ScrapingException;
import com.example.webscraper.model.ScrapeRequest;
import com.example.webscraper.model.ScrapeResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.webscraper.exception.RobotsDisallowedException;
import com.example.webscraper.robots.RobotsTxtService;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScraperService {
    private final RobotsTxtService robotsTxtService;

    public ScraperService(RobotsTxtService robotsTxtService) {
        this.robotsTxtService = robotsTxtService;
    }

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/125.0 Safari/537.36 SpringBootWebScraper/1.0";

    private static final int TIMEOUT_MS = 10_000;
    private static final int DEFAULT_ITEM_LIMIT = 50;
    private static final int TEXT_PREVIEW_LENGTH = 500;

    /**
     * Fetches the given URL and extracts common page data: title, meta tags,
     * headings, links, images, and a text preview. Optionally also runs a
     * user-supplied CSS selector and returns the matched elements' text.
     */
    public ScrapeResult scrape(ScrapeRequest request) {
        String url = request.getUrl().trim();
        int linkLimit = request.getLinkLimit() != null ? request.getLinkLimit() : DEFAULT_ITEM_LIMIT;
        int imageLimit = request.getImageLimit() != null ? request.getImageLimit() : DEFAULT_ITEM_LIMIT;
        if (!robotsTxtService.isAllowed(url)) {
            throw new RobotsDisallowedException(
                    "Scraping " + url + " is disallowed by the site's robots.txt");
        }
        try {
            Connection.Response response = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)
                    .maxBodySize(5 * 1024 * 1024) // 5 MB cap
                    .execute();

            Document doc = response.parse();

            ScrapeResult.Builder builder = ScrapeResult.builder()
                    .url(url)
                    .statusCode(response.statusCode())
                    .title(doc.title())
                    .metaDescription(metaContent(doc, "description"))
                    .metaKeywords(metaContent(doc, "keywords"))
                    .language(doc.select("html").attr("lang"))
                    .headings(extractHeadings(doc))
                    .links(extractLinks(doc, linkLimit))
                    .images(extractImages(doc, imageLimit))
                    .textPreview(extractTextPreview(doc))
                    .wordCount(countWords(doc));

            if (request.getSelector() != null && !request.getSelector().isBlank()) {
                builder.customSelectorMatches(runCustomSelector(doc, request.getSelector()));
            }

            return builder.build();

        } catch (SocketTimeoutException e) {
            throw new ScrapingException("Request timed out while fetching: " + url, e);
        } catch (IOException e) {
            throw new ScrapingException("Failed to fetch URL: " + url + " (" + e.getMessage() + ")", e);
        } catch (IllegalArgumentException e) {
            throw new ScrapingException("Invalid URL: " + url, e);
        }
    }

    private String metaContent(Document doc, String name) {
        Element tag = doc.selectFirst("meta[name=" + name + "]");
        if (tag == null) {
            tag = doc.selectFirst("meta[property=og:" + name + "]");
        }
        return tag != null ? tag.attr("content") : "";
    }

    private List<String> extractHeadings(Document doc) {
        Elements headings = doc.select("h1, h2, h3");
        return headings.stream()
                .map(Element::text)
                .filter(text -> !text.isBlank())
                .collect(Collectors.toList());
    }

    private List<ScrapeResult.LinkItem> extractLinks(Document doc, int limit) {
        Elements anchors = doc.select("a[href]");
        Set<String> seenHrefs = new LinkedHashSet<>();
        return anchors.stream()
                .map(a -> ScrapeResult.LinkItem.builder()
                        .text(a.text())
                        .href(a.absUrl("href"))
                        .build())
                .filter(link -> !link.getHref().isBlank())
                .filter(link -> seenHrefs.add(link.getHref())) // dedupe by href
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<String> extractImages(Document doc, int limit) {
        Elements imgs = doc.select("img[src]");
        Set<String> seen = new LinkedHashSet<>();
        for (Element img : imgs) {
            String src = img.absUrl("src");
            if (!src.isBlank()) {
                seen.add(src);
            }
            if (seen.size() >= limit) {
                break;
            }
        }
        return seen.stream().collect(Collectors.toList());
    }

    private String extractTextPreview(Document doc) {
        String bodyText = doc.body() != null ? doc.body().text() : "";
        if (bodyText.length() <= TEXT_PREVIEW_LENGTH) {
            return bodyText;
        }
        return bodyText.substring(0, TEXT_PREVIEW_LENGTH) + "...";
    }

    private int countWords(Document doc) {
        String bodyText = doc.body() != null ? doc.body().text() : "";
        if (bodyText.isBlank()) {
            return 0;
        }
        return bodyText.trim().split("\\s+").length;
    }

    private List<String> runCustomSelector(Document doc, String selector) {
        try {
            Elements matches = doc.select(selector);
            return matches.stream()
                    .map(Element::text)
                    .filter(text -> !text.isBlank())
                    .limit(100)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ScrapingException("Invalid CSS selector: " + selector, e);
        }
    }
}
