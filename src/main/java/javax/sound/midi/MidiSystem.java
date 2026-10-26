package javax.sound.midi;

import java.io.IOException;
import java.io.InputStream;

public class MidiSystem {

    public static Sequencer getSequencer() throws MidiUnavailableException {
        return new SequencerStub();
    }

    static class SequencerStub implements Sequencer {
        public void open() {
        }

        public void start() {
        }

        public void stop() {
        }

        public void close() {
        }

        public boolean isRunning() {
            return false;
        }

        public void setLoopCount(int count) {
        }

        public void setSequence(Sequence sequence) {
        }

        public void setSequence(InputStream stream) throws IOException {
        }

        public long getTickPosition() {
            return 0;
        }

        public long getTickLength() {
            return 0;
        }

        public void setTickPosition(long tick) {
        }
    }
}
