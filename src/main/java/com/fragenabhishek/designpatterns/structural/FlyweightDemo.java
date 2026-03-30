package com.fragenabhishek.designpatterns.structural;

import java.util.HashMap;
import java.util.Map;

/*
 * =====================================================
 *  FLYWEIGHT PATTERN (Structural)
 * =====================================================
 *
 *  Intent:   Share common state among many objects to save memory.
 *            Separate intrinsic (shared) state from extrinsic (unique) state.
 *
 *  Problem:  A game renders 10,000 bullets. Each bullet has a type (name, damage) and
 *            a position (x, y). The type data is identical for all bullets of the same kind.
 *            Storing type info in every bullet = massive memory waste.
 *
 *  Solution: Extract shared data (type) into a Flyweight object. A Factory ensures only
 *            one instance per type exists (cached). Each bullet holds a reference to
 *            the shared flyweight + its own unique position.
 *
 *  Structure:
 *    BulletType         →  Flyweight (shared intrinsic state: name, damage)
 *    BulletTypeFactory  →  Flyweight Factory (cache that ensures one instance per type)
 *    Bullet             →  Context (unique extrinsic state: x, y + reference to shared flyweight)
 *
 *  Intrinsic vs Extrinsic:
 *    - Intrinsic:  shared, immutable, stored in flyweight (type name, damage)
 *    - Extrinsic:  unique per object, stored externally (position x, y)
 *
 *  Real-world: Java String pool, Integer.valueOf() cache (-128 to 127), font glyph rendering
 * =====================================================
 */

// --- Flyweight: shared data (one instance per bullet type) ---
class BulletType {
    private final String name;
    private final int damage;

    public BulletType(String name, int damage) {
        this.name = name;
        this.damage = damage;
    }

    public String getName() { return name; }
    public int getDamage() { return damage; }
}

// --- Flyweight Factory: ensures shared instances are reused ---
class BulletTypeFactory {
    private static final Map<String, BulletType> cache = new HashMap<>();

    public static BulletType getType(String name, int damage) {
        return cache.computeIfAbsent(name, k -> {
            System.out.println("  [Factory] Creating new BulletType: " + name);
            return new BulletType(name, damage);
        });
    }

    public static int getCachedTypeCount() {
        return cache.size();
    }
}

// --- Context: unique per-bullet data + reference to shared flyweight ---
class Bullet {
    private final int x;
    private final int y;
    private final BulletType type;     // shared flyweight reference

    public Bullet(int x, int y, BulletType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void display() {
        System.out.println("Bullet at (" + x + "," + y + ") — type: "
                + type.getName() + ", damage: " + type.getDamage());
    }
}

// --- Demo ---
public class FlyweightDemo {
    public static void main(String[] args) {
        // Factory creates BulletType only once per type, then reuses it
        BulletType pistol = BulletTypeFactory.getType("Pistol", 10);
        BulletType sniper = BulletTypeFactory.getType("Sniper", 50);

        // 1000 bullets, but only 2 BulletType objects in memory
        Bullet b1 = new Bullet(5, 10, pistol);
        Bullet b2 = new Bullet(15, 20, pistol);     // same pistol flyweight
        Bullet b3 = new Bullet(50, 60, sniper);
        Bullet b4 = new Bullet(100, 200, BulletTypeFactory.getType("Pistol", 10)); // reused from cache

        b1.display();  // Bullet at (5,10) — type: Pistol, damage: 10
        b2.display();  // Bullet at (15,20) — type: Pistol, damage: 10
        b3.display();  // Bullet at (50,60) — type: Sniper, damage: 50
        b4.display();  // Bullet at (100,200) — type: Pistol, damage: 10

        System.out.println("\nTotal BulletType objects in memory: " + BulletTypeFactory.getCachedTypeCount());
        // 2 (not 4) — that's the memory saving
    }
}
