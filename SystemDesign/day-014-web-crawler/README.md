# Day 014 — Web Crawler

> **Interview Goal:** Design a distributed web crawler that discovers and indexes billions of web pages.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

A web crawler (spider) is the foundation of a search engine. It starts with a set of seed URLs, downloads each page, extracts links, and follows them — crawling the web graph. Google's crawler processes billions of pages per day. The challenge is: the web is massive, pages change constantly, and you must crawl politely without overwhelming websites.

---

## Functional Requirements

1. Start from a list of **seed URLs** and recursively discover new URLs by following links.
2. **Download and store** the HTML content of every crawled page.
3. **Avoid duplicate crawling** — don't crawl the same URL twice per crawl cycle.
4. **Respect `robots.txt`** — don't crawl pages that websites disallow.
5. **Crawl frequency** — popular pages should be re-crawled more often than static/rare pages.
6. Support **multi-domain** crawling (the entire web, not just one site).
7. Extract **metadata** from pages: title, description, links, language, last modified date.
8. Detect and skip **duplicate content** — two different URLs serving the same content.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Crawl scale | 1 billion pages crawled per day |
| Politeness | No more than 1 request/second per domain |
| Freshness | Popular pages re-crawled within 24 hours; rare pages within 30 days |
| Fault tolerance | Continue crawling if some worker nodes fail |
| Storage | Store raw HTML for later indexing pipeline |
| Deduplication | Near-zero duplicate URL crawls |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Pages crawled/day | 1B |
| Pages crawled/second | ~12,000 |
| Avg page size (HTML) | 50 KB |
| Storage/day (raw HTML) | ~50 TB |
| Total URLs discovered | 100B+ (web is enormous) |
| Unique domains | ~1B domains |

---

## Core Architecture Components

```
Seed URLs
    ↓
URL Frontier (priority queue of URLs to crawl)
    ↓
Fetcher Workers (download page HTML)
    ↓
Parser (extract links + metadata)
    ↓
Deduplicator (filter already-crawled URLs)
    ↓
URL Scheduler (decide priority + timing)
    ↓
Content Store (raw HTML → S3)
    ↓
Index Pipeline (downstream: search indexing)
```

---

## Key Challenges to Think About

- **URL Frontier design:** You have 100B URLs to potentially crawl. You can't fit them all in memory. How do you design a priority queue that scales? What determines priority (page rank, freshness, domain authority)?
- **Deduplication at scale:** How do you know if a URL has already been crawled without scanning billions of records? (Hint: Bloom filter — why is it appropriate here despite false positives?)
- **Politeness:** You must not hammer a website with 1,000 concurrent requests. How do you enforce a per-domain rate limit across hundreds of worker machines?
- **robots.txt caching:** Every domain has a `robots.txt`. You can't fetch it before every page request. How do you cache it efficiently?
- **Duplicate content:** Two URLs serve the same content (e.g., `http://` vs `https://`, `?ref=fb` query params). How do you detect this without reading all content? (Hint: content hashing / SimHash)
- **Traps:** Some sites generate infinite URLs (e.g., calendars, infinite scroll). How do you detect and escape crawler traps?
- **Dynamic pages (JavaScript-rendered):** A SPA rendered entirely in JS has no HTML content when fetched. How do you handle this? (Hint: headless browser — but it's slow)
- **Distributed coordination:** 1,000 worker nodes all crawling simultaneously. How do you ensure no two workers fetch the same URL?

---

## Clarifying Questions (practice asking these in an interview)

1. Is this crawler for a general-purpose search engine, or a specific domain (e.g., e-commerce product catalog)?
2. Should it support JavaScript-rendered pages (SPAs), or HTML-only?
3. What is the freshness requirement — how quickly should changed pages be re-crawled?
4. Do we need to crawl behind authentication (logged-in pages)?
5. What is the output — raw HTML for a downstream indexer, or fully extracted structured data?
6. How do we handle websites that block crawlers (CAPTCHAs, IP bans)?

---

## Concepts Tested

`BFS / DFS graph traversal` · `Bloom filter (seen URL deduplication)` · `Priority queue (URL Frontier)` · `Consistent Hashing (worker assignment)` · `Rate limiting per domain` · `robots.txt` · `SimHash (duplicate content)` · `Object Storage (S3)` · `Message queue`
