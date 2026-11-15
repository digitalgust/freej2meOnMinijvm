/*
 * MiniJVM compatibility buffer for the android-synth engine.
 * The buffer uses interleaved channel containers and floating-point samples.
 */
package com.ibm.realtime.synth.compat;

public final class AudioBuffer {
    private float[][] channels;
    private int sampleCount;
    private float sampleRate;

    public AudioBuffer(int channelCount, int sampleCount, float sampleRate) {
        if (channelCount <= 0 || sampleCount < 0) {
            throw new IllegalArgumentException("invalid audio buffer dimensions");
        }
        channels = new float[channelCount][sampleCount];
        this.sampleCount = sampleCount;
        this.sampleRate = sampleRate;
    }

    public int getChannelCount() {
        return channels.length;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public float[] getChannel(int channel) {
        return channels[channel];
    }

    public void makeSilence() {
        for (int channel = 0; channel < channels.length; channel++) {
            float[] samples = channels[channel];
            for (int i = 0; i < sampleCount; i++) {
                samples[i] = 0.0f;
            }
        }
    }

    public void mix(AudioBuffer other) {
        if (other == null || other.channels.length != channels.length) return;
        int count = Math.min(sampleCount, other.sampleCount);
        for (int channel = 0; channel < channels.length; channel++) {
            float[] target = channels[channel];
            float[] source = other.channels[channel];
            for (int i = 0; i < count; i++) target[i] += source[i];
        }
    }

    public void changeSampleCount(int count, boolean keepData) {
        if (count < 0) throw new IllegalArgumentException("negative sample count");
        int oldCount = sampleCount;
        if (channels.length > 0 && channels[0].length < count) {
            int capacity = Math.max(count, Math.max(64, channels[0].length * 2));
            float[][] replacement = new float[channels.length][capacity];
            for (int channel = 0; channel < channels.length; channel++) {
                System.arraycopy(channels[channel], 0, replacement[channel], 0,
                        Math.min(channels[channel].length, oldCount));
            }
            channels = replacement;
        }
        if (keepData && count > oldCount) {
            for (int channel = 0; channel < channels.length; channel++) {
                for (int i = oldCount; i < count; i++) channels[channel][i] = 0.0f;
            }
        } else if (!keepData && count > 0) {
            for (int channel = 0; channel < channels.length; channel++) {
                for (int i = 0; i < count; i++) channels[channel][i] = 0.0f;
            }
        }
        sampleCount = count;
    }

    public void copyTo(AudioBuffer destination, int offset, int count) {
        if (destination == null || destination.channels.length != channels.length
                || offset < 0 || count < 0 || offset + count > destination.sampleCount
                || count > sampleCount) {
            throw new IllegalArgumentException("incompatible audio buffers");
        }
        for (int channel = 0; channel < channels.length; channel++) {
            System.arraycopy(channels[channel], 0, destination.channels[channel], offset, count);
        }
    }
}
