package javax.sound.midi;

public class MidiSystem {
    public static Sequencer getSequencer() throws MidiUnavailableException {
        return new Sequencer();
    }
}
