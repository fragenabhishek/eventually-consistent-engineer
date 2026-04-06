# Day 009 — Ride-Sharing & Location Tracking (Uber)

> **Interview Goal:** Design a ride-hailing platform with real-time driver tracking, matching, and pricing.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Uber connects riders and drivers in real-time. A rider opens the app, sees available drivers nearby, requests a ride, gets matched to the best driver, tracks the driver live on a map, and pays automatically. The core engineering challenge is geospatial proximity at scale — with millions of drivers moving continuously, how do you find "the nearest available driver" in milliseconds?

---

## Functional Requirements

1. **Rider:** Enter pickup and drop-off location; see fare estimate.
2. **Rider:** Request a ride; get matched to a nearby available driver within 30 seconds.
3. **Rider:** Track driver's real-time location on a map while waiting and during the trip.
4. **Driver:** Go online/offline; accept or reject ride requests.
5. **Driver:** Navigate to pickup, then to destination.
6. **Trip completion:** Automatic payment from rider's saved card to driver.
7. **Ratings:** Rider and driver rate each other after the trip.
8. **Ride history:** Both riders and drivers can see past trips.
9. **Surge pricing:** Fare increases dynamically in high-demand areas.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Driver matching latency | < 1 second to find candidates; match confirmed < 5 seconds |
| Location update frequency | Drivers send GPS updates every 3–5 seconds |
| Consistency | Strong for payment; Eventual for driver location |
| Scale | 5M drivers online at peak; 20M rides/day |
| Geospatial coverage | Global, 70+ countries |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Active drivers (peak) | 5M |
| Location updates/second | 5M drivers × 1 update/4s = ~1.25M updates/sec |
| Ride requests/second | 20M rides/day ÷ 86,400s = ~230/sec (avg), peak ~2,000/sec |
| Active trips at once (peak) | ~500K |
| Trip records/day | 20M |

---

## Core API to Design

```
POST   /rides/estimate          → get fare estimate (pickup + dropoff → price, ETA)
POST   /rides/request           → request a ride
GET    /rides/{rideId}          → get ride status and driver location
DELETE /rides/{rideId}          → cancel a ride
POST   /drivers/location        → driver sends GPS update (every 4 seconds)
PUT    /drivers/status          → driver goes online/offline
POST   /rides/{rideId}/accept   → driver accepts a ride request
POST   /rides/{rideId}/complete → driver marks trip as complete
POST   /payments/{rideId}       → trigger payment
POST   /ratings/{rideId}        → submit rating
```

---

## Key Challenges to Think About

- **Geospatial indexing:** How do you find all drivers within a 2 km radius of a rider, when 5M drivers are moving continuously? (Hint: Geohashing or S2 cells)
- **1.25M location updates/second:** You can't write every update to a primary database. Where do you store ephemeral driver locations? What happens if that store fails?
- **Matching algorithm:** Multiple drivers are nearby. Which do you pick? How do you avoid sending a request to a driver who just accepted another trip?
- **Distributed locking for driver assignment:** Two riders both request at the same moment and the algorithm picks the same driver for both. How do you prevent double-assignment?
- **Surge pricing computation:** Surge is based on supply (available drivers) vs demand (ride requests) in a geographic cell. How do you compute this every minute across the entire city?
- **Real-time location streaming to rider:** The rider's app must show the driver's location moving on the map. How do you push 5-second updates without polling?
- **Payment reliability:** The trip ends, the driver drives away, but payment fails. How do you guarantee the driver gets paid even if there's a temporary system outage?
- **ETA calculation:** How do you compute a realistic ETA accounting for live traffic?

---

## Clarifying Questions (practice asking these in an interview)

1. Are we designing the full system or focusing on a specific component (matching, location, payments)?
2. Do we need to support scheduled rides (book now, ride in 2 hours)?
3. Should we support ride pooling (sharing a car with strangers)?
4. What is the acceptable time for the matching algorithm to complete?
5. Do we need to support cash payments in addition to card?
6. How do we handle GPS inaccuracy in dense urban areas?

---

## Concepts Tested

`Geohashing / S2 cells` · `Redis for ephemeral location store` · `WebSocket for real-time tracking` · `Distributed locking` · `Message queue for matching` · `Surge pricing (supply/demand calculation)` · `Idempotent payments` · `Consistent Hashing`
