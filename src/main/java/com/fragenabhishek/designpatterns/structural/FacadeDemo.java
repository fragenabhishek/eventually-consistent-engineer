package com.fragenabhishek.designpatterns.structural;

/*
 * =====================================================
 *  FACADE PATTERN (Structural)
 * =====================================================
 *
 *  Intent:   Provide a simplified interface to a complex subsystem.
 *
 *  Problem:  Watching a movie requires: turn on DVD player, turn on projector,
 *            set widescreen mode, turn on sound, set volume, play movie.
 *            That's 6 steps across 3 subsystems. The client shouldn't need to
 *            know the internal wiring.
 *
 *  Solution: A Facade class wraps the complex subsystem and exposes a single
 *            simple method (watchMovie). Internally it orchestrates all the steps.
 *            Clients interact with the facade, not the subsystems directly.
 *
 *  Structure:
 *    DVDPlayer, Projector, SoundSystem  →  Subsystem classes (complex internals)
 *    HomeTheaterFacade                  →  Facade (simplified interface)
 *
 *  Key: The facade doesn't replace the subsystem — clients CAN still use subsystems
 *       directly if they need fine-grained control. Facade is a convenience layer.
 *
 *  Real-world: Spring JdbcTemplate (facade over JDBC), RestTemplate, any service
 *              class that orchestrates multiple repos/clients
 * =====================================================
 */

// --- Subsystem classes ---

class DVDPlayer {
    void on() {
        System.out.println("  DVD Player: ON");
    }

    void play(String movie) {
        System.out.println("  DVD Player: Playing '" + movie + "'");
    }
}

class Projector {
    void on() {
        System.out.println("  Projector: ON");
    }

    void wideScreenMode() {
        System.out.println("  Projector: Widescreen mode");
    }
}

class SoundSystem {
    void on() {
        System.out.println("  Sound System: ON");
    }

    void setVolume(int level) {
        System.out.println("  Sound System: Volume set to " + level);
    }
}

// --- Facade: one simple method hides all subsystem complexity ---
class HomeTheaterFacade {
    private final DVDPlayer dvdPlayer;
    private final Projector projector;
    private final SoundSystem soundSystem;

    public HomeTheaterFacade(DVDPlayer dvd, Projector projector, SoundSystem sound) {
        this.dvdPlayer = dvd;
        this.projector = projector;
        this.soundSystem = sound;
    }

    public void watchMovie(String movie) {
        System.out.println("--- Setting up movie night ---");
        dvdPlayer.on();
        projector.on();
        projector.wideScreenMode();
        soundSystem.on();
        soundSystem.setVolume(10);
        dvdPlayer.play(movie);
        System.out.println("--- Enjoy the movie! ---");
    }
}

// --- Demo ---
public class FacadeDemo {
    public static void main(String[] args) {
        // Client creates subsystems once
        DVDPlayer dvd = new DVDPlayer();
        Projector projector = new Projector();
        SoundSystem sound = new SoundSystem();

        // Facade hides all wiring — one call does everything
        HomeTheaterFacade theater = new HomeTheaterFacade(dvd, projector, sound);
        theater.watchMovie("Inception");

        // Without facade, client would need to call 6+ methods in the right order
    }
}
