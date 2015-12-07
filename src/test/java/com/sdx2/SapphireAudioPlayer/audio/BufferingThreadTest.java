package test.java.com.sdx2.SapphireAudioPlayer.audio;

import main.java.com.sdx2.SapphireAudioPlayer.audio.BufferingThread;
import main.java.com.sdx2.SapphireAudioPlayer.audio.PlayThread;
import main.java.com.sdx2.SapphireAudioPlayer.audio.data.Track;
import main.java.com.sdx2.SapphireAudioPlayer.audio.data.TrackBuffer;
import main.java.com.sdx2.SapphireAudioPlayer.audio.mp3.MP3Decoder;
import main.java.com.sdx2.SapphireAudioPlayer.audio.mp3.MP3FileReader;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BufferingThreadTest {

    @Test
    public void testOpen() throws Exception {
        TrackBuffer mockedTrackBuffer = mock(TrackBuffer.class);
        PlayThread mockedPlayThread = mock(PlayThread.class);

        MP3FileReader mp3FileReader = new MP3FileReader();
        Track track = mp3FileReader.read(new File("test.mp3"));
        MP3Decoder decoder = new MP3Decoder();
        decoder.open(track);
        BufferingThread bufferingThread = new BufferingThread(mockedTrackBuffer, mockedPlayThread);
        AudioFormat format = decoder.getAudioFormat();
        bufferingThread.open(track, format, false);

        verify(mockedTrackBuffer).addTrack(track, format, false, -1);
    }
}