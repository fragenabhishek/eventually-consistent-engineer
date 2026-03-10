package com.fragenabhishek.designpatterns.creational;

public class SingletonExample {
    private static SingletonExample instance;

    private SingletonExample() {
        // private constructor
    }

    public static SingletonExample getInstance() {
        if (instance == null) {
            instance = new SingletonExample();
        }
        return instance;
    }

    public void showMessage() {
        System.out.println("Hello from Singleton!");
    }

    public static void main(String[] args) {
        SingletonExample singleton = SingletonExample.getInstance();
        singleton.showMessage();
    }
}
