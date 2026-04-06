# Day 016 — Payment System

> **Interview Goal:** Design a reliable, consistent, and secure payment processing system.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Money movement is the most consequence-laden operation in software. A payment system must guarantee that a payment either completes fully or not at all — never in a partial state. A user's money must never disappear into a void, and no user should ever be charged twice. You're designing the payment backbone for an e-commerce platform (think Amazon Pay or Stripe).

---

## Functional Requirements

1. Users can **initiate payments** using credit/debit card, UPI, or saved wallet.
2. Payments are processed via **third-party payment gateways** (Stripe, Razorpay, etc.).
3. Every payment must have a clear **final state**: Success, Failed, or Refunded.
4. Support **refunds**: full and partial, with audit trail.
5. Support **idempotency** — retrying a payment request never charges the user twice.
6. Every financial transaction is recorded in an **immutable ledger** (audit log).
7. Users can view their **payment history** and download receipts.
8. Support **multi-currency** transactions with real-time exchange rates.
9. Real-time **fraud detection** — flag or block suspicious transactions.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Consistency | Strong — no partial transactions; ACID guarantees |
| Availability | 99.999% ("five nines") |
| Latency | < 2 seconds for payment confirmation |
| Idempotency | Same payment request retried N times = charged exactly once |
| Auditability | Every state change logged immutably with timestamp |
| Security | PCI-DSS compliant; card data never stored in plain text |
| Scale | 10M transactions/day; peak 5,000 TPS |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Transactions/day | 10M |
| Transactions/second (avg) | ~116 |
| Transactions/second (peak) | 5,000 (sale events) |
| Avg transaction amount | $50 |
| Total money moved/day | $500M |
| Ledger entries/day | 30M (3 entries per payment: debit, credit, fee) |

---

## Core API to Design

```
POST   /payments                         → initiate a payment
    header: Idempotency-Key: <uuid>
    body: { amount, currency, paymentMethod, orderId, userId }
    returns: { paymentId, status: "PENDING" }

GET    /payments/{paymentId}             → get payment status
POST   /payments/{paymentId}/confirm     → confirm (for 3DS flows)
POST   /payments/{paymentId}/refund      → issue a refund
    body: { amount, reason }

GET    /users/{userId}/payments          → payment history
POST   /webhooks/gateway                 → receive async status update from payment gateway
```

---

## Key Challenges to Think About

- **Idempotency:** Client sends payment request, network times out, client retries. The gateway may have already charged the card. How do you detect this and return the original result without a double charge? (Hint: idempotency key + state machine)
- **Distributed transaction (the hardest part):** A payment involves:
  1. Debit user's account
  2. Charge payment gateway
  3. Credit merchant's account
  4. Send confirmation email
  
  If step 3 fails after step 2 already succeeded, how do you recover? This is where the **Saga pattern** comes in — how does it work?
- **Immutable ledger:** Financial records must never be updated or deleted. Every state change creates a new row. How do you model this (event sourcing) and query the current balance efficiently?
- **3DS (Two-factor auth):** Some card payments require the user to authenticate with their bank. This is async — the payment flow pauses, waiting for the user to confirm on their phone. How does your state machine handle this?
- **Fraud detection:** A user makes 10 payments in 5 seconds from 5 different countries. How do you detect and block this in real-time without adding latency to legitimate payments?
- **Currency conversion:** A user in India pays a US merchant in USD. The exchange rate at time of payment is locked in. How do you handle the rate lookup, locking, and reconciliation?
- **Partial refunds:** A merchant issues a $30 refund on a $100 payment. Then issues another $40 refund. How does your ledger model this without going below zero?

---

## Clarifying Questions (practice asking these in an interview)

1. Are we building the payment gateway itself, or an abstraction layer on top of existing gateways?
2. Should we support stored/tokenised card data, or always redirect to gateway?
3. What's the regulatory environment — do we need PCI-DSS, PSD2, RBI compliance?
4. Are refunds instant or T+3 days (bank-standard)?
5. Do we need to support split payments (e.g., pay 50% now, 50% on delivery)?
6. How long should we retain payment records (7 years for tax compliance)?

---

## Concepts Tested

`Idempotency keys` · `Saga pattern (distributed transactions)` · `Event sourcing (immutable ledger)` · `ACID vs BASE` · `3DS / async payment flows` · `Fraud detection (ML scoring)` · `Double-entry bookkeeping` · `PCI-DSS tokenization`
