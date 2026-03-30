package com.fragenabhishek.designpatterns.behavioral;

/*
 * =====================================================
 *  VISITOR PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Add new operations to existing object structures without modifying those objects.
 *
 *  Problem:  A zoo has Lions and Elephants. You need to add operations like Feeding and
 *            HealthCheck. Adding methods to each animal class for every new operation
 *            pollutes the animal classes and violates Single Responsibility.
 *
 *  Solution: Define a Visitor interface with a visit method for each element type.
 *            Each element has an accept(Visitor) method that calls the right visit method.
 *            New operations = new Visitor class, zero changes to elements.
 *
 *  Structure:
 *    Animal (Element)     →  Interface with accept(Visitor)
 *    Lion, Elephant       →  Concrete Elements (accept calls visitor.visitX(this))
 *    AnimalVisitor        →  Visitor interface (one visit method per element type)
 *    FeedingVisitor, etc. →  Concrete Visitors (each implements one operation across all types)
 *
 *  Double Dispatch: The actual method called depends on BOTH the element type (Lion/Elephant)
 *                   AND the visitor type (Feeding/HealthCheck). Java only has single dispatch,
 *                   so the accept() + visit() pair achieves double dispatch manually.
 *
 *  Trade-off: Easy to add new operations (new Visitor). Hard to add new element types
 *             (every Visitor must be updated). Use when element types are stable.
 *
 *  Real-world: Compiler AST traversal, file system operations, tax calculators, document exporters
 * =====================================================
 */

// --- Element interface ---
interface Animal {
    void accept(AnimalVisitor visitor);
}

// --- Visitor interface ---
interface AnimalVisitor {
    void visitLion(Lion lion);
    void visitElephant(Elephant elephant);
}

// --- Concrete Elements ---

class Lion implements Animal {
    @Override
    public void accept(AnimalVisitor visitor) {
        visitor.visitLion(this);   // "I'm a Lion, call the Lion-specific visit on yourself"
    }
}

class Elephant implements Animal {
    @Override
    public void accept(AnimalVisitor visitor) {
        visitor.visitElephant(this);
    }
}

// --- Concrete Visitors (operations) ---

class FeedingVisitor implements AnimalVisitor {
    @Override
    public void visitLion(Lion lion) {
        System.out.println("Feeding the lion meat");
    }

    @Override
    public void visitElephant(Elephant elephant) {
        System.out.println("Feeding the elephant fruits");
    }
}

class HealthCheckVisitor implements AnimalVisitor {
    @Override
    public void visitLion(Lion lion) {
        System.out.println("Checking lion's teeth and claws");
    }

    @Override
    public void visitElephant(Elephant elephant) {
        System.out.println("Checking elephant's trunk and tusks");
    }
}

// --- Demo ---
public class VisitorClient {
    public static void main(String[] args) {
        Animal[] animals = { new Lion(), new Elephant() };

        AnimalVisitor feeder = new FeedingVisitor();
        AnimalVisitor doctor = new HealthCheckVisitor();

        // Apply each visitor to every animal — without modifying animal classes
        for (Animal animal : animals) {
            animal.accept(feeder);
            animal.accept(doctor);
        }
        // Output:
        // Feeding the lion meat
        // Checking lion's teeth and claws
        // Feeding the elephant fruits
        // Checking elephant's trunk and tusks

        // Adding a new operation (e.g., TrainingVisitor) = new class, zero changes to Lion/Elephant
    }
}
