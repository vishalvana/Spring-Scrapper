package com.example.webscraper.repository;

import com.example.webscraper.model.ScheduledTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledTargetRepository extends JpaRepository<ScheduledTarget, Long> {

    List<ScheduledTarget> findByActiveTrue();
}