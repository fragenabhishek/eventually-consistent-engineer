# Day 020 — E-Commerce Platform (Amazon-lite)

> **Interview Goal:** Design the core backend of an e-commerce platform covering catalog, cart, orders, and inventory.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Amazon processes 1.6M orders/day. Each order involves a product catalog lookup, inventory check, cart management, payment, order creation, and fulfilment dispatch — all with strict consistency guarantees. You're designing the core backend services that power this. This is an end-to-end LLD + HLD challenge that tests everything you've learned across the previous 19 days.

---

## Functional Requirements

1. **Product Catalog:** Browse, search, and filter products by category, brand, price, rating.
2. **Product Detail:** View product description, images, specs, seller info, and reviews.
3. **Cart:** Add/remove items, update quantities, see price totals with tax.
4. **Inventory:** Real-time stock levels; cannot sell more than what's in stock.
5. **Checkout:** Select address and payment method; see order summary.
6. **Payment:** Process payment; handle failures gracefully (retry, alternative method).
7. **Order Management:** Order placed → confirmed → processing → shipped → delivered.
8. **Returns & Refunds:** Return request within 30 days; refund to original payment method.
9. **Reviews & Ratings:** Verified buyers can rate and review products.
10. **Seller Portal:** Sellers manage their product listings and inventory.
11. **Recommendations:** "Customers who bought this also bought..." on product pages.
12. **Flash Sale / Deal of the Day:** Limited-time, high-discount offers with strict inventory caps.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Product page latency | < 100 ms (mostly cached) |
| Checkout + order latency | < 2 seconds |
| Inventory consistency | Strong — never oversell |
| Scale | 300M products; 300M users; 1.6M orders/day |
| Peak load | 10× normal during Prime Day / Big Billion Day |
| Search | Full-text + faceted search across 300M products |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Products in catalog | 300M |
| Users | 300M |
| Daily active users | 30M |
| Orders/day | 1.6M |
| Orders/second (avg) | ~18 |
| Orders/second (peak) | 500–1,000 (flash sales) |
| Product page views/day | 500M |
| Search queries/day | 200M |

---

## Microservices to Design

```
Client (Web / Mobile / Alexa)
    ↓
[API Gateway]
    ├── Catalog Service       → product listings, search, categories
    ├── Product Service       → product detail, images, specs, reviews
    ├── Inventory Service     → stock levels, reservations
    ├── Cart Service          → shopping cart (session-based + persistent)
    ├── Order Service         → order lifecycle management
    ├── Payment Service       → payment processing (see Day 016)
    ├── Notification Service  → confirmation emails, shipping updates (see Day 013)
    ├── User Service          → accounts, addresses, preferences
    ├── Seller Service        → seller portal, listing management
    ├── Search Service        → full-text + faceted product search
    └── Recommendation Service → ML-based product recommendations
```

---

## Key Challenges to Think About

- **Product Catalog at 300M products:** How do you store and serve 300M product records with their images, specs, and variants? A SQL table at this size has serious query challenges. How do you partition/shard?
- **Inventory reservation during checkout:** User adds item to cart → item might be reserved for others → user checks out → inventory must be decremented atomically. How do you handle this without overselling, especially during a flash sale?
- **Cart persistence:** A user adds items to cart on mobile, then checks out on desktop. How do you sync carts across devices? What happens when a guest cart is merged with a logged-in cart?
- **Flash sale architecture:** A flash sale starts at 12:00 PM with 5,000 units. 1M users try to buy simultaneously. This is the same problem as ticket booking (Day 015) — how do you apply those lessons here?
- **Search at 300M products:** Users search "red wireless headphones under $50". This needs full-text search, category filtering, price range filtering, and relevance ranking — all sub-100 ms. How does your search architecture handle this? (Hint: Elasticsearch, but think about indexing pipeline)
- **Order state machine:** An order has many states: PENDING → PAYMENT_PROCESSING → CONFIRMED → PICKED → SHIPPED → DELIVERED. Each transition may involve multiple services. How do you ensure consistency across services during transitions? (Hint: Saga pattern, Outbox pattern)
- **Image serving:** 300M products with 5–10 images each = 1.5–3 billion images. How do you store and serve these globally with low latency? (Hint: CDN + object storage)
- **Reviews and ratings:** Only verified buyers can review. But what prevents a seller from buying their own product and reviewing it? How do you detect fake reviews?

---

## Event Flow for an Order

```
1. User clicks "Buy Now"
2. Inventory Service: reserve N units (soft lock, 10-min TTL)
3. Cart Service: create checkout session
4. Payment Service: process payment
5. IF payment SUCCESS:
   → Inventory Service: confirm reservation (deduct from stock)
   → Order Service: create order record (CONFIRMED state)
   → Notification Service: send confirmation email
   → Fulfilment Service: queue for picking/packing
6. IF payment FAILS:
   → Inventory Service: release reservation
   → Show user payment failure screen
```

---

## Clarifying Questions (practice asking these in an interview)

1. Are we designing the full platform or a specific service (catalog, orders, search)?
2. Is it a marketplace (multiple sellers) or single-seller (Amazon Basics-style)?
3. Do we need to handle international shipping and customs?
4. Should the cart be session-based (lost on logout) or persistent (synced across devices)?
5. How do we handle partial fulfilment (item A ships today, item B ships in 3 days)?
6. Do we need to support subscription orders (monthly recurring deliveries)?

---

## Concepts Tested

`Microservices architecture` · `Saga / Outbox pattern` · `Inventory reservation with distributed locking` · `Event-driven order state machine` · `Full-text + faceted search (Elasticsearch)` · `CDN for product images` · `Flash sale (see Day 015)` · `Cart merge logic` · `CQRS (separate read/write for catalog)` · `Everything from Days 001–019`

---

> **Congratulations!** You've completed 20 days of System Design. You now have requirements + mental models for every major distributed system asked in interviews. Next step: go back to Day 001 and write the `design.md`, `trade-offs.md`, and `diagram.md` for each one.
