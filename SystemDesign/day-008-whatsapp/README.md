# Day 008 — Real-Time Messaging (WhatsApp)

> **Interview Goal:** Design a real-time chat application supporting 1-on-1 and group messaging with delivery guarantees.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

WhatsApp has 2 billion active users who send ~100 billion messages per day. The product promise is simple but the engineering is hard: messages must be delivered reliably, in order, and in real-time — even when the recipient is temporarily offline. End-to-end encryption means the server cannot read message content.

---

## Functional Requirements

1. **1-on-1 messaging** — send and receive text, images, video, and documents.
2. **Group chats** — up to 512 members; all members receive all messages.
3. **Message status** indicators: Sent (✓), Delivered (✓✓), Read (✓✓ blue).
4. **Push notifications** when the app is in the background.
5. **Message history** — users can scroll back through past conversations.
6. **Last seen** and **online status** (optional privacy setting).
7. **Voice and video calls** — out of scope for core design, mention only.
8. **End-to-end encryption** — server stores only encrypted ciphertext.
9. Support **media sharing** — photos up to 100 MB, videos up to 16 MB.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Message delivery latency | < 200 ms (online recipient) |
| Offline delivery | Messages delivered within seconds of recipient coming online |
| Message ordering | Preserved per conversation |
| Durability | Messages never lost (even if server crashes after receipt) |
| Scale | 2B users; 100B messages/day; 2M messages/second peak |
| End-to-end encryption | Content never readable by the server |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Users | 2B total, 500M DAU |
| Messages/day | 100B |
| Messages/second (avg) | ~1.2M |
| Messages/second (peak) | ~2M |
| Avg message size | 100 bytes (text) |
| Storage/day (text) | ~10 TB |
| Media storage/day | ~100 TB (mix of photos/videos) |

---

## Core API to Design

```
WebSocket /ws/connect             → persistent connection for real-time delivery
POST      /messages               → send a message (if WebSocket isn't used for send)
GET       /messages/{chatId}      → fetch message history (paginated)
POST      /groups                 → create a group
POST      /groups/{groupId}/add   → add a member
GET       /groups/{groupId}       → get group info and member list
PUT       /messages/{msgId}/status → update delivery/read status
GET       /users/{userId}/status  → get online/last-seen status
POST      /media/upload           → get pre-signed URL for media upload
```

---

## Key Challenges to Think About

- **Persistent connections at scale:** Each of 500M DAU holds a WebSocket connection to a server. How do you manage millions of open connections without exhausting file descriptors?
- **Message routing:** User A is connected to Server-1. User B is connected to Server-37. How does a message from A reach B?
- **Offline delivery:** B is offline when A sends a message. How do you store it and deliver it the moment B reconnects?
- **Exactly-once delivery:** Networks are unreliable. How do you ensure a message is not delivered twice (or zero times)?
- **Group message fan-out:** A group with 512 members sends a message. You need to deliver it to 511 devices, which may be spread across hundreds of servers. How?
- **Message ordering:** A sends "Hello" then "How are you?" simultaneously from poor network. How do you ensure they arrive in order?
- **End-to-end encryption:** The server cannot decrypt messages. How do you implement message storage, multi-device sync, and group encryption under this constraint?
- **Last-seen / online status at scale:** If 500M users' status changes on connect/disconnect, how do you avoid a thundering herd of presence updates?

---

## Clarifying Questions (practice asking these in an interview)

1. Does the server store message content, or is this a relay-only design?
2. How long should message history be retained?
3. Do we need to support multi-device (same account on phone + iPad + Web)?
4. Should messages be deleted from server after delivery (like original WhatsApp)?
5. What's the maximum group size?
6. Do we need read receipts at the individual recipient level in groups?

---

## Concepts Tested

`WebSocket / long-polling` · `Message queue (Kafka)` · `Persistent connection management` · `Exactly-once delivery (idempotency)` · `Fan-out for groups` · `Offline message storage` · `End-to-end encryption (Signal protocol)` · `Presence system`
