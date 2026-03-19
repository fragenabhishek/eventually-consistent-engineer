# Day 001 — URL Shortener

**Date:** 2026-03-19  
**Category:** Web / Storage  
**Difficulty:** Medium

---

## Problem Statement

Design a URL shortening service like bit.ly or TinyURL.

## Functional Requirements

- User provides a long URL → service returns a short URL (e.g. `short.ly/aB3kX`)
- Visiting the short URL redirects to the original long URL
- Custom aliases (optional): user can choose a custom slug
- Link expiration (optional): short links can have a TTL

## Non-Functional Requirements

- High availability (redirects must never go down)
- Low latency reads (< 10 ms P99 for redirect)
- Eventual consistency is acceptable for analytics
- Scale: 100 M URLs created per day, 10 B redirects per day (read-heavy, ~100:1 read/write ratio)

---

## Capacity Estimation

| Metric | Value |
|--------|-------|
| Writes / sec | ~1,150 |
| Reads / sec | ~115,000 |
| Storage / year (500 bytes avg) | ~17 TB |
| Cache size (20% hot URLs) | ~170 GB |
