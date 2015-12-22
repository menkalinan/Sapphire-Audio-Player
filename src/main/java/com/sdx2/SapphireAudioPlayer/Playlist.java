package main.java.com.sdx2.SapphireAudioPlayer;

import java.io.InputStream;
import java.util.ArrayList;


public class Playlist {
    private ArrayList <Song> playlist;
    private int curr = 0;

    public Playlist(){
        playlist = new ArrayList<>();
    }

    public ArrayList getPlaylist(){
        return playlist;
    }

    public void addSong(Song song){
        playlist.add(song);
    }


    public InputStream getInputStream(){
        return playlist.get(curr).getInputStream();
    }
    public void goNext() {
        curr++;
        if (curr>=playlist.size()){
            curr = 0;
        }
    }
}
