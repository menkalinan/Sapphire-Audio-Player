package main.java.com.sdx2.SapphireAudioPlayer.audio.mp3;

import main.java.com.sdx2.SapphireAudioPlayer.audio.data.Track;
import main.java.com.sdx2.SapphireAudioPlayer.audio.util.AudioUtil;
import javazoom.jl.decoder.*;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

public class MP3Decoder {
    private static final int DECODE_AFTER_SEEK = 9;
    private LinkedHashMap<File, SeekTable> seekTableCache = new LinkedHashMap<File, SeekTable>(10, 0.7f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<File, SeekTable> eldest) {
            return size() > 10;
        }
    };

    private Bitstream bitstream;
    private javazoom.jl.decoder.Decoder decoder;
    private AudioFormat audioFormat;
    private Header readFrame;
    private Track track;

    private long totalSamples;
    private long streamSize;
    private int samplesPerFrame;
    private int sampleOffset = 0;
    private long currentSample;
    private boolean streaming = false;
    private int bitrate;

    private Header skipFrame() throws BitstreamException {
        readFrame = bitstream.readFrame();
        if (readFrame == null) {
            return null;
        }
        bitstream.closeFrame();

        return readFrame;
    }

    private int samplesToMinutes(long samples) {
        return (int) (samples / track.getSampleRate() / 60f);
    }

    private boolean createBitstream(long targetSample) {
        if (bitstream != null)
            try {
                bitstream.close();
            } catch (BitstreamException e) {
                e.printStackTrace();
            }
        bitstream = null;
        try {
            File file = track.getFile();
            FileInputStream fis = new FileInputStream(file);

            int targetFrame = (int) ((double) targetSample / samplesPerFrame);
            sampleOffset = (int) (targetSample - targetFrame * samplesPerFrame) * audioFormat.getFrameSize();
            long s = samplesToMinutes(totalSamples);
            SeekTable seekTable = seekTableCache.get(file);
            if (seekTable == null &&
                    samplesToMinutes(totalSamples) > 10) {
                seekTable = new SeekTable();
                seekTableCache.put(file, seekTable);
            }

            int currentFrame = 0;
            if (seekTable != null) {
                SeekTable.SeekPoint seekPoint = seekTable.get(targetFrame - DECODE_AFTER_SEEK);
                fis.skip(seekPoint.offset);
                currentFrame = seekPoint.frame;
            }

            bitstream = new Bitstream(fis);
            decoder = new javazoom.jl.decoder.Decoder();

            readFrame = null;
            for (int i = currentFrame; i < targetFrame - DECODE_AFTER_SEEK; i++) {
                skipFrame();
                if (seekTable != null && i % 10000 == 0) {
                    seekTable.add(i, streamSize - bitstream.header_pos());
                }
            }

            int framesToDecode = targetFrame < DECODE_AFTER_SEEK ? targetFrame : DECODE_AFTER_SEEK;
            for (int i = 0; i < framesToDecode; i++) {
                readFrame = bitstream.readFrame();
                if (readFrame != null)
                    decoder.decodeFrame(readFrame, bitstream);
                bitstream.closeFrame();
            }

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean open(final Track track) {
        if (track == null)
            return false;
        this.track = track;
        try {
            URI location = track.getLocation();
            InputStream fis;
            if (track.isFile()) {
                streaming = false;
                fis = new FileInputStream(track.getFile());
                streamSize = track.getFile().length();
            }
            else {
                track.setCodec("MP3 Stream");
                streaming = true;
                URLConnection connection = track.getLocation().toURL().openConnection();

                fis = new BufferedInputStream(connection.getInputStream());
            }
            bitstream = new Bitstream(fis);
            Header header = bitstream.readFrame();
            int sampleRate = header.frequency();
            int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
            track.setSampleRate(sampleRate);
            track.setChannels(channels);
            bitrate = track.getBitrate();
            samplesPerFrame = (int) (header.ms_per_frame() * header.frequency() / 1000);
            audioFormat = new AudioFormat(sampleRate, 16, channels, true, false);

            if (!streaming) {
                totalSamples = samplesPerFrame * (header.max_number_of_frames((int) streamSize) + header.min_number_of_frames((int) streamSize)) / 2;
                bitstream.close();
                fis.close();
                createBitstream(0);
            }
            currentSample = 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public boolean seekSample(long targetSample) {
        currentSample = targetSample;
        return createBitstream(targetSample);
    }

    public int decode(byte[] buf) {
        try {
            readFrame = bitstream.readFrame();

            if (readFrame == null) {
                return -1;
            }

            if (readFrame.bitrate_instant() > 0)
                track.setBitrate(readFrame.bitrate_instant() / 1000);

            if (!streaming && currentSample >= totalSamples)
                return -1;
            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(readFrame, bitstream);
            bitstream.closeFrame();
            int dataLen = output.getBufferLength() * 2;
            int len = dataLen - sampleOffset;
            if (dataLen == 0) {
                return 0;
            }

            currentSample += AudioUtil.bytesToSamples(len, audioFormat.getFrameSize());

            if (!streaming && currentSample > totalSamples) {
                len -= AudioUtil.samplesToBytes(currentSample - totalSamples, audioFormat.getFrameSize());
            }
            toByteArray(output.getBuffer(), sampleOffset / 2, len / 2, buf);
            sampleOffset = 0;
            readFrame = null;
            return len;
        } catch (BitstreamException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void close() {
        if (bitstream != null)
            try {
                bitstream.close();
            } catch (BitstreamException e) {
                e.printStackTrace();
            }
        track.setBitrate(bitrate);
        readFrame = null;
    }

    private void toByteArray(short[] samples, int offs, int len, byte[] dest) {
        int idx = 0;
        short s;
        while (len-- > 0) {
            s = samples[offs++];
            dest[idx++] = (byte) s;
            dest[idx++] = (byte) (s >>> 8);
        }
    }
}
