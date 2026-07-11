# Spring Boot Web Scraper

A simple, self-contained web scraping tool built with Spring Boot and [Jsoup](https://jsoup.org/).
Give it a URL and it extracts the page title, meta description/keywords, headings, links,
images, a text preview, word count, and (optionally) the results of a custom CSS selector.

Includes a small built-in web UI (`/`) and a JSON REST API (`/api/scrape`).

## Requirements

- Java 17+
- Maven 3.8+ (or use the included wrapper if you add one)

## Run it

```bash
cd webscraper
mvn spring-boot:run
```

Then open **http://localhost:8080** in your browser for the UI, or call the API directly.

## Build a jar

```bash
mvn clean package
java -jar target/webscraper.jar
```

## API

### POST /api/scrape

Request body:

```json
{
  "url": "https://example.com",
  "selector": "h2.title",
  "linkLimit": 30,
  "imageLimit": 20
}
```

- `url` (required) — must start with `http://` or `https://`
- `selector` (optional) — any CSS selector (Jsoup syntax, same as jQuery-style selectors);
  matched elements' text is returned in `customSelectorMatches`
- `linkLimit` (optional, default 50) — max number of links returned
- `imageLimit` (optional, default 50) — max number of images returned

Example:

```bash
curl -X POST http://localhost:8080/api/scrape \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com"}'
```

Response:

```json
{
  "url": "https://example.com",
  "statusCode": 200,
  "title": "Example Domain",
  "metaDescription": "",
  "metaKeywords": "",
  "language": "",
  "headings": ["Example Domain"],
  "links": [
    { "text": "More information...", "href": "https://www.iana.org/domains/example" }
  ],
  "images": [],
  "textPreview": "Example Domain This domain is for use in illustrative examples...",
  "wordCount": 28,
  "customSelectorMatches": null
}
```

### GET /api/scrape

Same behavior, for quick testing from a browser or curl:

```
GET /api/scrape?url=https://example.com&selector=h1&linkLimit=10&imageLimit=10
```

### GET /api/health

Returns `OK` — useful for uptime checks.

## Notes & extension ideas

- The scraper sends a realistic browser `User-Agent` and follows redirects, with a 10s timeout
  and a 5 MB response size cap (adjust in `ScraperService`).
- Respect target sites' `robots.txt` and terms of service; this tool does not check `robots.txt`
  automatically. Add that check in `ScraperService.scrape()` if you need it enforced.
- To scrape JavaScript-rendered pages (SPAs), Jsoup alone won't execute JS — you'd need to pair
  this with a headless browser (e.g. Playwright or Selenium) for those sites.
- To persist results, add a Spring Data repository + entity and save `ScrapeResult` after each call.
- To scrape multiple pages, add a `/api/scrape-batch` endpoint that loops over a list of URLs
  (with delays between requests to be a good citizen).
