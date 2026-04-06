# Day 015 — Ticket Booking System (BookMyShow / Ticketmaster)

> **Interview Goal:** Design a high-concurrency ticket booking system where inventory is strictly limited and overselling is unacceptable.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

A new Marvel movie releases. 10,000 tickets go on sale at 10:00 AM. 500,000 people try to book simultaneously. You have exactly 10,000 seats. Not 10,001 — ever. The system must be fair, fast, and never sell the same seat twice. This is one of the hardest concurrency problems in backend engineering.

---

## Functional Requirements

1. Users can **browse** movies, shows, venues, and available showtimes.
2. Users can **view a seat map** showing available, selected, and booked seats.
3. Users can **select specific seats** and hold them for up to **5 minutes** while they complete payment.
4. Users can **complete payment** to confirm their booking.
5. On successful payment, tickets are **confirmed** and a confirmation email is sent.
6. If payment is not completed within 5 minutes, held seats are **released** back to the pool.
7. Users can **cancel** bookings up to 2 hours before the show.
8. Admins can **manage events**: add venues, configure seating, set pricing tiers.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Correctness | Zero overselling — absolutely no seat booked by more than one user |
| Availability | 99.99% |
| Seat selection latency | < 300 ms |
| Concurrency | Handle 500,000 simultaneous users during hot events |
| Consistency | Strong — seat availability must be accurate |
| Hold expiry | Exactly 5 minutes — not approximately |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Users | 50M registered |
| Peak concurrent users | 500,000 (during popular releases) |
| Transactions/second (peak) | 5,000 bookings/sec |
| Average cinema | 300 seats |
| Events/day | 100,000 showtimes across India |
| Bookings/day | 5M |

---

## Core API to Design

```
GET    /movies                            → list movies (with filters)
GET    /movies/{movieId}/showtimes        → list showtimes for a movie
GET    /showtimes/{showtimeId}/seats      → get real-time seat availability map
POST   /showtimes/{showtimeId}/hold       → temporarily hold selected seats (5-min TTL)
    body: { seatIds: ["A1", "A2"], userId: 123 }
    returns: { holdId: "...", expiresAt: "..." }
POST   /bookings                          → confirm booking (payment)
    body: { holdId: "...", paymentToken: "..." }
DELETE /bookings/{bookingId}              → cancel a booking
GET    /bookings/{userId}                 → get user's booking history
```

---

## Key Challenges to Think About

- **The core concurrency problem:** 100 users all click "Select Seat A1" at the same moment. Only one should succeed. Options:
  - Pessimistic locking (DB `SELECT FOR UPDATE`) — safe, but slow and doesn't scale.
  - Optimistic locking (version number) — fast, but causes many retries under contention.
  - Redis distributed lock (`SET NX EX`) — fast distributed lock per seat ID. What happens if Redis fails?
  - Ticket queue — users are queued and processed serially. Fair, but introduces latency.
- **5-minute hold expiry:** Seat A1 is held by User X for 5 minutes. User X abandons their cart. How do you reliably release that seat at exactly 5 minutes without a background scanner hitting millions of rows?
- **Seat map at scale:** During a hot event, 100,000 users are viewing the seat map simultaneously. Hitting the DB for real-time availability at this rate will cause a brownout. How do you cache the seat map while keeping it fresh enough?
- **Partial failure in payment flow:** User holds 2 seats, payment service deducts money, but the booking confirmation write fails (DB crash). How do you prevent both money being taken and seats not being booked?
- **Queue fairness:** If 500,000 users try to book 10,000 tickets, should it be first-come-first-served? How do you implement a virtual waiting room?
- **Cascading failures:** The payment gateway slows down under load. This causes booking workers to back up. How does that affect the hold expiry logic?

---

## Clarifying Questions (practice asking these in an interview)

1. Is this general-purpose (concerts, flights, bus) or movies only?
2. Can a user hold seats without logging in?
3. What happens if two users hold adjacent seats — can the system suggest better adjacent availability?
4. Is the payment processed by our system or a third-party gateway?
5. Do we need to support group bookings (e.g., book a whole row together or not at all)?
6. Are there VIP / priority queues for premium members?

---

## Concepts Tested

`Distributed locking (Redis SETNX)` · `Optimistic vs Pessimistic concurrency` · `Idempotent payments` · `TTL-based hold expiry` · `Virtual waiting room / queue` · `Read replicas for seat maps` · `Saga pattern for distributed transactions` · `Exactly-once semantics`
