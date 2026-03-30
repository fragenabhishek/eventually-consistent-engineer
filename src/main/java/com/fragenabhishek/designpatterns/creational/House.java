package com.fragenabhishek.designpatterns.creational;

/*
 * =====================================================
 *  BUILDER PATTERN (Creational)
 * =====================================================
 *
 *  Intent:   Build complex objects step by step. Separate construction from representation.
 *
 *  Problem:  A House constructor with 10+ parameters is unreadable:
 *            new House("Villa", 10, 5, true, false, true, ...)
 *            Most params are optional — you'd need dozens of constructor overloads.
 *
 *  Solution: A fluent Builder inner class that sets fields one at a time and
 *            calls build() to produce the final immutable object.
 *
 *  Structure:
 *    House              →  Product (immutable, constructed via private constructor)
 *    House.Builder      →  Builder (fluent setters return 'this', build() validates & creates)
 *
 *  Key Points:
 *    - House constructor is private — only Builder can create it
 *    - Builder methods return 'this' for fluent chaining
 *    - build() validates required fields before construction
 *    - Resulting House is immutable (all fields are final)
 *
 *  Real-world: Lombok @Builder, StringBuilder, HttpRequest.Builder, Spring UriComponentsBuilder
 * =====================================================
 */
class House {

    // --- Product: all fields are final (immutable after construction) ---
    private final String type;
    private final int windows;
    private final int doors;
    private final boolean garage;
    private final boolean pool;

    private House(Builder builder) {
        this.type = builder.type;
        this.windows = builder.windows;
        this.doors = builder.doors;
        this.garage = builder.garage;
        this.pool = builder.pool;
    }

    @Override
    public String toString() {
        return "House{type='" + type + "', windows=" + windows +
                ", doors=" + doors + ", garage=" + garage + ", pool=" + pool + '}';
    }

    // --- Builder: fluent API for step-by-step construction ---
    public static class Builder {
        private String type;        // required
        private int windows;        // optional
        private int doors;          // optional
        private boolean garage;     // optional
        private boolean pool;       // optional

        // Fluent setters — no "set" prefix (Builder convention)
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder windows(int windows) {
            this.windows = windows;
            return this;
        }

        public Builder doors(int doors) {
            this.doors = doors;
            return this;
        }

        public Builder garage(boolean garage) {
            this.garage = garage;
            return this;
        }

        public Builder pool(boolean pool) {
            this.pool = pool;
            return this;
        }

        // Validation + construction
        public House build() {
            if (type == null || type.isEmpty()) {
                throw new IllegalStateException("House type must be set");
            }
            return new House(this);
        }
    }
}

// --- Demo ---
class BuilderDemo {
    public static void main(String[] args) {
        // Build a villa with selected options — readable, no parameter confusion
        House villa = new House.Builder()
                .type("Villa")
                .windows(10)
                .doors(5)
                .garage(true)
                .build();
        System.out.println(villa);
        // House{type='Villa', windows=10, doors=5, garage=true, pool=false}

        // Build a minimal cabin — only required field set
        House cabin = new House.Builder()
                .type("Cabin")
                .windows(2)
                .build();
        System.out.println(cabin);
        // House{type='Cabin', windows=2, doors=0, garage=false, pool=false}
    }
}
