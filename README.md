# Design Patterns & System Design — Learning Repository

A hands-on repository for learning and interview preparation covering **Java Design Patterns**, **System Design (HLD)**, and **Backend Engineering** fundamentals.

---

## Repository Structure

```
.
├── src/main/java/com/fragenabhishek/designpatterns/
│   ├── creational/       # Singleton, Factory, Builder, Abstract Factory, Prototype
│   ├── behavioral/       # Observer, Strategy, Command, State, Template Method,
│   │                       Memento, Chain of Responsibility, Visitor,
│   │                       Iterator, Mediator, Interpreter
│   └── structural/       # Adapter, Bridge, Composite, Decorator, Facade, Flyweight, Proxy
│
├── SystemDesign/          # Daily system design practice (20-day roadmap)
│   ├── day-001-tiny-url/
│   ├── ...
│   └── day-020-ecommerce/
│
├── Design_Patterns.md               # Complete GoF pattern tracker (23/23 done)
├── LLD_Design_Patterns_Story.md     # All 23 GoF patterns explained as stories
├── HLD_Story.md                     # High-Level Design concepts & case studies
├── The_Backend_Story.md             # Full backend engineering revision guide
└── _archive_first_round_QA_*.md     # Interview Q&A archives
```

---

## Design Patterns — Java Implementations

Each pattern lives in a single file with a detailed header comment (Intent, Problem, Solution, Structure, Real-world examples) and a runnable `main()` demo.

### Creational

| # | Pattern | File | Key Concept |
|---|---------|------|-------------|
| 1 | Singleton | [`DatabaseConnection.java`](src/main/java/com/fragenabhishek/designpatterns/creational/DatabaseConnection.java) | Single shared instance for DB connection |
| 2 | Factory Method | [`FactoryMethodDemo.java`](src/main/java/com/fragenabhishek/designpatterns/creational/FactoryMethodDemo.java) | Map + Supplier based factory |
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
| 11 | Memento | [`MementoDemo.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/MementoDemo.java) | Text editor undo/redo with state snapshots |
| 12 | Chain of Responsibility | [`CORDemo.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/CORDemo.java) | Support ticket escalation chain |
| 13 | Visitor | [`VisitorClient.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/VisitorClient.java) | Zoo animal feeding & health check visitors |
| 14 | Iterator | [`IteratorDemo.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/IteratorDemo.java) | Custom iterator over an order collection |
| 15 | Mediator | [`MediatorDemo.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/MediatorDemo.java) | Form mediator coordinating UI components |
| 16 | Interpreter | [`InterpreterDemo.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/InterpreterDemo.java) | Boolean expression evaluator for employee rules |

### Structural

| # | Pattern | File | Key Concept |
|---|---------|------|-------------|
| 17 | Facade | [`FacadeDemo.java`](src/main/java/com/fragenabhishek/designpatterns/structural/FacadeDemo.java) | Home theater simplified interface |
| 18 | Decorator | [`Notification.java`](src/main/java/com/fragenabhishek/designpatterns/structural/Notification.java) | Logging + Encryption decorators on notifications |
| 19 | Adapter | [`MediaPlayer.java`](src/main/java/com/fragenabhishek/designpatterns/structural/MediaPlayer.java) | Adapt advanced media formats to simple player |
| 20 | Bridge | [`BridgeDemo.java`](src/main/java/com/fragenabhishek/designpatterns/structural/BridgeDemo.java) | Order types × payment methods without class explosion |
| 21 | Proxy | [`RealImage.java`](src/main/java/com/fragenabhishek/designpatterns/structural/RealImage.java) | Lazy-loading image proxy |
| 22 | Composite | [`FileSystem.java`](src/main/java/com/fragenabhishek/designpatterns/structural/FileSystem.java) | Files and folders with recursive size calculation |
| 23 | Flyweight | [`FlyweightDemo.java`](src/main/java/com/fragenabhishek/designpatterns/structural/FlyweightDemo.java) | Shared bullet types in a game |

---

## Study Guides

| Guide | What It Covers | Best For |
|-------|---------------|----------|
| [Interview_Quick_Reference.md](Interview_Quick_Reference.md) | Rapid-fire Q&A cheat sheet: Java, Spring, Kafka, DB, System Design, Behavioral | **Night before an interview** |
| [Design_Patterns.md](Design_Patterns.md) | Complete GoF tracker — all 23/23 patterns implemented | Pattern status tracker |
| [LLD_Design_Patterns_Story.md](LLD_Design_Patterns_Story.md) | All 23 GoF patterns as stories with code sketches and real-world examples | LLD interview prep |
| [HLD_Story.md](HLD_Story.md) | Scalability, caching, CAP theorem, message queues, 7 system design case studies | HLD concept deep-dive |
| [The_Backend_Story.md](The_Backend_Story.md) | 15-chapter guide: OOP, Collections, Java 8, Concurrency, Spring, Kafka, DBs, Docker/K8s, Cloud, GenAI | Full topic deep-dive |

---

## System Design — Daily Practice

Each day folder contains: `README.md` (problem + requirements), `design.md` (HLD + components), `trade-offs.md` (decisions + failure modes), `diagram.md` (Mermaid architecture diagrams).

| Day | Problem | Status |
|-----|---------|--------|
| 001 | [URL Shortener (TinyURL)](SystemDesign/day-001-tiny-url/README.md) | ✅ Fully solved |
| 002 | [File Sharing (Pastebin)](SystemDesign/day-002-file-sharing/README.md) | ✅ Fully solved |
| 003 | [Cloud Storage (Dropbox)](SystemDesign/day-003-dropbox/README.md) | ✅ Fully solved |
| 004 | [Video Streaming (YouTube)](SystemDesign/day-004-youtube/README.md) | ✅ Fully solved |
| 005 | [Netflix (Streaming + CDN)](SystemDesign/day-005-netflix/README.md) | ✅ Fully solved |
| 006 | [Instagram (Photo Feed)](SystemDesign/day-006-instagram/README.md) | ✅ Fully solved |
| 007–020 | [Full roadmap (all requirements written)](SystemDesign/README.md) | 📋 Requirements ready — design it! |

---

## How to Run

This project uses an IntelliJ IDEA module (`DesignPatterns.iml`) with no external build tool required.

**Option 1 — IntelliJ IDEA (recommended)**
1. Open the project root in IntelliJ IDEA.
2. IntelliJ auto-detects the module and marks `src/main/java` as the sources root.
3. Navigate to any pattern file and click the green **Run** arrow next to its `main()` method.

**Option 2 — Command line (javac + java)**
```bash
# From the project root
javac -d out src/main/java/com/fragenabhishek/designpatterns/creational/DatabaseConnection.java
java -cp out com.fragenabhishek.designpatterns.creational.DatabaseConnection
```

> **Tip:** Each file is self-contained — just compile and run the file of interest. No dependencies beyond the JDK.
