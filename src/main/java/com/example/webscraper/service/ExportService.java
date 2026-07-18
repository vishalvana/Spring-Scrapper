package com.example.webscraper.service;

import com.example.webscraper.model.ScrapeHistoryItem;
import com.example.webscraper.model.ScrapeResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportService {

    private static final String[] LINKS_HEADER = { "text", "href" };
    private static final String[] IMAGES_HEADER = { "image_url" };
    private static final String[] HISTORY_HEADER = { "id", "url", "statusCode", "title", "scrapedAt" };

    public String linksToCsv(ScrapeResult result) {
        StringBuilder sb = new StringBuilder();
        writeRow(sb, LINKS_HEADER);
        if (result.getLinks() != null) {
            for (ScrapeResult.LinkItem link : result.getLinks()) {
                writeRow(sb, link.getText(), link.getHref());
            }
        }
        return sb.toString();
    }

    public String imagesToCsv(ScrapeResult result) {
        StringBuilder sb = new StringBuilder();
        writeRow(sb, IMAGES_HEADER);
        if (result.getImages() != null) {
            for (String image : result.getImages()) {
                writeRow(sb, image);
            }
        }
        return sb.toString();
    }

    public String historyToCsv(List<ScrapeHistoryItem> items) {
        StringBuilder sb = new StringBuilder();
        writeRow(sb, HISTORY_HEADER);
        for (ScrapeHistoryItem item : items) {
            writeRow(sb,
                    String.valueOf(item.getId()),
                    item.getUrl(),
                    String.valueOf(item.getStatusCode()),
                    item.getTitle(),
                    item.getScrapedAt() != null ? item.getScrapedAt().toString() : "");
        }
        return sb.toString();
    }

    private void writeRow(StringBuilder sb, String... fields) {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsv(fields[i]));
        }
        sb.append("\r\n");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }
}