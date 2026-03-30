package com.fragenabhishek.designpatterns.creational;

/*
 * =====================================================
 *  ABSTRACT FACTORY PATTERN (Creational)
 * =====================================================
 *
 *  Intent:   Create families of related objects without specifying concrete classes.
 *            "Factory of factories."
 *
 *  Problem:  A parking system needs BikeSpot + CarSpot. In India they follow one standard,
 *            in the US another. You don't want to mix IndianBikeSpot with USCarSpot.
 *            Each factory must produce a consistent family.
 *
 *  Solution: An abstract ParkingFactory declares createBikeSpot() and createCarSpot().
 *            Each country has a concrete factory that returns its own family of spots.
 *
 *  Structure:
 *    BikeSpot, CarSpot           →  Abstract Products (what each product can do)
 *    IndianBikeSpot, USBikeSpot  →  Concrete Products (country-specific)
 *    ParkingFactory              →  Abstract Factory (declares creation methods)
 *    IndianParkingFactory, etc.  →  Concrete Factories (produce one family each)
 *
 *  Factory Method vs Abstract Factory:
 *    - Factory Method creates ONE product
 *    - Abstract Factory creates FAMILIES of related products
 *
 *  Real-world: JDBC DriverManager (DB-specific Connection/Statement), Spring PlatformTransactionManager
 * =====================================================
 */

// --- Abstract Products ---

public interface BikeSpot {
    void park();
}

interface CarSpot {
    void park();
}

// --- Concrete Products: India family ---

class IndianBikeSpot implements BikeSpot {
    @Override
    public void park() {
        System.out.println("Bike parking in India");
    }
}

class IndianCarSpot implements CarSpot {
    @Override
    public void park() {
        System.out.println("Car parking in India");
    }
}

// --- Concrete Products: US family ---

class USBikeSpot implements BikeSpot {
    @Override
    public void park() {
        System.out.println("Bike parking in US");
    }
}

class USCarSpot implements CarSpot {
    @Override
    public void park() {
        System.out.println("Car parking in US");
    }
}

// --- Abstract Factory ---

interface ParkingFactory {
    CarSpot createCarSpot();
    BikeSpot createBikeSpot();
}

// --- Concrete Factories ---

class IndianParkingFactory implements ParkingFactory {
    @Override
    public CarSpot createCarSpot() { return new IndianCarSpot(); }

    @Override
    public BikeSpot createBikeSpot() { return new IndianBikeSpot(); }
}

class USParkingFactory implements ParkingFactory {
    @Override
    public CarSpot createCarSpot() { return new USCarSpot(); }

    @Override
    public BikeSpot createBikeSpot() { return new USBikeSpot(); }
}

// --- Demo ---
class AbstractFactoryDemo {
    public static void main(String[] args) {
        // Client works with the abstract factory — doesn't know Indian vs US
        ParkingFactory factory = new USParkingFactory();

        BikeSpot bikeSpot = factory.createBikeSpot();
        CarSpot carSpot = factory.createCarSpot();

        bikeSpot.park();  // Bike parking in US
        carSpot.park();   // Car parking in US

        // Swap to India — zero changes in client code, just a different factory
        factory = new IndianParkingFactory();
        factory.createBikeSpot().park();  // Bike parking in India
        factory.createCarSpot().park();   // Car parking in India
    }
}
