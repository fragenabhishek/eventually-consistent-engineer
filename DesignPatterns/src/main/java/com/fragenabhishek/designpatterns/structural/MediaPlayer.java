package com.fragenabhishek.designpatterns.structural;

public interface MediaPlayer {
    void play(String fileName);
}

class AdvanceMediaPlayer {
    void playVls(String fileName){
        System.out.println("Playing " + fileName + " in VLC mode");
    }

    void playMp4(String fileName){
        System.out.println("Playing " + fileName + " in Mp4 mode");
    }
}

class MediaAdapter implements MediaPlayer{
    AdvanceMediaPlayer advanceMediaPlayer = new AdvanceMediaPlayer();
    @Override
    public void play(String fileName) {
        if(fileName.endsWith(".mp4")){
            advanceMediaPlayer.playMp4(fileName);
        } else if (fileName.endsWith(".vlc")) {
            advanceMediaPlayer.playVls(fileName);
        }else {
            System.out.println("Unsupported format");
        }
    }
}

class Main2{
    public static void main(String[] args) {
        MediaPlayer mediaPlayer;  // only knows the interface

        // Depending on file type or requirement, assign the appropriate adapter
        mediaPlayer = new MediaAdapter(); // could later be SuperMediaAdapter
        mediaPlayer.play("song.mp4");
        mediaPlayer.play("movie.vlc");
        mediaPlayer.play("xman.heiv");
    }
}
