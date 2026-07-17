package com.example.webscraper.controller;

import com.example.webscraper.model.ScheduledTarget;
import com.example.webscraper.model.ScheduledTargetRequest;
import com.example.webscraper.service.ScheduledScrapingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-targets")
@CrossOrigin(origins = "*")
public class ScheduledTargetController {

    private final ScheduledScrapingService scheduledScrapingService;

    public ScheduledTargetController(ScheduledScrapingService scheduledScrapingService) {
        this.scheduledScrapingService = scheduledScrapingService;
    }

    @PostMapping
    public ResponseEntity<ScheduledTarget> create(@Valid @RequestBody ScheduledTargetRequest request) {
        return ResponseEntity.ok(scheduledScrapingService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ScheduledTarget>> list() {
        return ResponseEntity.ok(scheduledScrapingService.listAll());
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ScheduledTarget> activate(@PathVariable Long id) {
        return ResponseEntity.ok(scheduledScrapingService.setActive(id, true));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ScheduledTarget> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(scheduledScrapingService.setActive(id, false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduledScrapingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}