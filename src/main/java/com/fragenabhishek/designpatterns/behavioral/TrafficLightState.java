package com.fragenabhishek.designpatterns.behavioral;

/*
 * =====================================================
 *  STATE PATTERN (Behavioral)
 * =====================================================
 *
 *  Intent:   Allow an object to change its behavior when its internal state changes.
 *            The object appears to change its class.
 *
 *  Problem:  A traffic light behaves differently depending on its current color.
 *            Without State pattern, every method has if-else chains checking the current state.
 *            Adding a new state means touching every method.
 *
 *  Solution: Extract each state into its own class implementing a common State interface.
 *            The context (TrafficLight) delegates behavior to the current state object.
 *            State transitions return the next state object.
 *
 *  Structure:
 *    TrafficLightState       →  State interface (showLight + next)
 *    RedState, GreenState..  →  Concrete States (each knows its behavior and its successor)
 *    TrafficLight            →  Context (holds current state, delegates to it)
 *
 *  State vs Strategy:
 *    - State: the OBJECT transitions between states automatically (internal)
 *    - Strategy: the CLIENT chooses the algorithm (external)
 *
 *  Real-world: Order status (PENDING→CONFIRMED→SHIPPED), TCP connection states, workflow engines
 * =====================================================
 */

// --- State interface ---
public interface TrafficLightState {
    void showLight();
    TrafficLightState next();
}

// --- Concrete States ---

class RedState implements TrafficLightState {
    @Override
    public void showLight() {
        System.out.println("RED Light — STOP");
    }

    @Override
    public TrafficLightState next() {
        return new GreenState();
    }
}

class GreenState implements TrafficLightState {
    @Override
    public void showLight() {
        System.out.println("GREEN Light — GO");
    }

    @Override
    public TrafficLightState next() {
        return new YellowState();
    }
}

class YellowState implements TrafficLightState {
    @Override
    public void showLight() {
        System.out.println("YELLOW Light — SLOW DOWN");
    }

    @Override
    public TrafficLightState next() {
        return new RedState();
    }
}

// --- Context ---
class TrafficLight {
    private TrafficLightState currentState;

    public TrafficLight(TrafficLightState initialState) {
        this.currentState = initialState;
    }

    public void change() {
        currentState.showLight();                // show current
        currentState = currentState.next();      // transition to next
    }
}

// --- Demo ---
class StateDemo {
    public static void main(String[] args) {
        TrafficLight light = new TrafficLight(new RedState());

        // Cycle through 6 state changes: Red → Green → Yellow → Red → Green → Yellow
        for (int i = 0; i < 6; i++) {
            light.change();
        }
    }
}
