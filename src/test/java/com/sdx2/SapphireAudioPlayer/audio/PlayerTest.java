package test.java.com.sdx2.SapphireAudioPlayer.audio;

import main.java.com.sdx2.SapphireAudioPlayer.audio.Player;
import main.java.com.sdx2.SapphireAudioPlayer.audio.data.Playlist;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class PlayerTest {

    @Test
    public void testPlaylist() throws Exception {
        Player player = new Player();
        Playlist playlist = new Playlist();
        player.setPlayList(playlist);
        assertThat(player.getPlayList()).isEqualTo(playlist);
    }
}