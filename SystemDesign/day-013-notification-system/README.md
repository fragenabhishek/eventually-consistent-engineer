# Day 013 — Notification System

> **Interview Goal:** Design a multi-channel notification delivery system used across all company services.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Your company has 10 different product teams (payments, orders, social, marketing, etc.) — each needs to send notifications to users. Instead of each team building their own email/SMS/push infrastructure, you build a central Notification Service. Teams send an event to you, and you handle delivery across all channels, respecting user preferences, rate limits, and retry logic.

---

## Functional Requirements

1. Support **three delivery channels**: Push notification, Email, SMS.
2. **Any internal service** can trigger a notification by publishing an event with a template key and recipient ID.
3. Users can **configure preferences**: disable a channel, set quiet hours, choose language.
4. Support **template-based messaging** — service sends `{template: "order_shipped", userId: 123, vars: {...}}`, the notification service renders the actual message.
5. **Idempotent delivery** — if the same event is received twice (network retry), the user should not receive duplicate notifications.
6. **Prioritisation**: Critical alerts (OTP, fraud) bypass quiet hours and deliver immediately. Marketing messages batch and send in off-peak hours.
7. Provide a **delivery status dashboard**: sent, delivered, failed, bounced.
8. Support **scheduled notifications** — "send this email at 9 AM in the user's timezone."

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Critical notification latency | < 5 seconds (OTP, 2FA) |
| Marketing email latency | Within 1 hour of trigger |
| Scale | 1B notifications/day across all channels |
| Idempotency | No duplicate delivery even with retries |
| Fault tolerance | Retry with exponential backoff; dead-letter queue for failures |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Notifications/day | 1B |
| Notifications/second (avg) | ~12,000 |
| Notifications/second (peak) | ~100,000 (flash sale, marketing blast) |
| Email throughput | 500M/day |
| Push notifications | 400M/day |
| SMS | 100M/day |
| Template count | ~5,000 |

---

## Core API to Design

```
# Used by internal services to trigger notifications
POST /notifications/send
    body: {
        templateKey: "order_shipped",
        userId: 123,
        channels: ["push", "email"],   // optional override
        variables: { orderId: "ORD-456", deliveryDate: "2024-12-01" },
        priority: "normal"             // normal | critical
    }

# Used by users to manage preferences
GET  /users/{userId}/preferences
PUT  /users/{userId}/preferences
    body: { email: true, sms: false, push: true, quietHours: "22:00-08:00" }

# Admin / monitoring
GET  /notifications/{notifId}/status  → delivery status for a notification
GET  /notifications/analytics         → delivery rates, failure rates by channel
```

---

## Key Challenges to Think About

- **Multi-channel fan-out:** One event may need to trigger a push, an email, and an SMS. How do you fan out to multiple channels reliably and independently (one channel failing shouldn't block others)?
- **Provider abstraction:** Email is sent via SendGrid or AWS SES. SMS via Twilio or SNS. If one provider is down, how do you failover to another without code changes?
- **Deduplication:** A payment service retries its call due to a timeout. The notification service receives the same event twice. How do you ensure the user gets only one SMS?
- **Scheduling in user's timezone:** A marketing email scheduled for "9 AM user local time" needs to account for 400 different timezones. How do you implement this efficiently without running 400 cron jobs?
- **Rate limiting:** You shouldn't send 100 marketing emails to the same user in a day. How do you enforce per-user rate limits across channels?
- **Backpressure:** A flash sale triggers 10M notifications at once. How do you avoid overwhelming your email provider (which has rate limits of its own)?
- **Dead-letter queue:** An email bounces permanently. How do you record this and avoid retrying (which damages your email domain reputation)?
- **Template rendering:** Templates may have complex logic (if conditions, loops). How do you render 100,000 personalised emails per second?

---

## Clarifying Questions (practice asking these in an interview)

1. Are there more channels beyond push, email, SMS (WhatsApp, Slack, in-app)?
2. How do we handle unsubscribes and regulatory requirements (CAN-SPAM, GDPR)?
3. Is there a maximum number of notifications per user per day?
4. Who manages templates — engineers, or a no-code template editor for marketing teams?
5. Should the API be synchronous (wait for send confirmation) or async (fire and forget)?
6. Do we need real-time delivery reporting or is an hourly report sufficient?

---

## Concepts Tested

`Message queue (Kafka/SQS)` · `Fan-out pattern` · `Idempotency keys` · `Provider abstraction / Strategy pattern` · `Dead-letter queue` · `Scheduled jobs (cron + user timezone)` · `Rate limiting` · `Template engine at scale`
