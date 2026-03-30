# Design Patterns & System Design — Learning Repository

A hands-on repository for learning and interview preparation covering **Java Design Patterns**, **System Design (HLD)**, and **Backend Engineering** fundamentals.

---

## Repository Structure

```
.
├── src/main/java/com/fragenabhishek/designpatterns/
│   ├── creational/       # Singleton, Factory, Builder, Abstract Factory, Prototype
│   ├── behavioral/       # Observer, Strategy, Command, State, Template Method,
│   │                       Memento, Chain of Responsibility, Visitor, Facade
│   └── structural/       # Decorator, Adapter, Proxy, Composite, Flyweight
│
├── SystemDesign/          # Daily system design practice (HLD)
│   ├── day-001-tiny-url/
│   └── day-002-file-sharing/
│
├── Design_Patterns.md               # Daily pattern log & index
├── LLD_Design_Patterns_Story.md     # All 23 GoF patterns explained as stories
├── HLD_Story.md                     # High-Level Design concepts & case studies
├── The_Backend_Story.md             # Full backend engineering revision guide
└── _archive_first_round_QA_*.md     # Interview Q&A archives
```

---

## Design Patterns — Java Implementations

Each pattern is implemented with a small example and a `main()` method for quick testing.

### Creational

| # | Pattern | File | Key Concept |
|---|---------|------|-------------|
| 1 | Singleton | [`DatabaseConnection.java`](src/main/java/com/fragenabhishek/designpatterns/creational/DatabaseConnection.java) | Single shared instance for DB connection |
| 2 | Factory Method | [`Notification.java`](src/main/java/com/fragenabhishek/designpatterns/creational/Notification.java) | Map + Supplier based factory |
| 3 | Builder | [`House.java`](src/main/java/com/fragenabhishek/designpatterns/creational/House.java) | Step-by-step object construction with validation |
| 4 | Abstract Factory | [`BikeSpot.java`](src/main/java/com/fragenabhishek/designpatterns/creational/BikeSpot.java) | Indian vs US parking spot families |
| 5 | Prototype | [`PrototypeDemo.java`](src/main/java/com/fragenabhishek/designpatterns/creational/PrototypeDemo.java) | Clone-based object creation |

### Behavioral

| # | Pattern | File | Key Concept |
|---|---------|------|-------------|
| 6 | Observer | [`Observer.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/Observer.java) | Weather station notifies display devices |
| 7 | Strategy | [`PaymentStrategy.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/PaymentStrategy.java) | Swappable payment methods at runtime |
| 8 | Command | [`Command.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/Command.java) | Text editor with undo/redo |
| 9 | Template Method | [`Drink.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/Drink.java) | Tea vs Coffee — same skeleton, different steps |
| 10 | State | [`TrafficLightState.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/TrafficLightState.java) | Traffic light state transitions |
| 11 | Memento | [`Memento.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/Memento.java) | Text editor undo/redo with state snapshots |
| 12 | Chain of Responsibility | [`CORDemo.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/CORDemo.java) | Support ticket escalation chain |
| 13 | Visitor | [`VisitorClient.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/VisitorClient.java) | Zoo animal feeding & health check visitors |

### Structural

| # | Pattern | File | Key Concept |
|---|---------|------|-------------|
| 14 | Facade | [`FacadeDemo.java`](src/main/java/com/fragenabhishek/designpatterns/structural/FacadeDemo.java) | Home theater simplified interface |
| 15 | Decorator | [`Notification.java`](src/main/java/com/fragenabhishek/designpatterns/structural/Notification.java) | Logging + Encryption decorators on notifications |
| 16 | Adapter | [`MediaPlayer.java`](src/main/java/com/fragenabhishek/designpatterns/structural/MediaPlayer.java) | Adapt advanced media formats to simple player |
| 17 | Proxy | [`RealImage.java`](src/main/java/com/fragenabhishek/designpatterns/structural/RealImage.java) | Lazy-loading image proxy |
| 18 | Composite | [`FileSystem.java`](src/main/java/com/fragenabhishek/designpatterns/structural/FileSystem.java) | Files and folders with recursive size calculation |
| 19 | Flyweight | [`FlyweightDemo.java`](src/main/java/com/fragenabhishek/designpatterns/structural/FlyweightDemo.java) | Shared bullet types in a game |

---

## Study Guides

| Guide | What It Covers |
|-------|---------------|
| [Design_Patterns.md](Design_Patterns.md) | Daily pattern log — which pattern to study each day |
| [LLD_Design_Patterns_Story.md](LLD_Design_Patterns_Story.md) | All 23 GoF patterns as stories with code sketches and real-world examples |
| [HLD_Story.md](HLD_Story.md) | Scalability, caching, CAP theorem, message queues, 7 system design case studies |
| [The_Backend_Story.md](The_Backend_Story.md) | 15-chapter backend guide: OOP, Collections, Java 8, Concurrency, Spring, Kafka, DBs, Docker/K8s, Cloud, GenAI |

---

## System Design — Daily Practice

Each day folder contains: `README.md` (problem + requirements), `design.md` (HLD + components), `trade-offs.md` (decisions + failure modes), `diagram.md` (Mermaid architecture diagrams).

| Day | Problem | Status |
|-----|---------|--------|
| 001 | [URL Shortener (TinyURL)](SystemDesign/day-001-tiny-url/README.md) | Done |
| 002 | [File Sharing (Pastebin)](SystemDesign/day-002-file-sharing/README.md) | Done |
| 003 | Cloud Storage (Dropbox) | Up Next |
| 004–020 | [Full roadmap](SystemDesign/README.md) | Pending |

---

## How to Run

Open the project in IntelliJ IDEA, navigate to any pattern file, and run its `main()` method directly.
