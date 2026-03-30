package com.fragenabhishek.designpatterns.creational;

class House {
    private final String type;
    private final int windows;
    private final int doors;
    private final boolean garage;
    private final boolean pool;


    private House(HouseBuilder builder) {
        this.type = builder.type;
        this.windows = builder.windows;
        this.doors = builder.doors;
        this.garage = builder.garage;
        this.pool = builder.pool;
    }

    @Override
    public String toString() {
        return "House{" +
                "type='" + type + '\'' +
                ", windows=" + windows +
                ", doors=" + doors +
                ", garage=" + garage +
                ", pool=" + pool +
                '}';
    }

    public static class HouseBuilder{
        private  String type;
        private  int windows;
        private  int doors;
        private  boolean garage;
        private  boolean pool;


        public HouseBuilder setType(String type){
            this.type = type;
            return this;
        }

        public HouseBuilder setWindows(int windows){
            this.windows = windows;
            return this;
        }
        public HouseBuilder setDoors(int doors) {
            this.doors = doors;
            return this;
        }

        public HouseBuilder setGarage(boolean garage) {
            this.garage = garage;
            return this;
        }

        public HouseBuilder setSwimmingPool(boolean pool) {
            this.pool = pool;
            return this;
        }

        public House build(){
            if(type == null || type.isEmpty()){
                throw new IllegalStateException("House type must be set");
            }
            return new House(this);
        }


    }
}



 class Main3 {
    public static void main(String[] args) {
        House myHouse = new House.HouseBuilder()
                .setType("Villa")
                .setWindows(10)
                .setDoors(5)
                .setGarage(true)
                .build();

        System.out.println(myHouse);
    }
}