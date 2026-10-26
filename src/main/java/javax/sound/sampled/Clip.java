package javax.sound.sampled;

import java.io.IOException;

/*
 * FreeJ2ME is compiled against the real JDK, where Clip is an interface,
 * so its bytecode calls these methods via invokeinterface. This shim must keep
 * the same shape or miniJVM throws IncompatibleClassChangeError.
 */
public interface Clip {

    int LOOP_CONTINUOUSLY = -1;

    void open();

    void open(AudioInputStream stream) throws IOException;

    void close();

    int getFramePosition();

    void setFramePosition(int frames);

    int getFrameLength();

    void setMicrosecondPosition(long microseconds);

    long getMicrosecondPosition();

    void start();

    void stop();

    boolean isRunning();

    void loop(int count);

    void setLoopPoints(int start, int end);
}
