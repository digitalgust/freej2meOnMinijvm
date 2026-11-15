package javax.sound.sampled;

import org.mini.media.MaDecoder;
import org.mini.media.engine.MaEngine;
import org.mini.media.engine.MaSound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * MiniJVM implementation of the small Java Sound surface used by FreeJ2ME.
 * WAV data is decoded by MiniAudio and played through a shared engine so that
 * separate MIDlet players can overlap without opening one output device each.
 */
public class AudioSystem {

    private static MaEngine sharedEngine;
    private static int sharedEngineUsers;

    public static AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding,
                                                       AudioInputStream sourceStream) {
        return sourceStream;
    }

    public static AudioInputStream getAudioInputStream(InputStream stream) throws IOException {
        if (stream == null) throw new NullPointerException("stream");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = stream.read(buffer)) >= 0) {
            if (count > 0) output.write(buffer, 0, count);
        }
        return new AudioInputStream(output.toByteArray());
    }

    public static Clip getClip() {
        return new MiniJvmClip();
    }

    private static synchronized MaEngine getSharedEngine() throws IOException {
        if (sharedEngine == null || sharedEngine.getHandle() == 0) {
            sharedEngine = new MaEngine();
        }
        if (sharedEngine.getHandle() == 0) {
            sharedEngine = null;
            throw new IOException("MiniAudio could not open an output device");
        }
        sharedEngineUsers++;
        return sharedEngine;
    }

    private static synchronized void releaseSharedEngine(MaEngine engine) {
        if (engine == null || engine != sharedEngine || sharedEngineUsers <= 0) return;
        sharedEngineUsers--;
        if (sharedEngineUsers == 0) {
            MaEngine unused = sharedEngine;
            sharedEngine = null;
            try {
                unused.finalize();
            } catch (Throwable ignored) {
            }
        }
    }

    private static final class MiniJvmClip implements Clip {

        private byte[] data;
        private MaEngine engine;
        private MaDecoder decoder;
        private MaSound sound;
        private boolean closed = true;
        private boolean looping;
        private boolean playing;
        private int sampleRate;
        private long frameLength;
        private long framePosition;
        private long startedAtMillis;

        @Override
        public synchronized void open() {
            // FreeJ2ME opens this clip with an AudioInputStream.
        }

        @Override
        public synchronized void open(AudioInputStream stream) throws IOException {
            closeResources();
            if (stream == null) throw new NullPointerException("stream");

            data = stream.getAudioData();
            if (data == null || data.length == 0) {
                data = null;
                throw new IOException("WAV data is empty");
            }

            closed = false;
            looping = false;
            playing = false;
            framePosition = 0;
            sampleRate = 0;
            frameLength = 0;
            readWaveInfo(data);
            engine = getSharedEngine();
            createSound();
            if (sound == null) {
                closeResources();
                data = null;
                closed = true;
                throw new IOException("MiniAudio could not initialize WAV playback");
            }
        }

        @Override
        public synchronized void close() {
            closeResources();
            data = null;
            closed = true;
            looping = false;
            playing = false;
            framePosition = 0;
            frameLength = 0;
            sampleRate = 0;
        }

        @Override
        public synchronized int getFramePosition() {
            updatePosition();
            return framePosition > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) framePosition;
        }

        @Override
        public synchronized void setFramePosition(int frames) {
            if (frames <= 0) {
                rewind();
            }
        }

        @Override
        public synchronized int getFrameLength() {
            return frameLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) frameLength;
        }

        @Override
        public synchronized void setMicrosecondPosition(long microseconds) {
            if (microseconds <= 0) rewind();
        }

        @Override
        public synchronized long getMicrosecondPosition() {
            updatePosition();
            if (sampleRate > 0) return framePosition * 1000000L / sampleRate;
            if (playing) return (System.currentTimeMillis() - startedAtMillis) * 1000L;
            return 0;
        }

        @Override
        public synchronized void start() {
            looping = false;
            startPlayback();
        }

        @Override
        public synchronized void stop() {
            updatePosition();
            if (sound != null) sound.stop();
            playing = false;
        }

        @Override
        public synchronized boolean isRunning() {
            updatePosition();
            return playing && sound != null && sound.isPlaying();
        }

        @Override
        public synchronized void loop(int count) {
            looping = count == LOOP_CONTINUOUSLY;
            startPlayback();
        }

        @Override
        public synchronized void setLoopPoints(int start, int end) {
            // MiniAudio exposes whole-file looping for this playback path.
        }

        private void startPlayback() {
            if (closed || sound == null) return;
            if (!looping && sound.isPlayEnd()) {
                framePosition = 0;
                createSound();
            }
            if (sound == null) return;
            sound.setLooping(looping);
            startedAtMillis = System.currentTimeMillis();
            if (sampleRate > 0) startedAtMillis -= framePosition * 1000L / sampleRate;
            playing = true;
            sound.start();
        }

        private void rewind() {
            framePosition = 0;
            if (!closed && data != null) createSound();
        }

        private void updatePosition() {
            if (!playing || sound == null) return;
            if (sound.isPlayEnd() && !looping) {
                framePosition = frameLength;
                playing = false;
                return;
            }
            if (!sound.isPlaying()) {
                playing = false;
                return;
            }
            if (sampleRate > 0) {
                long elapsed = System.currentTimeMillis() - startedAtMillis;
                if (elapsed < 0) elapsed = 0;
                framePosition = elapsed * sampleRate / 1000L;
                if (looping && frameLength > 0) {
                    framePosition %= frameLength;
                } else if (frameLength > 0 && framePosition > frameLength) {
                    framePosition = frameLength;
                }
            }
        }

        private void createSound() {
            closeSoundResources();
            if (data == null || data.length == 0) return;

            try {
                decoder = new MaDecoder(data);
                if (decoder.getHandle_decoder() == 0) {
                    closeSoundResources();
                    return;
                }
                if (sampleRate <= 0) sampleRate = decoder.getSampleRate();

                sound = new MaSound(engine, decoder);
                if (sound.getHandle() == 0) {
                    closeSoundResources();
                    return;
                }
                sound.setLooping(looping);
            } catch (RuntimeException error) {
                closeSoundResources();
                sound = null;
                System.err.println("Unable to initialize WAV playback: " + error);
            }
        }

        private void closeResources() {
            closeSoundResources();
            MaEngine oldEngine = engine;
            engine = null;
            releaseSharedEngine(oldEngine);
        }

        private void closeSoundResources() {
            if (sound != null) {
                try {
                    sound.stop();
                } catch (RuntimeException ignored) {
                }
                try {
                    sound.close();
                } catch (RuntimeException ignored) {
                }
                sound = null;
            }
            if (decoder != null) {
                try {
                    // MaDecoder currently exposes native cleanup through finalize().
                    decoder.finalize();
                } catch (Throwable ignored) {
                }
                decoder = null;
            }
        }

        private void readWaveInfo(byte[] wav) {
            if (wav.length < 12 || !matches(wav, 0, "RIFF") || !matches(wav, 8, "WAVE")) return;

            int format = 0;
            int blockAlign = 0;
            long byteRate = 0;
            long dataLength = -1;
            int offset = 12;
            while (offset <= wav.length - 8) {
                long chunkLength = readUInt32(wav, offset + 4);
                int payload = offset + 8;
                if (matches(wav, offset, "fmt ") && chunkLength >= 16 && payload <= wav.length - 16) {
                    format = readUInt16(wav, payload);
                    sampleRate = (int) readUInt32(wav, payload + 4);
                    byteRate = readUInt32(wav, payload + 8);
                    blockAlign = readUInt16(wav, payload + 12);
                } else if (matches(wav, offset, "data")) {
                    dataLength = Math.min(chunkLength, wav.length - payload);
                }

                long next = payload + chunkLength + (chunkLength & 1L);
                if (next <= offset || next > wav.length) break;
                offset = (int) next;
            }

            if (dataLength >= 0 && sampleRate > 0) {
                if ((format == 1 || format == 3 || format == 0xfffe) && blockAlign > 0) {
                    frameLength = dataLength / blockAlign;
                } else if (byteRate > 0) {
                    frameLength = dataLength * sampleRate / byteRate;
                }
            }
        }

        private static boolean matches(byte[] data, int offset, String text) {
            if (offset < 0 || offset + 4 > data.length) return false;
            for (int i = 0; i < 4; i++) {
                if ((data[offset + i] & 0xff) != text.charAt(i)) return false;
            }
            return true;
        }

        private static int readUInt16(byte[] data, int offset) {
            return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
        }

        private static long readUInt32(byte[] data, int offset) {
            return (data[offset] & 0xffL)
                    | ((data[offset + 1] & 0xffL) << 8)
                    | ((data[offset + 2] & 0xffL) << 16)
                    | ((data[offset + 3] & 0xffL) << 24);
        }
    }
}
