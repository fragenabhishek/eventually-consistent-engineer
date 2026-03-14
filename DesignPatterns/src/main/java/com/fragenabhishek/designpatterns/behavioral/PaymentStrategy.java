package com.fragenabhishek.designpatterns.behavioral;

interface PaymentStrategy {
    void pay(int amount);
}

class Card implements PaymentStrategy{

    @Override
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using Card");
    }
}

class NetBanking implements PaymentStrategy{

    @Override
    public void pay(int amount) {
        System.out.println("Paid " + amount + " using NetBanking");
    }
}

class Payment{
    private PaymentStrategy paymentStrategy;
    void setPaymentStrategy(PaymentStrategy strategy){
        this.paymentStrategy = strategy;
    }

    void payment(int amount){
        paymentStrategy.pay(amount);
    }
}

class Main2{

    public static void main(String[] args) {
        Payment payment = new Payment();
        payment.setPaymentStrategy(new Card());
        payment.payment(100);

        payment.setPaymentStrategy(new NetBanking());
        payment.payment(111);
    }

}


