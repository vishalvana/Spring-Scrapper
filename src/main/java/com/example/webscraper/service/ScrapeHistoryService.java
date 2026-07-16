package com.example.webscraper.service;

import com.example.webscraper.exception.ScrapingException;
import com.example.webscraper.model.ScrapeHistoryItem;
import com.example.webscraper.model.ScrapeRecord;
import com.example.webscraper.model.ScrapeResult;
import com.example.webscraper.repository.ScrapeRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScrapeHistoryService {

    private final ScrapeRecordRepository repository;
    private final ObjectMapper objectMapper;

    public ScrapeHistoryService(ScrapeRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /** Persists a scrape result as history. Never throws — a save failure shouldn't break the scrape response. */
    public void save(ScrapeResult result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            ScrapeRecord record = new ScrapeRecord(
                    result.getUrl(), result.getStatusCode(), result.getTitle(), Instant.now(), json);
            repository.save(record);
        } catch (Exception e) {
            System.err.println("Failed to save scrape history for " + result.getUrl() + ": " + e.getMessage());
        }
    }

    public List<ScrapeHistoryItem> listRecent(int limit, String urlFilter) {
        Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 200)), Sort.by(Sort.Direction.DESC, "scrapedAt"));

        Page<ScrapeRecord> page = (urlFilter != null && !urlFilter.isBlank())
                ? repository.findByUrlContainingIgnoreCaseOrderByScrapedAtDesc(urlFilter.trim(), pageable)
                : repository.findAllByOrderByScrapedAtDesc(pageable);

        return page.getContent().stream()
                .map(r -> new ScrapeHistoryItem(r.getId(), r.getUrl(), r.getStatusCode(), r.getTitle(), r.getScrapedAt()))
                .collect(Collectors.toList());
    }

    public ScrapeResult getFullResultById(Long id) {
        ScrapeRecord record = repository.findById(id)
                .orElseThrow(() -> new ScrapingException("No scrape history found with id " + id));
        try {
            return objectMapper.readValue(record.getResultJson(), ScrapeResult.class);
        } catch (Exception e) {
            throw new ScrapingException("Could not deserialize stored scrape result for id " + id, e);
        }
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}