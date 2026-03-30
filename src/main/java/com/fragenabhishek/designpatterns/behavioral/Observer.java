package com.fragenabhishek.designpatterns.behavioral;

import java.util.ArrayList;
import java.util.List;

/*
 * =====================================================
 *  OBSERVER PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Define a one-to-many dependency so that when one object changes state,
 *            all its dependents are notified automatically.
 *
 *  Problem:  A WeatherStation updates its data. Phone, Tablet, and TV displays all
 *            need to reflect the new weather. Without Observer, the station would need
 *            to know about every display — tight coupling, hard to add new displays.
 *
 *  Solution: Station (Subject) maintains a list of Observers. When state changes,
 *            it loops through and calls update() on each. Observers register/unregister themselves.
 *
 *  Structure:
 *    Observer        →  Interface that all subscribers implement
 *    WeatherStation  →  Subject (holds observer list, notifies on change)
 *    DisplayDevice   →  Concrete Observer (reacts to notifications)
 *
 *  Key Principle: Loose coupling — Subject doesn't know concrete observer types,
 *                 only the Observer interface. New observers = zero changes to Subject.
 *
 *  Real-world: Spring @EventListener, Kafka producer-consumer, JavaScript addEventListener
 * =====================================================
 */

// --- Observer interface ---
public interface Observer {
    void update(String weather);
}

// --- Subject (Observable) ---
class WeatherStation {
    private final List<Observer> observers = new ArrayList<>();
    private String weather;

    public void registerObserver(Observer o) {
        observers.add(o);
    }

    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    public void setWeather(String newWeather) {
        this.weather = newWeather;
        notifyObservers();
    }

    private void notifyObservers() {
        for (Observer o : observers) {
            o.update(weather);
        }
    }
}

// --- Concrete Observer ---
class DisplayDevice implements Observer {
    private final String name;

    public DisplayDevice(String name) {
        this.name = name;
    }

    @Override
    public void update(String weather) {
        System.out.println(name + " display updated: " + weather);
    }
}

// --- Demo ---
class ObserverDemo {
    public static void main(String[] args) {
        WeatherStation station = new WeatherStation();

        // Register observers
        DisplayDevice phone = new DisplayDevice("Phone");
        DisplayDevice tablet = new DisplayDevice("Tablet");
        station.registerObserver(phone);
        station.registerObserver(tablet);

        // State change → all observers notified automatically
        station.setWeather("Sunny");
        // Phone display updated: Sunny
        // Tablet display updated: Sunny

        station.setWeather("Rainy");
        // Phone display updated: Rainy
        // Tablet display updated: Rainy

        // Unregister one observer
        station.removeObserver(tablet);
        station.setWeather("Cloudy");
        // Phone display updated: Cloudy   (only phone notified)
    }
}
