package com.fragenabhishek.designpatterns.structural;

// Shared data for bullets
class BulletType {
    private String name;
    private int damage;

    public BulletType(String name, int damage) {
        this.name = name;
        this.damage = damage;
    }

    // getters
    public String getName() { return name; }
    public int getDamage() { return damage; }
}

// Unique bullet instance
class Bullet {
    private int x;
    private int y;
    private BulletType type;  // reference to shared flyweight

    public Bullet(int x, int y, BulletType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void display() {
        System.out.println("Bullet at (" + x + "," + y + ") of type "
                + type.getName() + " with damage " + type.getDamage());
    }
}
public class FlyweightDemo {
    public static void main(String[] args) {
        // shared bullet types
        BulletType pistol = new BulletType("pistol", 10);
        BulletType sniper = new BulletType("sniper", 50);

        // bullets with unique positions
        Bullet b1 = new Bullet(5, 10, pistol);
        Bullet b2 = new Bullet(15, 20, pistol);
        Bullet b3 = new Bullet(50, 60, sniper);

        // display bullets
        b1.display();
        b2.display();
        b3.display();
    }
}
