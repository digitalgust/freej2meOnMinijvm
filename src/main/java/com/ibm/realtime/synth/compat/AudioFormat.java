/*
 * MiniJVM compatibility format descriptor for the android-synth engine.
 */
package com.ibm.realtime.synth.compat;

public final class AudioFormat {
    private final float sampleRate;
    private final int sampleSizeInBits;
    private final int channels;
    private final int frameSize;
    private final boolean signed;
    private final boolean bigEndian;

    public AudioFormat(float sampleRate, int sampleSizeInBits, int channels,
                       boolean signed, boolean bigEndian) {
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = ((sampleSizeInBits + 7) / 8) * channels;
        this.signed = signed;
        this.bigEndian = bigEndian;
    }

    public float getSampleRate() { return sampleRate; }
    public int getSampleSizeInBits() { return sampleSizeInBits; }
    public int getChannels() { return channels; }
    public int getFrameSize() { return frameSize; }
    public boolean isSigned() { return signed; }
    public boolean isBigEndian() { return bigEndian; }
}
