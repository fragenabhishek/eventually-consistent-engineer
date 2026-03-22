package com.fragenabhishek.designpatterns.behavioral;

public interface TrafficLightState {
    void showLight();
    TrafficLightState next();

}


class RedState implements TrafficLightState{

    @Override
    public void showLight() {
        System.out.println("Red Light - STOP");
    }

    @Override
    public TrafficLightState next() {
        return new GreenState();
    }
}

class GreenState implements TrafficLightState{

    @Override
    public void showLight() {
        System.out.println("Green Light - GO");
    }

    @Override
    public TrafficLightState next() {
        return new YellowState();
    }
}

class YellowState implements TrafficLightState{

    @Override
    public void showLight() {
        System.out.println("Yellow Light - WAIT");
    }

    @Override
    public TrafficLightState next() {
        return new RedState();
    }
}

class TrafficLight{
    private TrafficLightState currentState;

    public TrafficLight(TrafficLightState initialState){
        this.currentState = initialState;
    }

    public void change(){
        currentState = currentState.next();
        currentState.showLight();
    }
}

class Main3{
    public static void main(String[] args) {
        TrafficLight trafficLight = new TrafficLight(new RedState());

        for (int i = 0; i < 6; i++){
            trafficLight.change();
        }
    }
}
