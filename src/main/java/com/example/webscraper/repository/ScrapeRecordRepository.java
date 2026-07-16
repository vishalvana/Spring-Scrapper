package com.example.webscraper.repository;

import com.example.webscraper.model.ScrapeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapeRecordRepository extends JpaRepository<ScrapeRecord, Long> {

    Page<ScrapeRecord> findAllByOrderByScrapedAtDesc(Pageable pageable);

    Page<ScrapeRecord> findByUrlContainingIgnoreCaseOrderByScrapedAtDesc(String urlFragment, Pageable pageable);
}