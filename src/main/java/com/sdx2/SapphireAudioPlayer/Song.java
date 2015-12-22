package main.java.com.sdx2.SapphireAudioPlayer;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Song {
    private String artist;
    private String title;
    private String url;
    private String duration;
    private String lyricsId;
    private String fileLocation;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLyricsId() {
        return lyricsId;
    }

    public void setLyricsId(String lyricsId) {
        this.lyricsId = lyricsId;
    }

    public InputStream getInputStream(){
        if(url!=null){
            try {
                return (new URL(url).openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
            try {
                return new FileInputStream(fileLocation);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

