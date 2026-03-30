# Java Design Patterns — Complete Tracker

## Overview

This repository contains implementations of the **Gang of Four (GoF)** design patterns in Java.
Each pattern is in a single file with a `main()` demo and detailed comments explaining the concept.

---

## All 23 GoF Patterns — Status

### Creational — 5/5 done

| # | Pattern | File | Status |
|---|---------|------|--------|
| 1 | Singleton | [`DatabaseConnection.java`](src/main/java/com/fragenabhishek/designpatterns/creational/DatabaseConnection.java) | Done |
| 2 | Factory Method | [`Notification.java`](src/main/java/com/fragenabhishek/designpatterns/creational/Notification.java) | Done |
| 3 | Abstract Factory | [`BikeSpot.java`](src/main/java/com/fragenabhishek/designpatterns/creational/BikeSpot.java) | Done |
| 4 | Builder | [`House.java`](src/main/java/com/fragenabhishek/designpatterns/creational/House.java) | Done |
| 5 | Prototype | [`PrototypeDemo.java`](src/main/java/com/fragenabhishek/designpatterns/creational/PrototypeDemo.java) | Done |

### Structural — 6/7 done

| # | Pattern | File | Status |
|---|---------|------|--------|
| 6 | Adapter | [`MediaPlayer.java`](src/main/java/com/fragenabhishek/designpatterns/structural/MediaPlayer.java) | Done |
| 7 | Bridge | — | Pending |
| 8 | Composite | [`FileSystem.java`](src/main/java/com/fragenabhishek/designpatterns/structural/FileSystem.java) | Done |
| 9 | Decorator | [`Notification.java`](src/main/java/com/fragenabhishek/designpatterns/structural/Notification.java) | Done |
| 10 | Facade | [`FacadeDemo.java`](src/main/java/com/fragenabhishek/designpatterns/structural/FacadeDemo.java) | Done |
| 11 | Flyweight | [`FlyweightDemo.java`](src/main/java/com/fragenabhishek/designpatterns/structural/FlyweightDemo.java) | Done |
| 12 | Proxy | [`RealImage.java`](src/main/java/com/fragenabhishek/designpatterns/structural/RealImage.java) | Done |

### Behavioral — 8/11 done

| # | Pattern | File | Status |
|---|---------|------|--------|
| 13 | Chain of Responsibility | [`CORDemo.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/CORDemo.java) | Done |
| 14 | Command | [`Command.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/Command.java) | Done |
| 15 | Interpreter | — | Pending |
| 16 | Iterator | — | Pending |
| 17 | Mediator | — | Pending |
| 18 | Memento | [`Memento.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/Memento.java) | Done |
| 19 | Observer | [`Observer.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/Observer.java) | Done |
| 20 | State | [`TrafficLightState.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/TrafficLightState.java) | Done |
| 21 | Strategy | [`PaymentStrategy.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/PaymentStrategy.java) | Done |
| 22 | Template Method | [`Drink.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/Drink.java) | Done |
| 23 | Visitor | [`VisitorClient.java`](src/main/java/com/fragenabhishek/designpatterns/behavioral/VisitorClient.java) | Done |

---

## Summary

| Category | Implemented | Pending |
|----------|:-----------:|:-------:|
| Creational | 5/5 | — |
| Structural | 6/7 | Bridge |
| Behavioral | 8/11 | Iterator, Mediator, Interpreter |
| **Total** | **19/23** | **4** |

---

## Interview Priority

> If you only have time for the top patterns, focus on these (appear in every LLD interview):

**Must Know:** Singleton, Factory, Builder, Adapter, Decorator, Facade, Proxy, Observer, Strategy, Template Method

**Important:** Abstract Factory, Composite, Chain of Responsibility, Command, State

**Good to Know:** Prototype, Flyweight, Memento, Visitor, Bridge, Iterator, Mediator, Interpreter
