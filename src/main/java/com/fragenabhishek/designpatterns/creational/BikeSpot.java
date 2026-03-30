package com.fragenabhishek.designpatterns.creational;

public interface BikeSpot {
    void park();
}

interface CarSpot{
    void park();
}

class IndianBikeSpot implements BikeSpot{

    @Override
    public void park() {
        System.out.println("Bike parking in India");
    }
}

class IndianCarSpot implements CarSpot{

    @Override
    public void park() {
        System.out.println("Car parking in India");
    }
}

interface ParkingFactory{
    CarSpot createCarSpot();
    BikeSpot createBikeSpot();
}


class IndianParkingFactory implements ParkingFactory{

    @Override
    public CarSpot createCarSpot() {
        return new IndianCarSpot();
    }

    @Override
    public BikeSpot createBikeSpot() {
        return new IndianBikeSpot();
    }
}


class USBikeSpot implements BikeSpot{

    @Override
    public void park() {
        System.out.println("Bike parking in US");
    }
}

class USCarSpot implements CarSpot{

    @Override
    public void park() {
        System.out.println("Car parking in US");
    }
}

class USParkingFactory implements ParkingFactory{

    @Override
    public CarSpot createCarSpot() {
        return new USCarSpot();
    }

    @Override
    public BikeSpot createBikeSpot() {
        return new USBikeSpot();
    }
}


class ClientParking{
    public static void main(String[] args) {
        ParkingFactory factory = new USParkingFactory();

        BikeSpot bikeSpot = factory.createBikeSpot();
        CarSpot carSpot = factory.createCarSpot();

        bikeSpot.park();
        carSpot.park();

    }
}