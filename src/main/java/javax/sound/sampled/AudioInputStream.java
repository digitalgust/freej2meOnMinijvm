package javax.sound.sampled;

import java.io.IOException;
import java.io.InputStream;

/**
 * Minimal in-memory AudioInputStream used by FreeJ2ME's WAV player.
 * The actual WAV decoding is handled by MiniAudio's MaDecoder.
 */
public class AudioInputStream extends InputStream {

    private final byte[] data;
    private int position;
    private boolean closed;

    public AudioInputStream() {
        this(new byte[0]);
    }

    public AudioInputStream(byte[] data) {
        this.data = data == null ? new byte[0] : data;
    }

    public byte[] getAudioData() {
        return data;
    }

    @Override
    public int read() throws IOException {
        if (closed || position >= data.length) return -1;
        return data[position++] & 0xff;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (buffer == null) throw new NullPointerException();
        if (offset < 0 || length < 0 || length > buffer.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0) return 0;
        if (closed || position >= data.length) return -1;

        int count = Math.min(length, data.length - position);
        System.arraycopy(data, position, buffer, offset, count);
        position += count;
        return count;
    }

    @Override
    public long skip(long count) throws IOException {
        if (count <= 0 || closed) return 0;
        int skipped = (int) Math.min(count, data.length - position);
        position += skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return closed ? 0 : data.length - position;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }
}
