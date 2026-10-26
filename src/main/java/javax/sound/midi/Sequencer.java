package javax.sound.midi;

import java.io.IOException;
import java.io.InputStream;

/*
 * FreeJ2ME is compiled against the real JDK, where Sequencer is an interface,
 * so its bytecode calls these methods via invokeinterface. This shim must keep
 * the same shape or miniJVM throws IncompatibleClassChangeError.
 */
public interface Sequencer {

    void open();

    void start();

    void stop();

    void close();

    boolean isRunning();

    void setLoopCount(int count);

    void setSequence(Sequence sequence);

    void setSequence(InputStream stream) throws IOException;

    long getTickPosition();

    long getTickLength();

    void setTickPosition(long tick);
}
