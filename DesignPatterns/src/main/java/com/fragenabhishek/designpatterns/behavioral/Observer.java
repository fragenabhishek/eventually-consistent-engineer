package com.fragenabhishek.designpatterns.behavioral;

import java.util.ArrayList;
import java.util.List;

public interface Observer {
    void update(String weather);
}

class WeatherStation{
    private List<Observer> observers = new ArrayList<>();
    private String weather;

    public void registerObserver(Observer o){
        observers.add(o);
    }

    public void removeObserver(Observer o){
        observers.remove(o);
    }

    public void setWeather(String newWeather){
        this.weather = newWeather;
        notifyObservers();
    }

    private void notifyObservers() {
        for (Observer o : observers) {
            o.update(weather);
        }
    }


}
class DisplayDevice implements Observer {
    private String name;

    public DisplayDevice(String name) {
        this.name = name;
    }

    public void update(String weather) {
        System.out.println(name + " display updated: " + weather);
    }
}

 class Main {
    public static void main(String[] args) {
        WeatherStation station = new WeatherStation();

        DisplayDevice phone = new DisplayDevice("Phone");
        DisplayDevice tablet = new DisplayDevice("Tablet");

        station.registerObserver(phone);
        station.registerObserver(tablet);

        station.setWeather("Sunny");
        station.setWeather("Rainy");
    }
}

