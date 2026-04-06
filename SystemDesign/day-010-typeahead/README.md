# Day 010 — Search Typeahead / Autocomplete (Google Search Bar)

> **Interview Goal:** Design a system that suggests search completions as the user types, in real-time.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Every time you type in the Google search bar, within milliseconds you see up to 10 suggestions completing your query. This is typeahead (autocomplete). It feels instant because it must respond in under 100 ms — before the user has even finished their thought. At Google's scale, this system handles billions of queries per day and must rank suggestions by relevance and freshness.

---

## Functional Requirements

1. As the user types each character, the system returns **up to 10 suggestions** that complete the typed prefix.
2. Suggestions are **ranked** by popularity (most searched completions globally + personalised for the user).
3. Suggestions update with **each keystroke** (latency must feel instant).
4. The suggestion corpus is **updated regularly** from real search queries (not real-time, but within ~24 hours).
5. Support for **multiple languages**.
6. Filter out **banned/offensive queries** from suggestions.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Latency | < 100 ms end-to-end (p99) |
| Availability | 99.99% |
| Freshness | Suggestions updated within 24 hours of trending queries |
| Scale | 5B searches/day on Google; ~50B typeahead requests/day (10 per search) |
| Data size | Top 1 billion search queries need to be indexed |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Typeahead requests/day | 50B |
| Typeahead requests/second | ~600,000 |
| Avg query string size | 30 bytes |
| Top 1B queries index size | ~30 GB raw, fits in memory |
| Read:Write ratio | ~10,000:1 (mostly reads, updates are batch) |

---

## Core API to Design

```
GET /autocomplete?q={prefix}&limit=10&lang={lang}
    → returns: ["prefix completion 1", "prefix completion 2", ...]

Internal:
POST /admin/suggestions/rebuild  → trigger batch rebuild of suggestion index
POST /admin/suggestions/ban      → add a query to blocklist
```

---

## Key Challenges to Think About

- **Data structure:** A naive approach returns all strings starting with the prefix. At 1 billion strings, this is far too slow. What data structure gives you O(prefix_length) lookup? (Hint: Trie / Prefix Tree)
- **Trie in a distributed system:** A trie for 1 billion queries won't fit on one server. How do you shard it? By prefix? By first letter?
- **Ranking within prefix:** Multiple queries share the same prefix. How do you rank them? What data do you store at each trie node?
- **Update frequency vs consistency:** Search trends change (e.g., a celebrity news event). How often do you rebuild the trie? Real-time or batch?
- **Storage vs memory:** Serving from memory is fast but expensive. Can you compress the trie? What do you do when the trie outgrows RAM?
- **Personalisation:** "piz" should suggest "pizza" for most users but "piracy laws" for a law student. How do you personalise without making every user's trie unique?
- **Offensive content filtering:** Typeahead should never suggest harmful queries. Where in the pipeline do you apply filters efficiently?
- **Mobile keyboards:** On mobile, each keystroke is a network request. How do you reduce round trips? (Hint: client-side trie cache)

---

## Clarifying Questions (practice asking these in an interview)

1. Do we need to support personalised suggestions per user, or just global popularity?
2. Should suggestions update in real-time as new queries arrive, or in batches?
3. Do we need to support spell correction (e.g., "fooball" → "football")?
4. What languages and scripts must be supported?
5. Is there a blocklist of queries that should never be suggested?
6. Should suggestions include query completion only, or also related entities (people, places)?

---

## Concepts Tested

`Trie / Prefix Tree` · `Distributed trie sharding` · `Top-K queries (min-heap)` · `LRU cache for hot prefixes` · `Batch pipeline for suggestion updates` · `CDN for edge-caching suggestions` · `Bloom filter (blocklist)` · `Client-side caching`
