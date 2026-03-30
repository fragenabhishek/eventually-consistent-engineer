package com.fragenabhishek.designpatterns.creational;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/*
 * =====================================================
 *  FACTORY METHOD PATTERN (Creational)
 * =====================================================
 *
 *  Intent:   Create objects without specifying the exact class — let a factory decide.
 *
 *  Problem:  Adding a new notification channel (email, SMS, push) means touching
 *            if-else chains everywhere the object is created. Violates Open/Closed Principle.
 *
 *  Solution: A factory class maps type strings to constructors. Adding a new type = one line,
 *            zero changes to existing code.
 *
 *  Structure:
 *    Notification             →  Product interface (what all notifications can do)
 *    EmailNotification, etc.  →  Concrete Products
 *    NotificationFactory      →  Factory (Map<String, Supplier> for O(1) lookup)
 *
 *  Why Map<String, Supplier> instead of switch/if-else?
 *    - Open for extension: register new types without modifying factory code
 *    - No risk of missing a break statement
 *    - Clean and testable
 *
 *  Real-world: Calendar.getInstance(), NumberFormat.getInstance(), Spring BeanFactory
 * =====================================================
 */

// --- Product interface ---
public interface Notification {
    void notifyUser();
}

// --- Concrete Products ---

class EmailNotification implements Notification {
    @Override
    public void notifyUser() {
        System.out.println("Sending Email Notification");
    }
}

class SMSNotification implements Notification {
    @Override
    public void notifyUser() {
        System.out.println("Sending SMS Notification");
    }
}

class PushNotification implements Notification {
    @Override
    public void notifyUser() {
        System.out.println("Sending PUSH Notification");
    }
}

// --- Factory ---
class NotificationFactory {

    private static final Map<String, Supplier<Notification>> registry = new HashMap<>();

    static {
        registry.put("EMAIL", EmailNotification::new);
        registry.put("SMS", SMSNotification::new);
        registry.put("PUSH", PushNotification::new);
    }

    public static Notification createNotification(String type) {
        Supplier<Notification> supplier = registry.get(type.toUpperCase());
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown notification type: " + type);
        }
        return supplier.get();
    }
}

// --- Demo ---
class FactoryDemo {
    public static void main(String[] args) {
        // Client never knows the concrete class — just asks the factory
        Notification sms = NotificationFactory.createNotification("SMS");
        sms.notifyUser();   // Sending SMS Notification

        Notification push = NotificationFactory.createNotification("PUSH");
        push.notifyUser();  // Sending PUSH Notification

        Notification email = NotificationFactory.createNotification("EMAIL");
        email.notifyUser(); // Sending Email Notification

        // Unknown type → throws IllegalArgumentException (fail-fast, not silent null)
        // NotificationFactory.createNotification("PIGEON");
    }
}
