package com.fragenabhishek.designpatterns.behavioral;

public class DVDPlayer {
    void on(){
        System.out.println("DVDPlayer is ON");
    }

    void play(String movie){
        System.out.println("DVDPlayer is playing " + movie);
    }
}

class Projector{
    void on(){
        System.out.println("Projector is ON");
    }

    void wideScreenMode(){
        System.out.println("Projector is in wide screen mode");
    }
}

class SoundSystem{
    void on(){
        System.out.println("SoundSystem is ON");
    }
    void setVolume(int level){
        System.out.println("SoundSystem volume is up by " + level);
    }
}

class HomeTheaterFacade{
    private DVDPlayer dvdPlayer;
    private Projector projector;
    private SoundSystem soundSystem;
    public HomeTheaterFacade(DVDPlayer dvd, Projector projector, SoundSystem soundSystem){
        this.dvdPlayer = dvd;
        this.projector = projector;
        this.soundSystem = soundSystem;
    }

    void watchMovie(){
        dvdPlayer.on();;
        projector.on();
        soundSystem.on();
        projector.wideScreenMode();
        dvdPlayer.play("Movie");
        soundSystem.setVolume(10);

    }
}

class Client{
    public static void main(String[] args) {
        HomeTheaterFacade homeTheaterFacade = new HomeTheaterFacade(new DVDPlayer(), new Projector(), new SoundSystem());
        homeTheaterFacade.watchMovie();
    }
}
