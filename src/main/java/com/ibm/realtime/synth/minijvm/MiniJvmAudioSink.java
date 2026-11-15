/*
 * MiniJVM audio-device adapter for the BSD-licensed android-synth engine.
 * Audio output goes through XGUI's existing MiniAudio device API.
 */
package com.ibm.realtime.synth.minijvm;

import com.ibm.realtime.synth.compat.AudioBuffer;
import com.ibm.realtime.synth.engine.AudioSink;
import com.ibm.realtime.synth.engine.AudioTime;
import org.mini.media.MaDevice;
import org.mini.media.MaDeviceListener;
import org.mini.media.MiniAudio;
import org.mini.reflect.DirectMemObj;

import java.util.Arrays;

public final class MiniJvmAudioSink implements AudioSink, MaDeviceListener {
    public static final int SAMPLE_RATE = 44100;
    private static final int CHANNELS = 2;
    private static final int BYTES_PER_FRAME = CHANNELS * 2;
    private static final int BUFFER_SIZE_FRAMES = 512;
    private static final int RING_CAPACITY_FRAMES = 4096;
    private static final int RING_CAPACITY_BYTES = RING_CAPACITY_FRAMES * BYTES_PER_FRAME;
    private static final int RING_MASK = RING_CAPACITY_BYTES - 1;

    private final byte[] ring = new byte[RING_CAPACITY_BYTES];
    private final byte[] pcmBuffer = new byte[RING_CAPACITY_BYTES];
    private final byte[] callbackBuffer = new byte[RING_CAPACITY_BYTES];
    private final MaDevice device;
    private volatile boolean open = true;
    private volatile int readCounter;
    private volatile int writeCounter;
    private volatile long consumedFrames;
    private volatile AudioTime timeOffset = new AudioTime(0);

    public MiniJvmAudioSink() {
        device = new MaDevice(MiniAudio.mal_device_type_playback,
                MiniAudio.mal_format_s16, CHANNELS, SAMPLE_RATE);
        device.setDeviceListener(this);
    }

    public void startDevice() {
        if (open && !device.isStarted()) device.start();
    }

    @Override
    public void write(AudioBuffer buffer) {
        if (buffer == null || buffer.getChannelCount() < CHANNELS) return;
        int frames = buffer.getSampleCount();
        if (frames <= 0 || frames > RING_CAPACITY_FRAMES) return;
        int bytes = frames * BYTES_PER_FRAME;

        // AudioPullThread expects AudioSink.write() to apply backpressure.
        // Keep that wait on the producer thread; the MiniAudio callback below
        // remains lock-free and can continue draining the ring.
        while (open) {
            int write = writeCounter;
            int used = write - readCounter;
            if (used >= 0 && used <= ring.length && ring.length - used >= bytes) {
                break;
            }
            try {
                Thread.sleep(1L);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        if (!open) return;

        float[] left = buffer.getChannel(0);
        float[] right = buffer.getChannel(1);
        for (int i = 0, out = 0; i < frames; i++) {
            int l = toPcm16(left[i]);
            int r = toPcm16(right[i]);
            pcmBuffer[out++] = (byte) l;
            pcmBuffer[out++] = (byte) (l >>> 8);
            pcmBuffer[out++] = (byte) r;
            pcmBuffer[out++] = (byte) (r >>> 8);
        }

        if (!open) return;
        int write = writeCounter;
        int used = write - readCounter;
        // There is one producer. The consumer can only increase available
        // space after the capacity check above.
        if (used < 0 || used > ring.length || ring.length - used < bytes) return;
        copyIntoRing(pcmBuffer, write & RING_MASK, bytes);
        writeCounter = write + bytes;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        if (!open) return;
        open = false;
        device.close();
    }

    @Override
    public int getChannels() {
        return CHANNELS;
    }

    @Override
    public int getBufferSize() {
        return BUFFER_SIZE_FRAMES;
    }

    @Override
    public int getBufferSizeMillis() {
        return (BUFFER_SIZE_FRAMES * 1000) / SAMPLE_RATE;
    }

    @Override
    public float getSampleRate() {
        return SAMPLE_RATE;
    }

    @Override
    public AudioTime getAudioTime() {
        return new AudioTime(consumedFrames, SAMPLE_RATE).add(timeOffset);
    }

    @Override
    public void setTimeOffset(AudioTime offset) {
        timeOffset = offset == null ? new AudioTime(0) : offset;
    }

    @Override
    public AudioTime getTimeOffset() {
        return timeOffset;
    }

    @Override
    public void onCapture(MaDevice device, int frameCount, DirectMemObj data) {
        // This is a playback-only device.
    }

    @Override
    public int onPlayback(MaDevice device, int frameCount, DirectMemObj data) {
        // Keep the realtime callback nonblocking and free of Java monitors.
        try {
            if (!open || data == null || frameCount <= 0 || frameCount > RING_CAPACITY_FRAMES) {
                return 0;
            }
            int bytes = frameCount * BYTES_PER_FRAME;
            if (data.getDataPtr() == 0 || data.getLength() < bytes) return 0;
            Arrays.fill(callbackBuffer, 0, bytes, (byte) 0);
            int read = readCounter;
            int available = writeCounter - read;
            if (available < 0 || available > ring.length) available = 0;
            int copied = Math.min(bytes, available);
            copyFromRing(callbackBuffer, read & RING_MASK, copied);
            readCounter = read + copied;
            consumedFrames += frameCount;
            data.copyFrom(0, callbackBuffer, 0, bytes);
            return frameCount;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    @Override
    public void onStop(MaDevice device) {
        // Playback is intentionally not coupled to a callback-thread monitor.
    }

    private void copyIntoRing(byte[] source, int ringIndex, int length) {
        int first = Math.min(length, ring.length - ringIndex);
        System.arraycopy(source, 0, ring, ringIndex, first);
        int remaining = length - first;
        if (remaining > 0) System.arraycopy(source, first, ring, 0, remaining);
    }

    private void copyFromRing(byte[] destination, int ringIndex, int length) {
        int first = Math.min(length, ring.length - ringIndex);
        System.arraycopy(ring, ringIndex, destination, 0, first);
        int remaining = length - first;
        if (remaining > 0) System.arraycopy(ring, 0, destination, first, remaining);
    }

    private static int toPcm16(float sample) {
        if (sample > 1.0f) sample = 1.0f;
        if (sample < -1.0f) sample = -1.0f;
        return (int) (sample * (sample < 0 ? 32768.0f : 32767.0f));
    }
}
