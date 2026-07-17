package com.example.webscraper.service;

import com.example.webscraper.exception.ScrapingException;
import com.example.webscraper.model.ScheduledTarget;
import com.example.webscraper.model.ScheduledTargetRequest;
import com.example.webscraper.model.ScrapeRequest;
import com.example.webscraper.model.ScrapeResult;
import com.example.webscraper.repository.ScheduledTargetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ScheduledScrapingService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledScrapingService.class);

    private final ScheduledTargetRepository repository;
    private final ScraperService scraperService;
    private final ScrapeHistoryService scrapeHistoryService;

    @Value("${scraper.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public ScheduledScrapingService(ScheduledTargetRepository repository,
                                    ScraperService scraperService,
                                    ScrapeHistoryService scrapeHistoryService) {
        this.repository = repository;
        this.scraperService = scraperService;
        this.scrapeHistoryService = scrapeHistoryService;
    }

    @Scheduled(fixedRateString = "${scraper.scheduler.poll-rate-ms:60000}")
    public void runDueScrapes() {
        if (!schedulerEnabled) {
            return;
        }

        Instant now = Instant.now();
        List<ScheduledTarget> dueTargets = repository.findByActiveTrue().stream()
                .filter(t -> t.isDue(now))
                .toList();

        if (dueTargets.isEmpty()) {
            return;
        }

        log.info("Running {} due scheduled scrape(s)", dueTargets.size());

        for (ScheduledTarget target : dueTargets) {
            try {
                ScrapeRequest request = new ScrapeRequest(target.getUrl(), null, null, null);
                ScrapeResult result = scraperService.scrape(request);
                scrapeHistoryService.save(result);
                target.setLastRunAt(Instant.now());
                repository.save(target);
                log.info("Scheduled scrape succeeded for {}", target.getUrl());
            } catch (Exception e) {
                target.setLastRunAt(Instant.now());
                repository.save(target);
                log.warn("Scheduled scrape failed for {}: {}", target.getUrl(), e.getMessage());
            }
        }
    }

    public ScheduledTarget create(ScheduledTargetRequest request) {
        ScheduledTarget target = new ScheduledTarget(request.getUrl().trim(), request.getIntervalMinutes());
        return repository.save(target);
    }

    public List<ScheduledTarget> listAll() {
        return repository.findAll();
    }

    public ScheduledTarget setActive(Long id, boolean active) {
        ScheduledTarget target = repository.findById(id)
                .orElseThrow(() -> new ScrapingException("No scheduled target found with id " + id));
        target.setActive(active);
        return repository.save(target);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}