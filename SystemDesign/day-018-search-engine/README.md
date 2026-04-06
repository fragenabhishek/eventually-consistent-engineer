# Day 018 — Search Engine (Google-lite)

> **Interview Goal:** Design a web-scale search engine covering crawling, indexing, ranking, and serving.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

You're building a simplified version of a web search engine. Unlike autocomplete (Day 010), a full search engine must crawl the web (or a corpus), parse and understand content, build an inverted index, rank results by relevance, and serve search results for arbitrary queries in under 200 ms — even with a corpus of billions of documents.

---

## Functional Requirements

1. **Crawl** the web and index billions of pages (see Day 014 for crawling details).
2. Users enter a **text query** and receive a ranked list of relevant web pages.
3. Results include **title, URL, and a snippet** (relevant excerpt from the page).
4. Support **phrase search**: `"machine learning"` returns pages with that exact phrase.
5. Support **boolean operators**: `java AND spring NOT framework`.
6. Results ranked by **relevance** — more relevant and authoritative pages appear first.
7. **Fresh index** — recently published or updated pages appear in search within hours.
8. Search results load in **under 200 ms** (p99).
9. Support **spell correction**: query `"machien leraning"` → "Did you mean: machine learning?"

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Corpus size | 100 billion indexed documents |
| Query latency | < 200 ms (p99) |
| Indexing freshness | New pages indexed within 24 hours; updates within hours |
| Availability | 99.99% |
| Throughput | 100,000 queries/second (Google handles ~100K QPS) |
| Index size | ~1 PB (inverted index across all docs) |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Indexed pages | 100B |
| Avg page word count | 500 words |
| Unique terms in index | ~5M |
| Avg postings per term | 100M (very common words) → 10 (rare words) |
| Index storage | ~1 PB |
| Queries/day | 8.5B (Google: ~100K QPS) |
| Queries/second | ~100,000 |

---

## Core Components to Design

```
Web Pages (from Crawler → Day 014)
    ↓
Document Parser
    → Extract: title, body text, links, metadata
    → Language detection, text normalisation, tokenisation
    ↓
Indexer
    → Build inverted index: term → [docId, position, TF score]
    → Store forward index: docId → [title, URL, snippet text]
    → Compute PageRank (link graph analysis)
    ↓
Index Storage (distributed, sharded by term)
    ↓
Query Processor
    → Parse query (tokenise, normalise, expand synonyms)
    → Look up postings for each term
    → Intersect / Union postings lists
    → Score documents (TF-IDF × PageRank × freshness × UX signals)
    → Return Top-K results
    ↓
Snippet Generator
    → Find most relevant sentence for each result
    ↓
Search Results (ranked list with snippets)
```

---

## Key Challenges to Think About

- **Inverted index at scale:** An inverted index maps every word to a list of documents containing it. For the word "the" this list is billions of documents long. How do you store, shard, and query this efficiently?
- **Index sharding:** The index is 1 PB — far more than one machine. Do you shard by term (horizontal — all postings for a term on one machine) or by document (vertical — each machine has all terms for a subset of docs)? What are the trade-offs?
- **Ranking — TF-IDF vs PageRank:** TF-IDF measures how important a word is in a document. PageRank measures how important a page is based on who links to it. How do you combine them into a single relevance score?
- **Query latency < 200 ms:** A query for "java tutorial" matches billions of documents. You can't score all of them. How do you prune the candidate set without hurting quality? (Hint: DAAT vs TAAT processing, early termination)
- **Index freshness:** A breaking news article should appear in results within an hour. But re-indexing 100B pages takes weeks. How do you maintain a real-time incremental index alongside the main index?
- **Spell correction:** To suggest "machine learning" when the user types "machien leraning", you need to know that "machien" is a common misspelling. How do you build this at scale? (Hint: edit distance + query log analysis)
- **Personalisation vs privacy:** Search results can be personalised based on user history. But storing query history raises privacy concerns. How do you balance these?

---

## Clarifying Questions (practice asking these in an interview)

1. What is the corpus — general web, or a specific domain (e.g., code search, e-commerce)?
2. Does ranking require real-time user signals (click-through rates), or is batch-computed ranking fine?
3. Do we need to support image/video search, or text-only?
4. How fresh must the index be — hours, or days acceptable?
5. Do we need personalised results, or the same results for everyone?
6. Should we support search operators (`site:`, `filetype:`, `inurl:`)? 

---

## Concepts Tested

`Inverted index` · `TF-IDF scoring` · `PageRank algorithm` · `Index sharding (term vs doc partitioning)` · `MapReduce for batch indexing` · `Real-time index updates` · `Top-K query processing` · `Bloom filter` · `Edit distance (spell correction)`
