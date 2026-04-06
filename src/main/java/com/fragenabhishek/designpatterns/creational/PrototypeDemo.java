package com.fragenabhishek.designpatterns.creational;

/*
 * =====================================================
 *  PROTOTYPE PATTERN (Creational)
 * =====================================================
 *
 *  Intent:   Create new objects by cloning an existing object (the prototype)
 *            instead of constructing from scratch.
 *
 *  Problem:  Creating a Car involves heavy setup (DB calls, config loading, etc.).
 *            When you need many similar objects with small variations, cloning
 *            a template is much cheaper than repeating the full creation process.
 *
 *  Solution: Define a Prototype interface with clone(). Concrete classes implement
 *            clone() to return a copy of themselves. Client clones and tweaks.
 *
 *  Structure:
 *    Prototype  →  Interface declaring clone()
 *    Car        →  Concrete Prototype (implements clone by copying its own fields)
 *
 *  Deep vs Shallow copy:
 *    - Shallow: copies primitive fields; reference fields still point to same objects
 *    - Deep: copies everything recursively (needed when object has mutable references)
 *    - This example copies all field values into a new object. Because both fields are
 *      Strings (immutable), this is effectively equivalent to a deep copy. If Car held
 *      mutable objects (e.g. List, Date), you would need to clone those too.
 *
 *  Real-world: Object.clone(), Spring prototype bean scope, copying config/template objects
 * =====================================================
 */

// --- Prototype interface ---
interface Prototype {
    Prototype clone();
}

// --- Concrete Prototype ---
class Car implements Prototype {
    private String engineType;
    private String color;

    public Car(String engineType, String color) {
        this.engineType = engineType;
        this.color = color;
    }

    @Override
    public Prototype clone() {
        return new Car(this.engineType, this.color);  // new independent object; safe because Strings are immutable
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void showDetails() {
        System.out.println("Car [engine=" + engineType + ", color=" + color + "]");
    }
}

// --- Demo ---
public class PrototypeDemo {
    public static void main(String[] args) {
        // Create an expensive-to-build template car
        Car prototype = new Car("V8 Classic", "Red");

        // Clone and customize — much cheaper than building from scratch
        Car car1 = (Car) prototype.clone();
        car1.setColor("Black");

        Car car2 = (Car) prototype.clone();
        car2.setColor("Yellow");

        // Each is an independent object with its own state
        prototype.showDetails();  // Car [engine=V8 Classic, color=Red]
        car1.showDetails();       // Car [engine=V8 Classic, color=Black]
        car2.showDetails();       // Car [engine=V8 Classic, color=Yellow]
    }
}
