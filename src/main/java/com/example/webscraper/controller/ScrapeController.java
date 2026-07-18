package com.example.webscraper.controller;

import com.example.webscraper.model.ScrapeRequest;
import com.example.webscraper.model.ScrapeResult;
import com.example.webscraper.service.ScraperService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.webscraper.model.BatchScrapeItemResult;
import com.example.webscraper.model.BatchScrapeRequest;
import java.util.List;
import com.example.webscraper.model.ScrapeHistoryItem;
import com.example.webscraper.service.ScrapeHistoryService;
import com.example.webscraper.service.ExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // relax for demo purposes; restrict in production
public class ScrapeController {

    private final ScraperService scraperService;
    private final ScrapeHistoryService scrapeHistoryService;

    private final ExportService exportService;
    private final ObjectMapper objectMapper;

    public ScrapeController(ScraperService scraperService,
                            ScrapeHistoryService scrapeHistoryService,
                            ExportService exportService,
                            ObjectMapper objectMapper) {
        this.scraperService = scraperService;
        this.scrapeHistoryService = scrapeHistoryService;
        this.exportService = exportService;
        this.objectMapper = objectMapper;
    }

//    public ScrapeController(ScraperService scraperService, ScrapeHistoryService scrapeHistoryService) {
//        this.scraperService = scraperService;
//        this.scrapeHistoryService = scrapeHistoryService;
//    }

    /**
     * POST /api/scrape
     * Body: { "url": "https://example.com", "selector": "h2.title", "linkLimit": 20, "imageLimit": 20 }
     */
    @PostMapping("/scrape")
    public ResponseEntity<ScrapeResult> scrape(@Valid @RequestBody ScrapeRequest request) {
        ScrapeResult result = scraperService.scrape(request);
        scrapeHistoryService.save(result);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/scrape?url=https://example.com&selector=h2&linkLimit=20&imageLimit=20
     * Convenience GET version for quick testing (e.g. from a browser or curl).
     */
    @GetMapping("/scrape")
    public ResponseEntity<ScrapeResult> scrapeGet(
            @RequestParam String url,
            @RequestParam(required = false) String selector,
            @RequestParam(required = false) Integer linkLimit,
            @RequestParam(required = false) Integer imageLimit) {

        ScrapeRequest request = new ScrapeRequest(url, selector, linkLimit, imageLimit);
        ScrapeResult result = scraperService.scrape(request);
        scrapeHistoryService.save(result);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/scrape-batch")
    public ResponseEntity<List<BatchScrapeItemResult>> scrapeBatch(@Valid @RequestBody BatchScrapeRequest request) {
        List<BatchScrapeItemResult> results = scraperService.scrapeBatch(request);
        results.stream()
                .filter(BatchScrapeItemResult::isSuccess)
                .forEach(item -> scrapeHistoryService.save(item.getResult()));
        return ResponseEntity.ok(results);
    }

    @GetMapping("/scrapes")
    public ResponseEntity<List<ScrapeHistoryItem>> listHistory(
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false) String url) {
        return ResponseEntity.ok(scrapeHistoryService.listRecent(limit, url));
    }

    @GetMapping("/scrapes/{id}")
    public ResponseEntity<ScrapeResult> getHistoryItem(@PathVariable Long id) {
        return ResponseEntity.ok(scrapeHistoryService.getFullResultById(id));
    }

    @DeleteMapping("/scrapes/{id}")
    public ResponseEntity<Void> deleteHistoryItem(@PathVariable Long id) {
        scrapeHistoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/scrapes/{id}/export")
    public ResponseEntity<byte[]> exportScrape(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "json") String format) throws Exception {

        ScrapeResult result = scrapeHistoryService.getFullResultById(id);

        return switch (format.toLowerCase()) {
            case "links-csv" -> fileResponse(
                    exportService.linksToCsv(result), "scrape-" + id + "-links.csv", "text/csv");
            case "images-csv" -> fileResponse(
                    exportService.imagesToCsv(result), "scrape-" + id + "-images.csv", "text/csv");
            default -> {
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
                yield fileResponse(json, "scrape-" + id + ".json", "application/json");
            }
        };
    }

    @GetMapping("/scrapes/export")
    public ResponseEntity<byte[]> exportHistory(
            @RequestParam(required = false, defaultValue = "100") int limit,
            @RequestParam(required = false) String url) {

        List<ScrapeHistoryItem> items = scrapeHistoryService.listRecent(limit, url);
        return fileResponse(exportService.historyToCsv(items), "scrape-history.csv", "text/csv");
    }

    private ResponseEntity<byte[]> fileResponse(String content, String filename, String contentType) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType + "; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
