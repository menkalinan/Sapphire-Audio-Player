package main.java.com.sdx2.SapphireAudioPlayer;

import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;

import java.io.InputStream;

/**
 * Created by ONYX on 22.12.2015.
 */
public class SPlayer extends Thread{
    private Playlist playlist;
    private String fileLocation;
    private boolean loop;
    private Player prehravac;

    public SPlayer(Playlist playlist, boolean loop) {
        this.playlist = playlist;
        this.loop = loop;
    }

    public void run() {

        try {
            do {
                InputStream buff = playlist.getInputStream();
                prehravac = new Player(buff);
                prehravac.play();
                playlist.goNext();
            } while (loop);
        } catch (Exception ioe) {
            // TODO error handling
        }
    }

    public void close(){
        loop = false;
        prehravac.close();
        this.interrupt();
    }
}
