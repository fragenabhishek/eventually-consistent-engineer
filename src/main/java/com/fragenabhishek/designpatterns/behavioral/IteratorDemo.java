package com.fragenabhishek.designpatterns.behavioral;

import java.util.Iterator;
import java.util.NoSuchElementException;

/*
 * =====================================================
 *  ITERATOR PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Provide a way to sequentially access elements of a collection
 *            without exposing its underlying representation.
 *
 *  Problem:  A DynamicOrderCollection generates orders on the fly rather than
 *            storing them. How do you let clients iterate over it using a
 *            standard for-each loop without exposing internal logic?
 *
 *  Solution: Implement java.lang.Iterable<T> on the collection and provide a
 *            private inner class that implements java.util.Iterator<T>.
 *            The Iterator encapsulates traversal state (currentIndex) and
 *            exposes only hasNext() and next().
 *
 *  Structure:
 *    Iterable<Order>           →  java.lang.Iterable (makes for-each work)
 *    DynamicOrderCollection    →  Concrete collection implementing Iterable<Order>
 *    OrderIterator             →  Private inner Iterator (tracks traversal state)
 *    Order                     →  Element produced by the iterator
 *
 *  Key Insight: The iterator holds all traversal state. The collection itself
 *               remains stateless — multiple iterators can traverse independently.
 *
 *  Real-world: java.util.ArrayList.iterator(), ResultSet in JDBC,
 *              Stream.iterator(), Spring Data Page<T>
 * =====================================================
 */

class DynamicOrderCollection implements Iterable<Order> {

    private final int totalOrders;

    public DynamicOrderCollection(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    @Override
    public Iterator<Order> iterator() {
        return new OrderIterator();
    }

    private class OrderIterator implements Iterator<Order> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return currentIndex < totalOrders;
        }

        @Override
        public Order next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more orders in the collection");
            }
            currentIndex++;
            return new Order(currentIndex, "Customer" + currentIndex);
        }
    }
}

// --- Element ---
class Order {
    private final int id;
    private final String customer;

    public Order(int id, String customer) {
        this.id = id;
        this.customer = customer;
    }

    @Override
    public String toString() {
        return "Order[id=" + id + ", customer=" + customer + "]";
    }
}

// --- Demo ---
public class IteratorDemo {
    public static void main(String[] args) {
        DynamicOrderCollection orders = new DynamicOrderCollection(5);

        // Standard for-each — works because DynamicOrderCollection implements Iterable
        for (Order o : orders) {
            System.out.println(o);
        }
        // Output:
        // Order[id=1, customer=Customer1]
        // Order[id=2, customer=Customer2]
        // ...

        // Multiple independent iterators over the same collection
        System.out.println("\nSecond pass (fresh iterator):");
        for (Order o : orders) {
            System.out.println(o);
        }
    }
}
