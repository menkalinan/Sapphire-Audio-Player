package main.java.com.sdx2.SapphireAudioPlayer.audio.mp3;

import main.java.com.sdx2.SapphireAudioPlayer.audio.Player;
import main.java.com.sdx2.SapphireAudioPlayer.audio.data.Track;
import main.java.com.sdx2.SapphireAudioPlayer.audio.util.AudioUtil;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Frame;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.framebody.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static java.lang.Thread.sleep;

public class MP3FileReader {

    public Track read(File file) {
        Track track = new Track();
        track.setLocation(file.toURI().toString());
        return readSingle(track);
    }

    public Track read(String url) {
        Track track = new Track();
        try {
            track.setLocation(new URL(url).toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return (track);
    }

    public Track readSingle(Track track) {

        MP3File mp3File = null;
        try {
            mp3File = new MP3File(track.getFile(), MP3File.LOAD_ALL, true);
        } catch (Exception ignored) {
            System.out.println("Couldn't read file: " + track.getFile());
        }

        ID3v24Tag v24Tag = null;
        if (mp3File != null) {
            try {
                v24Tag = mp3File.getID3v2TagAsv24();
                if (v24Tag != null) {
                    copyTagFields(v24Tag, track);
                }

                ID3v1Tag id3v1Tag = mp3File.getID3v1Tag();
                if (id3v1Tag != null) {
                    copyTagFields(id3v1Tag, track);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            MP3AudioHeader mp3AudioHeader = mp3File.getMP3AudioHeader();
            copyHeaderFields(mp3AudioHeader, track);
        }

        return track;
    }

    protected void copyTagFields(Tag tag, Track track) throws IOException {
        if (tag instanceof ID3v24Tag) {
            ID3v24Tag v24Tag = (ID3v24Tag) tag;
            for (FieldKey key : FieldKey.values()) {
                setTagFieldValues(track, key, v24Tag);
            }
        } else if (tag instanceof ID3v1Tag) {
            ID3v1Tag id3v1Tag = (ID3v1Tag) tag;
            for (FieldKey key : FieldKey.values()) {
                String val = id3v1Tag.getFirst(key);
                if (!(val == null || val.isEmpty())) {
                        track.addTagFieldValue(key, val);
                }
            }
        }
    }

    private void setTagFieldValues(Track track, FieldKey key, ID3v24Tag tag) {
        List<TagField> fields;

        try {
            fields = tag.getFields(key);
        }
        catch (KeyNotFoundException ignored) {
            return;
        }

        for (TagField field : fields) {
            ID3v24Frame frame = (ID3v24Frame) field;
            if (frame.getBody() instanceof FrameBodyTRCK) {
                FrameBodyTRCK body = (FrameBodyTRCK) frame.getBody();
                if (FieldKey.TRACK.equals(key)) {
                    track.addTrack(body.getTrackNo());
                }
                else if (FieldKey.TRACK_TOTAL.equals(key)) {
                    track.addTrackTotal(body.getTrackTotal());
                }
            }
            else if (frame.getBody() instanceof FrameBodyTPOS) {
                FrameBodyTPOS body = (FrameBodyTPOS) frame.getBody();
                if (FieldKey.DISC_NO.equals(key)) {
                    track.addDisc(body.getDiscNo());
                }
                else if (FieldKey.DISC_TOTAL.equals(key)) {
                    track.addDiscTotal(body.getDiscTotal());
                }
            }
            else if (frame.getBody() instanceof FrameBodyCOMM) {
                FrameBodyCOMM body = (FrameBodyCOMM) frame.getBody();
                track.addComment(body.getText());
            }
            else if (frame.getBody() instanceof FrameBodyPOPM) {
                FrameBodyPOPM body = (FrameBodyPOPM) frame.getBody();
                track.addRating(String.valueOf(body.getRating()));
            }
            else if (frame.getBody() instanceof AbstractFrameBodyTextInfo) {
                AbstractFrameBodyTextInfo body = (AbstractFrameBodyTextInfo) frame.getBody();
                    track.addTagFieldValue(key, body.getFirstTextValue());
            }
        }
    }

    protected void copyHeaderFields(MP3AudioHeader header, Track track) {
        if (header != null && track != null) {
            track.setTotalSamples(header.getNoOfSamples());
            track.setTrackLength(header.getTrackLength());
            track.setSampleRate(header.getSampleRateAsNumber());
            track.setStartPosition(header.getMp3StartByte());
            track.setCodec(header.getFormat());
            track.setBitrate((int) header.getBitRateAsNumber());
        }
    }

    public static void main(String[] args) {
        MP3FileReader mp3FileReader = new MP3FileReader();
        //Track track = mp3FileReader.read(("https://cs7-4v4.vk-cdn.net/p21/191e9777a72ebb.mp3?extra=QLBCEOB4GPYjk0UHgF0mvroxHcjni8vEnoiZkLGG9Hxy8a_dAdnaU1GZUuWYWKVqf_YeYoiXloNVfOGKJ_5CI4cIKIRLcbo"));
        Track track = mp3FileReader.read(new File("test.mp3"));
        Player player = new Player();
        player.open(track);
        player.setVolume(0.9f);

    }
}
