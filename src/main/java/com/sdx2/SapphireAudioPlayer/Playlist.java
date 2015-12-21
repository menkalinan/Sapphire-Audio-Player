package main.java.com.sdx2.SapphireAudioPlayer;

import java.util.ArrayList;


public class Playlist {
    private ArrayList <Song> playlist;

    public Playlist(){
        playlist = new ArrayList<>();
    }

    public ArrayList getPlaylist(){
        return playlist;
    }

    public void addSong(Song song){
        playlist.add(song);
    }

}
