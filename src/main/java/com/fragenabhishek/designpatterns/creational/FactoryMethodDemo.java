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
 *    NotificationChannel                →  Product interface (what all channels can do)
 *    EmailChannel, SMSChannel, etc.     →  Concrete Products
 *    NotificationFactory                →  Factory (Map<String, Supplier> for O(1) lookup)
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
interface NotificationChannel {
    void notifyUser();
}

// --- Concrete Products ---

class EmailChannel implements NotificationChannel {
    @Override
    public void notifyUser() {
        System.out.println("Sending Email Notification");
    }
}

class SMSChannel implements NotificationChannel {
    @Override
    public void notifyUser() {
        System.out.println("Sending SMS Notification");
    }
}

class PushChannel implements NotificationChannel {
    @Override
    public void notifyUser() {
        System.out.println("Sending PUSH Notification");
    }
}

// --- Factory ---
class NotificationFactory {

    private static final Map<String, Supplier<NotificationChannel>> registry = new HashMap<>();

    static {
        registry.put("EMAIL", EmailChannel::new);
        registry.put("SMS", SMSChannel::new);
        registry.put("PUSH", PushChannel::new);
    }

    public static NotificationChannel createNotification(String type) {
        Supplier<NotificationChannel> supplier = registry.get(type.toUpperCase());
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown notification type: " + type);
        }
        return supplier.get();
    }
}

// --- Demo ---
public class FactoryMethodDemo {
    public static void main(String[] args) {
        // Client never knows the concrete class — just asks the factory
        NotificationChannel sms = NotificationFactory.createNotification("SMS");
        sms.notifyUser();   // Sending SMS Notification

        NotificationChannel push = NotificationFactory.createNotification("PUSH");
        push.notifyUser();  // Sending PUSH Notification

        NotificationChannel email = NotificationFactory.createNotification("EMAIL");
        email.notifyUser(); // Sending Email Notification

        // Unknown type → throws IllegalArgumentException (fail-fast, not silent null)
        // NotificationFactory.createNotification("PIGEON");
    }
}
