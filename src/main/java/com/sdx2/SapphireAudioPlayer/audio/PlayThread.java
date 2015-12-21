package main.java.com.sdx2.SapphireAudioPlayer.audio;

import main.java.com.sdx2.SapphireAudioPlayer.audio.data.BufferEntry;
import main.java.com.sdx2.SapphireAudioPlayer.audio.data.Track;
import main.java.com.sdx2.SapphireAudioPlayer.audio.data.TrackBuffer;
import main.java.com.sdx2.SapphireAudioPlayer.audio.io.AudioOutput;
import main.java.com.sdx2.SapphireAudioPlayer.audio.util.AudioUtil;

import javax.sound.sampled.AudioFormat;

public class PlayThread extends PlayerActor implements Runnable {
    private static final int BUFFER_SIZE = AudioOutput.BUFFER_SIZE;

    private AudioFormat format;
    private Player player;
    private TrackBuffer buffer;
    private final Object lock = new Object();
    private AudioOutput output = new AudioOutput();
    private Track currentTrack;
    private long currentByte;
    private boolean active = false;
    private double playbackTime;
    private long playbackBytes;

    public PlayThread(Player player, TrackBuffer buffer) {
        this.player = player;
        this.buffer = buffer;
    }

    @Override
    public void process(Message message) {
        Object[] params = message.getParams();
        switch (message) {
            case PAUSE:
                setState(!active);
                break;
            case PLAY:
                setState(true);
                break;
            case STOP:
                stop();
                break;
            case FLUSH:
                output.flush();
                break;
            case VOLUME:
                output.setVolume((Float) params[0]);
                break;
        }
    }

    private void stop() {
        output.flush();
        setState(false);
        output.close();
        updatePlaybackTime();
        player.fireEvent(PlayerEvent.PlayerEventCode.STOPPED);
    }

    private void setState(boolean newState) {
        if (active != newState) {
            active = newState;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        byte[] buf = new byte[BUFFER_SIZE];
        while (true) {
            synchronized (lock) {
                try {
                    while (!active) {
                        if (output.isOpen())
                            player.fireEvent(PlayerEvent.PlayerEventCode.PAUSED);
                        output.stop();
                        System.gc();
                        lock.wait();
                    }

                    output.start();
                    player.fireEvent(PlayerEvent.PlayerEventCode.PLAYING_STARTED);
                    out : while (active) {
                        int len = buffer.read(buf, 0, BUFFER_SIZE);
                        if(len != -1){
                            int fff  = 0;
                        }
                        while (len == -1) {
                            if (!openNext()) {
                                stop();
                                break out;
                            }
                            len = buffer.read(buf, 0, BUFFER_SIZE);
                        }
                        currentByte += len;
                        playbackBytes += len;
                        output.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    currentTrack = null;
                    stop();
                }
            }
        }
    }

    public boolean openNext() {
        try {
            BufferEntry nextEntry = buffer.pollTrack();
            if (nextEntry.track == null) {
                return false;
            }
            currentTrack = nextEntry.track;
            if (nextEntry.forced) {
                output.flush();
            }
            format = nextEntry.format;
            output.init(format);
            if (nextEntry.startSample >= 0) {
                currentByte = AudioUtil.samplesToBytes(nextEntry.startSample, format.getFrameSize());
                player.fireEvent(PlayerEvent.PlayerEventCode.SEEK_FINISHED);
            } else {
                currentByte = 0;
                updatePlaybackTime();
                player.fireEvent(PlayerEvent.PlayerEventCode.FILE_OPENED);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updatePlaybackTime() {
        if (format != null) {
            playbackTime = AudioUtil.bytesToMillis(
                    playbackBytes, format);
        }
        playbackBytes = 0;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public AudioOutput getOutput() {
        return output;
    }

    public boolean isActive() {
        return active;
    }

    public long getCurrentSample() {
        if (format != null) {
            return AudioUtil.bytesToSamples(currentByte, format.getFrameSize());
        } else return 0;
    }

    public double getPlaybackTime() {
        return playbackTime;
    }
}
