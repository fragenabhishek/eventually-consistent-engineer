package com.fragenabhishek.designpatterns.structural;

/*
 * =====================================================
 *  DECORATOR PATTERN (Structural)
 * =====================================================
 *
 *  Intent:   Add behavior to objects dynamically by wrapping them,
 *            without changing the original class or using subclassing.
 *
 *  Problem:  A notification system needs SMS + logging, SMS + encryption,
 *            email + logging + encryption, etc. Subclassing every combo = 2^N classes.
 *
 *  Solution: Create a base Decorator that wraps a Notification and delegates to it.
 *            Concrete decorators add behavior before/after delegating.
 *            Stack decorators like layers: new Encrypt(new Log(new SMS())).
 *
 *  Structure:
 *    Notification            →  Component interface
 *    SMSNotification, Email  →  Concrete Components (base objects)
 *    NotificationDecorator   →  Base Decorator (wraps a Notification, delegates to it)
 *    LoggingDecorator        →  Concrete Decorator (adds logging before delegating)
 *    EncryptionDecorator     →  Concrete Decorator (modifies message before delegating)
 *
 *  Key Insight: Decorators implement the SAME interface as the component.
 *               Callers don't know (or care) how many layers of decoration exist.
 *
 *  Real-world: Java I/O (BufferedReader wraps FileReader), Spring Security filters, HTTP middleware
 * =====================================================
 */

// --- Component interface ---
public interface Notification {
    void send(String message);
}

// --- Concrete Components ---

class SMSNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

class EmailNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

// --- Base Decorator: wraps a Notification and delegates ---
class NotificationDecorator implements Notification {
    protected final Notification wrapped;

    public NotificationDecorator(Notification wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void send(String message) {
        wrapped.send(message);     // pure delegation — subclasses add behavior
    }
}

// --- Concrete Decorator: adds logging ---
class LoggingDecorator extends NotificationDecorator {
    public LoggingDecorator(Notification wrapped) {
        super(wrapped);
    }

    @Override
    public void send(String message) {
        System.out.println("[LOG] Sending notification...");
        super.send(message);       // add behavior BEFORE delegating
    }
}

// --- Concrete Decorator: encrypts the message ---
class EncryptionDecorator extends NotificationDecorator {
    public EncryptionDecorator(Notification wrapped) {
        super(wrapped);
    }

    @Override
    public void send(String message) {
        String encrypted = "Encrypted(" + message + ")";
        super.send(encrypted);     // modify data BEFORE delegating
    }
}

// --- Demo ---
class DecoratorDemo {
    public static void main(String[] args) {
        // Layer 1: plain SMS
        Notification sms = new SMSNotification();

        // Layer 2: SMS + logging
        Notification logged = new LoggingDecorator(sms);

        // Layer 3: SMS + logging + encryption
        Notification encrypted = new EncryptionDecorator(logged);

        System.out.println("--- Plain SMS ---");
        sms.send("Order created");
        // SMS: Order created

        System.out.println("\n--- SMS + Logging ---");
        logged.send("Order created");
        // [LOG] Sending notification...
        // SMS: Order created

        System.out.println("\n--- SMS + Logging + Encryption ---");
        encrypted.send("Order created");
        // [LOG] Sending notification...
        // SMS: Encrypted(Order created)

        // One-liner stacking: Email + Logging + Encryption
        System.out.println("\n--- Email + Logging + Encryption (one-liner) ---");
        Notification email = new EncryptionDecorator(new LoggingDecorator(new EmailNotification()));
        email.send("Payment received");
    }
}
