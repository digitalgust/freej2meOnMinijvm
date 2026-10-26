package javax.sound.sampled;

import java.io.IOException;
import java.io.InputStream;

public class AudioSystem {

    public static AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding,
                                                       AudioInputStream sourceStream) {
        return new AudioInputStream();
    }

    public static AudioInputStream getAudioInputStream(InputStream stream) {
        return new AudioInputStream();
    }

    public static Clip getClip() {
        return new ClipStub();
    }

    static class ClipStub implements Clip {
        public void open() {
        }

        public void open(AudioInputStream stream) throws IOException {
        }

        public void close() {
        }

        public int getFramePosition() {
            return 0;
        }

        public void setFramePosition(int frames) {
        }

        public int getFrameLength() {
            return 0;
        }

        public void setMicrosecondPosition(long microseconds) {
        }

        public long getMicrosecondPosition() {
            return 0;
        }

        public void start() {
        }

        public void stop() {
        }

        public boolean isRunning() {
            return false;
        }

        public void loop(int count) {
        }

        public void setLoopPoints(int start, int end) {
        }
    }
}
