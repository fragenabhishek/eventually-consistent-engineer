package com.fragenabhishek.designpatterns.behavioral;

/*
 * =====================================================
 *  STRATEGY PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Define a family of algorithms, encapsulate each one, and make them
 *            interchangeable at runtime.
 *
 *  Problem:  Payment processing supports Card and NetBanking today. Tomorrow: UPI, Crypto.
 *            If-else chains in the payment method grow endlessly and violate Open/Closed.
 *
 *  Solution: Extract each algorithm into its own class implementing a Strategy interface.
 *            The Context (Payment) holds a reference to the current strategy and delegates to it.
 *            Swap strategies at runtime without changing the context.
 *
 *  Structure:
 *    PaymentStrategy      →  Strategy interface (defines the algorithm contract)
 *    Card, NetBanking     →  Concrete Strategies (each implements the algorithm differently)
 *    Payment              →  Context (holds a strategy reference, delegates to it)
 *
 *  Strategy vs State:
 *    - Strategy: CLIENT chooses which algorithm to use
 *    - State: OBJECT changes its own behavior based on internal state
 *
 *  Real-world: Comparator (sort strategy), Spring AuthenticationStrategy, compression algorithms
 * =====================================================
 */

// --- Strategy interface ---
interface PaymentStrategy {
    void pay(int amount);
}

// --- Concrete Strategies ---

class Card implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using Card");
    }
}

class NetBanking implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using NetBanking");
    }
}

// --- Context ---
class Payment {
    private PaymentStrategy strategy;

    public Payment(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void pay(int amount) {
        strategy.pay(amount);
    }
}

// --- Demo ---
class StrategyDemo {
    public static void main(String[] args) {
        // Start with Card strategy
        Payment payment = new Payment(new Card());
        payment.pay(100);        // Paid 100 using Card

        // Swap strategy at runtime — no code change in Payment class
        payment.setPaymentStrategy(new NetBanking());
        payment.pay(200);        // Paid 200 using NetBanking

        // Adding a new strategy (e.g., UPI) = one new class, zero changes to Payment
    }
}
