# LLD – Design Patterns: The Story Edition
### All 23 Gang of Four Patterns, Told as Stories

> **How to read this:** Each pattern has a one-line story hook, the concept in plain English, a minimal code sketch, and a real-world "you've seen this before" moment. No mugging. Just understanding.

---

## Frequency Legend

| Badge | Meaning |
|-------|---------|
| 🔥 Must Know | Asked in almost every interview; used daily in production |
| ⚡ Important | Comes up often; good to explain clearly |
| 📘 Good to Know | Less frequent but shows depth |

---

## The Big Picture – All 23 at a Glance

```
Creational (HOW objects are created)
  🔥 Singleton · 🔥 Factory Method · ⚡ Abstract Factory · 🔥 Builder · 📘 Prototype

Structural (HOW objects are assembled)
  🔥 Adapter · 📘 Bridge · ⚡ Composite · 🔥 Decorator · 🔥 Facade · 📘 Flyweight · 🔥 Proxy

Behavioral (HOW objects communicate)
  ⚡ Chain of Responsibility · ⚡ Command · 📘 Iterator · 📘 Mediator · 📘 Memento
  🔥 Observer · ⚡ State · 🔥 Strategy · 🔥 Template Method · 📘 Visitor · 📘 Interpreter
```

---

# Part 1 – Creational Patterns
> *"Patterns that control how objects come to life."*

---

## 1. Singleton 🔥

**Story:** Your app needs one configuration loader. If every class creates its own, you get 50 file reads and inconsistent configs. You need exactly one instance, shared everywhere.

**Concept:** One instance, globally accessible, lazily created.

```java
public class AppConfig {
    private static volatile AppConfig instance;
    private AppConfig() {}

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) instance = new AppConfig();
            }
        }
        return instance;
    }
}
```

**You've seen this in:** Spring beans are singletons by default. `Runtime.getRuntime()`. Logger instances.

**Gotcha:** Singleton + multithreading = use `volatile` + double-checked locking, or the Bill Pugh holder pattern.

---

## 2. Factory Method 🔥

**Story:** You're building a notification system. Today it sends emails. Tomorrow SMS. Next month, push notifications. You don't want `if-else` chains in your business logic every time a new channel appears.

**Concept:** Define an interface for creating an object, but let subclasses/implementations decide which class to instantiate.

```java
interface Notification {
    void send(String message);
}

class EmailNotification implements Notification {
    public void send(String message) { /* send email */ }
}

class NotificationFactory {
    public static Notification create(String type) {
        return switch (type) {
            case "email" -> new EmailNotification();
            case "sms"   -> new SmsNotification();
            default      -> throw new IllegalArgumentException("Unknown: " + type);
        };
    }
}

// Usage – caller never knows concrete class
Notification n = NotificationFactory.create("email");
n.send("Hello!");
```

**You've seen this in:** `Calendar.getInstance()`, `NumberFormat.getInstance()`, Spring `BeanFactory`.

---

## 3. Abstract Factory ⚡

**Story:** You're building a UI toolkit that works on both Windows and Mac. A button on Mac looks different from Windows. You need families of related objects (MacButton, MacCheckbox) without mixing them with Windows ones.

**Concept:** Factory of factories. Creates families of related objects without specifying concrete classes.

```java
interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

class MacUIFactory implements UIFactory {
    public Button createButton()   { return new MacButton(); }
    public Checkbox createCheckbox() { return new MacCheckbox(); }
}

class WindowsUIFactory implements UIFactory {
    public Button createButton()   { return new WinButton(); }
    public Checkbox createCheckbox() { return new WinCheckbox(); }
}
```

**You've seen this in:** JDBC (`DriverManager` returns DB-specific connections/statements), Spring's `PlatformTransactionManager`.

**Factory Method vs Abstract Factory:** Factory Method creates *one* product. Abstract Factory creates *families* of related products.

---

## 4. Builder 🔥

**Story:** You're creating a `Pizza` object. Small or large? Thin or thick crust? Cheese? Extra toppings? A constructor with 12 parameters is unreadable. What if most are optional?

**Concept:** Build complex objects step by step. Separate construction from representation.

```java
Pizza pizza = new Pizza.Builder("large")
    .crust("thin")
    .cheese(true)
    .topping("mushrooms")
    .topping("olives")
    .build();
```

```java
public class Pizza {
    private final String size;
    private final String crust;
    private final boolean cheese;
    private final List<String> toppings;

    private Pizza(Builder b) {
        this.size     = b.size;
        this.crust    = b.crust;
        this.cheese   = b.cheese;
        this.toppings = b.toppings;
    }

    public static class Builder {
        private final String size;
        private String crust = "regular";
        private boolean cheese = false;
        private List<String> toppings = new ArrayList<>();

        public Builder(String size) { this.size = size; }
        public Builder crust(String c)    { this.crust = c; return this; }
        public Builder cheese(boolean c)  { this.cheese = c; return this; }
        public Builder topping(String t)  { this.toppings.add(t); return this; }
        public Pizza build()              { return new Pizza(this); }
    }
}
```

**You've seen this in:** Lombok `@Builder`, `StringBuilder`, `HttpRequest.Builder`, `UriComponentsBuilder` in Spring.

---

## 5. Prototype 📘

**Story:** You're spawning 1000 game enemies. Each enemy has the same base stats but small variations. Creating each from scratch (DB calls, heavy init) is too slow. Clone a template instead.

**Concept:** Create new objects by copying (cloning) an existing object.

```java
public class Enemy implements Cloneable {
    private String type;
    private int health;

    @Override
    public Enemy clone() {
        try { return (Enemy) super.clone(); }
        catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }
}

Enemy template = new Enemy("Orc", 100);
Enemy clone1   = template.clone(); // cheap copy, then tweak
```

**You've seen this in:** `Object.clone()`, Spring's prototype bean scope, copying config objects.

---

# Part 2 – Structural Patterns
> *"Patterns about how objects are composed to form larger structures."*

---

## 6. Adapter 🔥

**Story:** You have a modern payment system that speaks JSON REST. Your legacy bank integration only speaks SOAP XML. You can't change either side. You build a translator in the middle.

**Concept:** Make incompatible interfaces work together. Wrap one interface to look like another.

```java
// Legacy interface you can't change
interface LegacyBankClient {
    String getXmlBalance(String accountId);
}

// New interface your app expects
interface BankClient {
    double getBalance(String accountId);
}

// Adapter bridges the two
class BankClientAdapter implements BankClient {
    private final LegacyBankClient legacy;

    public BankClientAdapter(LegacyBankClient legacy) { this.legacy = legacy; }

    public double getBalance(String accountId) {
        String xml = legacy.getXmlBalance(accountId); // call legacy
        return parseXmlToDouble(xml);                  // translate
    }
}
```

**You've seen this in:** `Arrays.asList()` (array → List), `InputStreamReader` (bytes → chars), Spring's `HandlerAdapter`.

---

## 7. Bridge 📘

**Story:** You have shapes (Circle, Square) and rendering modes (Vector, Raster). Without Bridge: `VectorCircle`, `RasterCircle`, `VectorSquare`, `RasterSquare` – 4 classes for 2 shapes × 2 renderers. Add one more renderer: 2 more classes. It explodes.

**Concept:** Separate abstraction from implementation so both can vary independently.

```java
interface Renderer { void render(String shape); }
class VectorRenderer implements Renderer { public void render(String s) { /* vector */ } }
class RasterRenderer implements Renderer { public void render(String s) { /* raster */ } }

abstract class Shape {
    protected Renderer renderer;
    Shape(Renderer r) { this.renderer = r; }
    abstract void draw();
}

class Circle extends Shape {
    Circle(Renderer r) { super(r); }
    public void draw() { renderer.render("Circle"); }
}
```

**You've seen this in:** JDBC (your code uses `Connection`/`Statement`; driver provides the impl), logging facades (SLF4J + Logback/Log4j).

---

## 8. Composite ⚡

**Story:** You're building a file system browser. A folder can contain files AND other folders. You want to call `getSize()` on anything – file or folder – and get the right answer without checking the type.

**Concept:** Treat individual objects and compositions (groups) of objects uniformly.

```java
interface FileSystemItem {
    long getSize();
    String getName();
}

class File implements FileSystemItem {
    private long size;
    public long getSize() { return size; }
    public String getName() { return name; }
}

class Folder implements FileSystemItem {
    private List<FileSystemItem> children = new ArrayList<>();
    public void add(FileSystemItem item) { children.add(item); }
    public long getSize() {
        return children.stream().mapToLong(FileSystemItem::getSize).sum(); // recursive!
    }
}
```

**You've seen this in:** HTML DOM tree, Java's `JComponent` (Swing), Org chart hierarchies, menu systems.

---

## 9. Decorator 🔥

**Story:** You have a `Coffee` class. Customer wants milk. Another wants milk + sugar. Another wants milk + sugar + whipped cream. Subclassing every combo = 2^n classes. Instead, wrap the base object and add behaviour layer by layer.

**Concept:** Add behaviour to objects dynamically without changing their class. Wraps the original.

```java
interface Coffee { double getCost(); String getDescription(); }

class SimpleCoffee implements Coffee {
    public double getCost() { return 1.0; }
    public String getDescription() { return "Coffee"; }
}

class MilkDecorator implements Coffee {
    private final Coffee coffee;
    MilkDecorator(Coffee c) { this.coffee = c; }
    public double getCost()        { return coffee.getCost() + 0.25; }
    public String getDescription() { return coffee.getDescription() + ", Milk"; }
}

// Usage
Coffee order = new MilkDecorator(new SugarDecorator(new SimpleCoffee()));
// Coffee, Milk, Sugar → $1.50
```

**You've seen this in:** Java I/O (`BufferedReader` wraps `FileReader`), Spring Security filter chain, HTTP middleware, Lombok `@Delegate`.

---

## 10. Facade 🔥

**Story:** Starting a movie night means: turn on TV, switch to HDMI, turn on sound system, set volume, dim lights, start Netflix. That's 6 steps. A universal remote (facade) does all of it with one button: `movieMode()`.

**Concept:** Provide a simplified interface to a complex subsystem.

```java
class HomeTheatreFacade {
    private TV tv;
    private SoundSystem sound;
    private Lights lights;

    public void movieMode() {
        lights.dim(20);
        tv.on();
        tv.setHDMI();
        sound.on();
        sound.setVolume(30);
    }

    public void off() {
        tv.off(); sound.off(); lights.full();
    }
}
```

**You've seen this in:** Spring's `JdbcTemplate` (facade over JDBC), `RestTemplate`, any service class that orchestrates multiple repos/clients.

---

## 11. Flyweight 📘

**Story:** A game renders 10,000 trees. Each tree has: type, texture, colour (shared), and position (unique per tree). Storing texture for all 10,000 trees = OOM. Share the intrinsic state (texture), store only the extrinsic state (position) per instance.

**Concept:** Share common state among many objects to save memory.

```java
class TreeType { // Flyweight – shared
    String name, texture, colour;
    void draw(int x, int y) { /* draw at position */ }
}

class Tree { // Context – unique
    int x, y;
    TreeType type; // shared reference
}

class TreeFactory {
    private static Map<String, TreeType> cache = new HashMap<>();
    static TreeType get(String name) {
        return cache.computeIfAbsent(name, k -> new TreeType(k));
    }
}
```

**You've seen this in:** Java `String` pool (interned strings), `Integer.valueOf()` cache (-128 to 127), font glyph rendering.

---

## 12. Proxy 🔥

**Story:** Your service calls a slow external API. You want to add caching, logging, and access control – without changing the original service. You put a proxy in front that looks identical to the real thing.

**Concept:** A substitute that controls access to another object. Same interface, added behaviour.

```java
interface PaymentService { void pay(String userId, double amount); }

class RealPaymentService implements PaymentService {
    public void pay(String userId, double amount) { /* actual payment */ }
}

class PaymentServiceProxy implements PaymentService {
    private final RealPaymentService real = new RealPaymentService();
    private final Cache cache = new Cache();

    public void pay(String userId, double amount) {
        log("Payment request: " + userId);            // logging
        if (!isAuthorized(userId)) throw new AccessDeniedException();
        real.pay(userId, amount);                      // delegate to real
    }
}
```

**Types of Proxy:**
- **Virtual Proxy:** Lazy load expensive object (load image only when displayed)
- **Protection Proxy:** Access control
- **Remote Proxy:** Local stand-in for a remote object (RMI, gRPC stub)
- **Caching Proxy:** Cache results

**You've seen this in:** Spring AOP (`@Transactional`, `@Cacheable` are proxy-based), Hibernate lazy loading, gRPC stubs.

---

# Part 3 – Behavioral Patterns
> *"Patterns about how objects talk to each other and share responsibilities."*

---

## 13. Chain of Responsibility ⚡

**Story:** A support ticket comes in. Level-1 support tries to solve it. Can't? Escalate to Level-2. Can't? Escalate to Level-3. Each handler decides: handle it or pass it on.

**Concept:** Pass a request along a chain of handlers. Each handler decides to handle or forward.

```java
abstract class SupportHandler {
    protected SupportHandler next;
    public SupportHandler setNext(SupportHandler h) { this.next = h; return h; }
    public abstract void handle(Ticket ticket);
}

class Level1 extends SupportHandler {
    public void handle(Ticket t) {
        if (t.severity() == LOW) resolve(t);
        else if (next != null) next.handle(t); // pass up
    }
}

// Setup chain
SupportHandler l1 = new Level1();
SupportHandler l2 = new Level2();
SupportHandler l3 = new Level3();
l1.setNext(l2).setNext(l3); // chain

l1.handle(ticket); // entry point
```

**You've seen this in:** Spring Security filter chain, Java Servlet filters, logging levels (DEBUG → INFO → WARN → ERROR), middleware pipelines.

---

## 14. Command ⚡

**Story:** You're building a text editor with undo/redo. You need to store what was done (not just the result), so you can reverse it. Wrap each action as an object.

**Concept:** Encapsulate a request as an object. Supports undo, queue, log of operations.

```java
interface Command { void execute(); void undo(); }

class TypeCommand implements Command {
    private final TextEditor editor;
    private final String text;

    TypeCommand(TextEditor e, String t) { this.editor = e; this.text = t; }

    public void execute() { editor.type(text); }
    public void undo()    { editor.delete(text.length()); }
}

// Invoker keeps history
Deque<Command> history = new ArrayDeque<>();
Command cmd = new TypeCommand(editor, "Hello");
cmd.execute();
history.push(cmd);

// Undo
history.pop().undo();
```

**You've seen this in:** Job queues (each job is a command), database transactions (commit/rollback), UI action history, `Runnable` is essentially a command.

---

## 15. Iterator 📘

**Story:** You have a custom tree data structure. You want to give callers a way to loop through it with a `for-each` loop, without exposing the internal tree structure.

**Concept:** Provide a standard way to traverse a collection without exposing internals.

```java
// Java's Iterable + Iterator – you already use this
for (String item : myCollection) { ... } // uses Iterator under the hood
```

**You've seen this in:** Java's `Iterator`, `Iterable`, all Collection classes, `Stream` (a kind of iterator), cursor in DB result sets.

> **Note:** Java handles this so well natively that you rarely implement it manually — but understanding it helps you build custom collections.

---

## 16. Mediator 📘

**Story:** In an air traffic control system, planes don't talk directly to each other (chaos!). They all talk to the control tower (mediator), which coordinates. Remove the mediator = disaster.

**Concept:** Centralise complex communication between many objects through one mediator. Reduces direct dependencies.

```java
interface AirTrafficControl {
    void notify(Aircraft sender, String event);
}

class Tower implements AirTrafficControl {
    public void notify(Aircraft sender, String event) {
        if ("landing".equals(event)) clearRunway(sender);
    }
}

class Aircraft {
    private AirTrafficControl tower;
    Aircraft(AirTrafficControl t) { this.tower = t; }
    void land() { tower.notify(this, "landing"); }
}
```

**You've seen this in:** Chat room (mediator between users), MVC controller (mediator between model and view), Event bus / message broker.

---

## 17. Memento 📘

**Story:** A game needs a save/restore feature. You want to snapshot the game state without exposing its internals to the save system.

**Concept:** Capture and restore an object's internal state without violating encapsulation.

```java
class GameState { // Memento
    private final int level;
    private final int score;
    GameState(int level, int score) { this.level = level; this.score = score; }
    int getLevel() { return level; }
    int getScore() { return score; }
}

class Game { // Originator
    private int level, score;
    public GameState save()      { return new GameState(level, score); }
    public void restore(GameState s) { this.level = s.getLevel(); this.score = s.getScore(); }
}

class SaveSlot { // Caretaker
    private GameState saved;
    public void save(GameState s) { this.saved = s; }
    public GameState load() { return saved; }
}
```

**You've seen this in:** Browser back/forward history, undo/redo systems, database transaction rollback, `Serializable` for state persistence.

---

## 18. Observer 🔥

**Story:** You're building an e-commerce app. When an order is placed: send email, send SMS, update inventory, notify warehouse. These are 4 separate concerns. You don't want `OrderService` to know about all of them. When order is placed → publish event → subscribers react independently.

**Concept:** One-to-many dependency. When one object changes state, all dependents are notified automatically.

```java
interface OrderListener { void onOrderPlaced(Order order); }

class OrderService {
    private List<OrderListener> listeners = new ArrayList<>();

    public void subscribe(OrderListener l) { listeners.add(l); }

    public void placeOrder(Order order) {
        // core logic
        processPayment(order);
        // notify all subscribers
        listeners.forEach(l -> l.onOrderPlaced(order));
    }
}

// Subscribers
class EmailService  implements OrderListener { public void onOrderPlaced(Order o) { sendEmail(o); } }
class SMSService    implements OrderListener { public void onOrderPlaced(Order o) { sendSMS(o); } }
class WarehouseSvc  implements OrderListener { public void onOrderPlaced(Order o) { notify(o); } }
```

**You've seen this in:** Spring `ApplicationEvent`/`@EventListener`, Kafka (producer-consumer is observer at scale), RxJava/Reactor (reactive streams), JavaScript `addEventListener`.

---

## 19. State ⚡

**Story:** A vending machine behaves differently depending on its state: idle (waiting for coin), has coin (waiting for selection), dispensing, out of stock. Without State pattern: a mess of `if-else` across every method checking current state.

**Concept:** Allow an object to alter its behaviour when its internal state changes. Looks like it changes its class.

```java
interface VendingState {
    void insertCoin(VendingMachine machine);
    void selectItem(VendingMachine machine);
    void dispense(VendingMachine machine);
}

class IdleState implements VendingState {
    public void insertCoin(VendingMachine m) { m.setState(new HasCoinState()); }
    public void selectItem(VendingMachine m) { System.out.println("Insert coin first"); }
    public void dispense(VendingMachine m)   { System.out.println("Insert coin first"); }
}

class VendingMachine {
    private VendingState state = new IdleState();
    public void setState(VendingState s) { this.state = s; }
    public void insertCoin() { state.insertCoin(this); }
    public void selectItem() { state.selectItem(this); }
}
```

**You've seen this in:** Order status (PENDING → CONFIRMED → SHIPPED → DELIVERED), TCP connection states, workflow engines.

---

## 20. Strategy 🔥

**Story:** Your app sorts data. For small datasets, bubble sort is fine. For large ones, quicksort is better. For nearly-sorted data, insertion sort. You want to swap the algorithm at runtime based on the context – without `if-else` everywhere.

**Concept:** Define a family of algorithms, encapsulate each, make them interchangeable.

```java
interface SortStrategy { void sort(int[] data); }

class QuickSort  implements SortStrategy { public void sort(int[] d) { /* quicksort */ } }
class BubbleSort implements SortStrategy { public void sort(int[] d) { /* bubblesort */ } }

class DataProcessor {
    private SortStrategy strategy;
    DataProcessor(SortStrategy s) { this.strategy = s; }
    void setStrategy(SortStrategy s) { this.strategy = s; } // swap at runtime!
    void process(int[] data) { strategy.sort(data); }
}

// Usage
DataProcessor dp = new DataProcessor(new QuickSort());
dp.process(largeData);
dp.setStrategy(new BubbleSort());
dp.process(smallData);
```

**You've seen this in:** `Comparator` (strategy for comparison), payment strategies (credit card, PayPal, crypto), compression algorithms, Spring's `AuthenticationStrategy`.

**Strategy vs State:** Strategy = you choose the algorithm. State = the object changes its own behaviour based on internal state.

---

## 21. Template Method 🔥

**Story:** Every report in your system has the same structure: fetch data → process → format → send. But "fetch" differs per report (some from DB, some from API), and "format" differs (PDF vs CSV). The skeleton stays the same; the steps vary.

**Concept:** Define the skeleton of an algorithm in a base class; defer specific steps to subclasses.

```java
abstract class ReportGenerator {

    // Template method – the fixed skeleton
    public final void generate() {
        fetchData();
        processData();
        formatReport();
        sendReport();
    }

    protected abstract void fetchData();      // subclass decides
    protected abstract void formatReport();   // subclass decides

    // Default implementations (optional override)
    protected void processData()  { /* common processing */ }
    protected void sendReport()   { /* common email send */ }
}

class PDFRevenueReport extends ReportGenerator {
    protected void fetchData()    { /* query revenue DB */ }
    protected void formatReport() { /* generate PDF */ }
}

class CSVUserReport extends ReportGenerator {
    protected void fetchData()    { /* call user API */ }
    protected void formatReport() { /* write CSV */ }
}
```

**You've seen this in:** `HttpServlet.service()` (calls `doGet`/`doPost`), Spring's `JdbcTemplate`, `AbstractList`, any abstract base class with hook methods.

---

## 22. Visitor 📘

**Story:** You have a document with different elements: Paragraph, Image, Table. You need to add operations on them: export to HTML, export to PDF, spell-check. Adding each operation to every class pollutes them. Instead, let a "visitor" carry the operation.

**Concept:** Add operations to objects without modifying them. Separate algorithm from object structure.

```java
interface DocumentElement { void accept(DocumentVisitor visitor); }

class Paragraph implements DocumentElement {
    public void accept(DocumentVisitor v) { v.visit(this); }
}
class Image implements DocumentElement {
    public void accept(DocumentVisitor v) { v.visit(this); }
}

interface DocumentVisitor {
    void visit(Paragraph p);
    void visit(Image i);
}

class HTMLExporter implements DocumentVisitor {
    public void visit(Paragraph p) { /* <p>...</p> */ }
    public void visit(Image i)     { /* <img src=.../> */ }
}

// New operation? Just add a new Visitor class – no changes to elements!
class PDFExporter implements DocumentVisitor { ... }
```

**You've seen this in:** Java compiler AST traversal, file system operations, tax calculation across different product types.

---

## 23. Interpreter 📘

**Story:** Your app needs to evaluate custom expressions from config files like `age > 18 AND country == 'IN'`. You need a small language parser.

**Concept:** Define a grammar for a language and provide an interpreter to deal with that grammar.

```java
interface Expression { boolean interpret(Map<String, Object> context); }

class GreaterThan implements Expression {
    String field; int value;
    public boolean interpret(Map<String, Object> ctx) {
        return (int) ctx.get(field) > value;
    }
}

class AndExpression implements Expression {
    Expression left, right;
    public boolean interpret(Map<String, Object> ctx) {
        return left.interpret(ctx) && right.interpret(ctx);
    }
}

// age > 18 AND country == 'IN'
Expression rule = new AndExpression(
    new GreaterThan("age", 18),
    new Equals("country", "IN")
);
rule.interpret(Map.of("age", 25, "country", "IN")); // true
```

**You've seen this in:** SQL parsers, regex engines, Spring SpEL (Spring Expression Language), rule engines.

---

# Quick Revision – One Line Each

| # | Pattern | One Line | Badge |
|---|---------|----------|-------|
| 1 | Singleton | One instance, globally shared | 🔥 |
| 2 | Factory Method | Let subclasses decide what to create | 🔥 |
| 3 | Abstract Factory | Create families of related objects | ⚡ |
| 4 | Builder | Build complex objects step by step | 🔥 |
| 5 | Prototype | Clone instead of creating from scratch | 📘 |
| 6 | Adapter | Make incompatible interfaces talk | 🔥 |
| 7 | Bridge | Separate abstraction from implementation | 📘 |
| 8 | Composite | Treat single items and groups the same | ⚡ |
| 9 | Decorator | Add behaviour by wrapping, not subclassing | 🔥 |
| 10 | Facade | One simple interface over a complex system | 🔥 |
| 11 | Flyweight | Share common state to save memory | 📘 |
| 12 | Proxy | Surrogate that controls access to the real object | 🔥 |
| 13 | Chain of Responsibility | Pass request along a chain until handled | ⚡ |
| 14 | Command | Wrap a request as an object (supports undo) | ⚡ |
| 15 | Iterator | Standard way to traverse a collection | 📘 |
| 16 | Mediator | Centralise communication through one hub | 📘 |
| 17 | Memento | Snapshot and restore state | 📘 |
| 18 | Observer | Notify all subscribers when state changes | 🔥 |
| 19 | State | Behaviour changes as internal state changes | ⚡ |
| 20 | Strategy | Swap algorithms at runtime | 🔥 |
| 21 | Template Method | Fixed skeleton, variable steps in subclasses | 🔥 |
| 22 | Visitor | Add operations without modifying objects | 📘 |
| 23 | Interpreter | Evaluate sentences in a defined grammar | 📘 |

---

## The 8 You Must Know Cold

If you only have time to master 8, make it these:

> **Singleton · Factory Method · Builder · Adapter · Decorator · Facade · Proxy · Observer · Strategy · Template Method**

These appear in every real Java codebase and come up in almost every LLD interview round.

---

## Spot-the-Pattern: Real Java/Spring Examples

| You see this in Java/Spring | Pattern behind it |
|----------------------------|------------------|
| `@Cacheable`, `@Transactional` | Proxy |
| Spring `@EventListener` | Observer |
| `BufferedReader(new FileReader())` | Decorator |
| `JdbcTemplate`, `RestTemplate` | Facade + Template Method |
| `Comparator` passed to `sort()` | Strategy |
| Spring beans default scope | Singleton |
| `Arrays.asList()` | Adapter |
| Kafka consumer-producer | Observer (at scale) |
| Spring Security filter chain | Chain of Responsibility |
| Lombok `@Builder` | Builder |

---

*Read one pattern per sitting. Close the file. Draw it on paper. Write the code without looking. That's when it sticks.*
