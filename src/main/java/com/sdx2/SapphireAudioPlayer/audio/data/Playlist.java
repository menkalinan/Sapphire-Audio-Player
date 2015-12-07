package main.java.com.sdx2.SapphireAudioPlayer.audio.data;

import org.json.JSONException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Playlist {

    private ArrayList<Track> trackList;
    private int current;

    public Playlist(String response){
        trackList = new ArrayList<Track>();
        parseJSON(response);
    }

    public Playlist(){}

    private void parseJSON(String response){
//        JSONArray jsonArray = null;
//        try {
//            jsonArray = new JSONArray(response) ;
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return;
//        }
//        Track track = new Track();
//        for (int i = 1; i < jsonArray.length() ; i++) {
//            try {
//                track.setArtist((String)(jsonArray.getJSONObject(i).get("artist")));
//                track.setDate((Integer)(jsonArray.getJSONObject(i).get("date")));
//                track.setDuration((Integer)(jsonArray.getJSONObject(i).get("duration")));
//                track.setGenre_id((Integer)(jsonArray.getJSONObject(i).get("genre_id")));
//                track.setLyrics_id((Integer)(jsonArray.getJSONObject(i).get("lyrics_id")));
//                track.setTitle((String)(jsonArray.getJSONObject(i).get("title")));
//                track.setUrl((String)(jsonArray.getJSONObject(i).get("url")));
//                musicList.add(track);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }

//    public void addTrackToPlayList(Track track){
//        this.musicList.add(track);
//    }
//
//    public ArrayList<Track> getMusicList(){
//        return this.musicList;
//    }

    public void savePlaylist() throws JSONException {
//        JSONArray jsonArray = new JSONArray();
//        for(Track track: musicList){
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("artist",track.getArtist());
//            jsonObject.put("date", track.getDate());
//            jsonObject.put("duration", track.getDuration());
//            jsonObject.put("genre_id", track.getGenre_id());
//            jsonObject.put("lyrics_id", track.getLyrics_id());
//            jsonObject.put("title", track.getTitle());
//            jsonObject.put("url", track.getUrl());
//            jsonArray.put(jsonObject);
//        }
//        try {
//
//            FileWriter file = new FileWriter("my_playlist.json");
//            file.write(jsonArray.toString());
//            file.flush();
//            file.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void loadPlaylist() throws IOException {
        FileReader file = new FileReader("my_playlist.json");
        String result = "";
        int c;
        while((c=file.read())!=-1){
            result += Character.toString((char)c);
        }
        parseJSON(result);
    }

    public Track next() {
        current++;
        if (current < trackList.size() - 1) {
            Track track = trackList.get(current);
            if (track.getLocation() == null)
                return next();
            return track;
        } else {
            return null;
        }
    }

    public Track prev() {
        current--;
        if (current >= 0) {
            Track track = trackList.get(current);
            if (track.getLocation() == null)
                return prev();
            return track;
        } else {
            return null;
        }
    }

    private Track set(int index) {
        if (index < trackList.size() - 1 && index >=0) {
            Track track = trackList.get(index);
            if (track.getLocation() == null)
                return trackList.get(index + 1);
            current = index;
            return track;
        } else {
            return null;
        }
    }

}
