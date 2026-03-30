package com.fragenabhishek.designpatterns.structural;

/*
 * =====================================================
 *  ADAPTER PATTERN (Structural)
 * =====================================================
 *
 *  Intent:   Make incompatible interfaces work together by wrapping one
 *            to look like another.
 *
 *  Problem:  Your app uses a simple MediaPlayer interface (play by filename).
 *            A third-party library supports VLC and MP4 but through a different
 *            interface (AdvanceMediaPlayer). You can't change either side.
 *
 *  Solution: An Adapter implements the interface the client expects (MediaPlayer)
 *            and internally delegates to the incompatible interface (AdvanceMediaPlayer).
 *            Client code stays clean — it only sees MediaPlayer.
 *
 *  Structure:
 *    MediaPlayer          →  Target interface (what the client expects)
 *    AdvanceMediaPlayer   →  Adaptee interface (the incompatible library)
 *    VlcPlayer, Mp4Player →  Concrete Adaptees
 *    MediaAdapter         →  Adapter (implements Target, wraps Adaptee)
 *
 *  Real-world: Arrays.asList() (array→List), InputStreamReader (bytes→chars), Spring HandlerAdapter
 * =====================================================
 */

// --- Target interface (what our client code uses) ---
public interface MediaPlayer {
    void play(String fileName);
}

// --- Adaptee interface (the third-party / legacy system) ---
interface AdvanceMediaPlayer {
    void playVlc(String fileName);
    void playMp4(String fileName);
}

// --- Concrete Adaptee ---
class AdvancedMediaPlayerImpl implements AdvanceMediaPlayer {
    @Override
    public void playVlc(String fileName) {
        System.out.println("Playing " + fileName + " in VLC format");
    }

    @Override
    public void playMp4(String fileName) {
        System.out.println("Playing " + fileName + " in MP4 format");
    }
}

// --- Adapter: bridges MediaPlayer (target) to AdvanceMediaPlayer (adaptee) ---
class MediaAdapter implements MediaPlayer {
    private final AdvanceMediaPlayer advancedPlayer = new AdvancedMediaPlayerImpl();

    @Override
    public void play(String fileName) {
        if (fileName.endsWith(".mp4")) {
            advancedPlayer.playMp4(fileName);
        } else if (fileName.endsWith(".vlc")) {
            advancedPlayer.playVlc(fileName);
        } else {
            System.out.println("Unsupported format: " + fileName);
        }
    }
}

// --- Demo ---
class AdapterDemo {
    public static void main(String[] args) {
        // Client only knows MediaPlayer interface
        MediaPlayer player = new MediaAdapter();

        player.play("song.mp4");        // Playing song.mp4 in MP4 format
        player.play("movie.vlc");       // Playing movie.vlc in VLC format
        player.play("clip.avi");        // Unsupported format: clip.avi
    }
}
