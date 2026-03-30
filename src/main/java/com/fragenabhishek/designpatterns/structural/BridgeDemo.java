package com.fragenabhishek.designpatterns.structural;

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
