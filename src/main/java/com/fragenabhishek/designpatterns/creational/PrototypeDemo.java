package com.fragenabhishek.designpatterns.creational;

interface Prototype{
    Prototype clone();
}

class Car implements Prototype{

    String engineType;
    String color;

    public Car(String engine, String color){
        this.color = color;
        this.engineType = engine;
    }

    @Override
    public Prototype clone() {
        return new Car(this.engineType,this.color);
    }

    public void setColor(String color){
        this.color = color;
    }

    public void showDetails() {
        System.out.println("Car color: " + color + ", Engine: " + engineType);
    }
}

public class PrototypeDemo {
    public static void main(String[] args) {
        Car prototype = new Car("Classic", "Red");

        Car car1 = (Car)prototype.clone();
        car1.setColor("Black");

        Car car2 = (Car) prototype.clone();
        car2.setColor("Yellow");

        prototype.showDetails();
        car2.showDetails();
        car1.showDetails();
    }
}
