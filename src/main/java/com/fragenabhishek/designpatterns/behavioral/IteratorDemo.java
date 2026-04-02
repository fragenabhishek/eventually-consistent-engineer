package com.fragenabhishek.designpatterns.behavioral;

import java.util.Iterator;

class DynamicOrderCollection implements Iterable<Order>{
    private int totalOrders;
    public DynamicOrderCollection(int totalOrders){
        this.totalOrders = totalOrders;
    }
    @Override
    public Iterator<Order> iterator() {
        return new OrderIterator();
    }

    private class OrderIterator implements Iterator<Order>{
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return currentIndex < totalOrders;
        }

        @Override
        public Order next() {
            currentIndex++;
            return new Order(currentIndex, "Customer" + currentIndex);
        }
    }
}


class Order{
    private int id;
    private String customer;
    public Order(int id, String customer){
        this.id = id;
        this.customer = customer;
    }

    @Override
    public String toString() {
        return "Order ID: " + id + ", Customer: " + customer;
    }
}
public class IteratorDemo {
    public static void main(String[] args) {
        DynamicOrderCollection orders = new DynamicOrderCollection(5);

        for(Order o : orders){
            System.out.println(o);
        }
    }
}
