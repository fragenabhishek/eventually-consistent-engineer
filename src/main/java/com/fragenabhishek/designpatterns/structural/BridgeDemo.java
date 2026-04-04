package com.fragenabhishek.designpatterns.structural;

/*
 * =====================================================
 *  BRIDGE PATTERN (Structural)
 * =====================================================
 *
 *  Intent:   Decouple abstraction from implementation so that
 *            both can vary independently.
 *
 *  Problem:  We have multiple types of Orders (Online, Store, Bulk)
 *            and multiple Payment Methods (Card, UPI, Cash).
 *
 *            Without the Bridge pattern, we would need a class for
 *            every combination:
 *              OnlineCardOrder, OnlineUPIOrder, StoreCashOrder...
 *            → Leads to class explosion.
 *
 *  Solution: Separate:
 *            - Abstraction  → Order
 *            - Implementation → PaymentMethod
 *
 *            Then "bridge" them using composition.
 *
 *  Structure:
 *    PaymentMethod (interface) → Implementation hierarchy
 *      ├── CardPayment
 *      ├── UPI
 *      └── Cash
 *
 *    Order (abstract class) → Abstraction hierarchy
 *      ├── OnlineOrder
 *      ├── StoreOrder
 *      └── BulkOrder
 *
 *    Bridge:
 *      Order HAS-A PaymentMethod
 *
 *  Key:
 *    - You can change payment method at runtime
 *    - You can add new orders or payments independently
 *
 *  Real-world:
 *    - Payment gateways in e-commerce apps
 *    - UI themes (Dark/Light) applied to different screens
 *
 * =====================================================
 */

interface PaymentMethod{
    void pay(double amount);
}

class CardPayment implements PaymentMethod{

    @Override
    public void pay(double amount) {
        System.out.println("Pay amount " + amount + " using Card");
    }
}

class UPI implements PaymentMethod{

    @Override
    public void pay(double amount) {
        System.out.println("Pay amount " + amount + " using UPI");
    }
}

class Cash implements PaymentMethod{

    @Override
    public void pay(double amount) {
        System.out.println("Pay amount " + amount + " using Cash");
    }
}

abstract class Order{
    protected PaymentMethod paymentMethod;

    public Order(PaymentMethod paymentMethod){
        this.paymentMethod = paymentMethod;
    }

    abstract void processOrder(double amount);
}

class OnlineOrder extends Order{

    public OnlineOrder(PaymentMethod paymentMethod){
        super(paymentMethod);
    }

    @Override
    void processOrder(double amount) {
        System.out.println("Processing Online Order ....");
        paymentMethod.pay(amount);
    }
}

class StoreOrder extends Order{

    public StoreOrder(PaymentMethod paymentMethod){
        super(paymentMethod);
    }

    @Override
    void processOrder(double amount) {
        System.out.println("Processing Store Order ....");
        paymentMethod.pay(amount);
    }
}

class BlukOrder extends Order{

    public BlukOrder(PaymentMethod paymentMethod){
        super(paymentMethod);
    }

    @Override
    void processOrder(double amount) {
        System.out.println("Processing Bluk Order ....");
        paymentMethod.pay(amount);
    }
}

public class BridgeDemo {
    public static void main(String[] args) {

        Order onlineOrder = new OnlineOrder(new CardPayment());
        onlineOrder.processOrder(123.23);

        Order onlineOrder2 = new OnlineOrder(new UPI());
        onlineOrder2.processOrder(441.2312);

        Order storeOrder = new StoreOrder(new Cash());
        storeOrder.processOrder(54354.43);
    }
}