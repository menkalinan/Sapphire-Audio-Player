package test.java.com.sdx2.SapphireAudioPlayer.audio;

import main.java.com.sdx2.SapphireAudioPlayer.audio.PlayThread;
import main.java.com.sdx2.SapphireAudioPlayer.audio.Player;
import main.java.com.sdx2.SapphireAudioPlayer.audio.data.BufferEntry;
import main.java.com.sdx2.SapphireAudioPlayer.audio.data.Track;
import main.java.com.sdx2.SapphireAudioPlayer.audio.data.TrackBuffer;
import main.java.com.sdx2.SapphireAudioPlayer.audio.mp3.MP3Decoder;
import main.java.com.sdx2.SapphireAudioPlayer.audio.mp3.MP3FileReader;
import org.junit.Test;
import org.junit.Ignore;
import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PlayThreadTest {

    @Test
    @Ignore
    public void testGetCurrentTrack() throws Exception {
        Player mockedPlayer = mock(Player.class);
        TrackBuffer mockedTrackBuffer = mock(TrackBuffer.class);

        MP3FileReader mp3FileReader = new MP3FileReader();
        Track track = mp3FileReader.read(new File("test.mp3"));
        MP3Decoder mp3Decoder = new MP3Decoder();
        mp3Decoder.open(track);
        BufferEntry entry = new BufferEntry(track, mp3Decoder.getAudioFormat(), -1, false);

        when(mockedTrackBuffer.pollTrack()).thenReturn(entry);

        PlayThread playThread = new PlayThread(mockedPlayer, mockedTrackBuffer);
        assertThat(playThread.openNext()).isTrue();
    }

}
