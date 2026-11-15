/*
 * SoundFont sample conversion helpers for the android-synth engine.
 */
package com.ibm.realtime.synth.compat;

public final class ConversionTool {
    public static final int CT_16SL = 1;
    private static final int CT_16SS = 2;
    private static final int CT_8S = 3;
    private static final int CT_8U = 4;
    private static final int CT_16UL = 5;
    private static final int CT_16US = 6;

    private ConversionTool() { }

    public static int getFormatType(AudioFormat format) {
        return getFormatType(format.getSampleSizeInBits(), (format.getSampleSizeInBits() + 7) / 8,
                format.isSigned(), format.isBigEndian());
    }

    public static int getFormatType(int bitsPerSample, int bytesPerSample,
                                    boolean signed, boolean bigEndian) {
        if (bitsPerSample == 16 && bytesPerSample == 2) {
            if (signed) return bigEndian ? CT_16SS : CT_16SL;
            return bigEndian ? CT_16US : CT_16UL;
        }
        if (bitsPerSample == 8 && bytesPerSample == 1) {
            return signed ? CT_8S : CT_8U;
        }
        return 0;
    }

    public static void byte2floatLSRC_16SL(byte[] source, int sourceOffset,
                                           int channelCount, double sourcePosition,
                                           double sourceDelta, float[] destination,
                                           int destinationOffset, int count) {
        for (int i = 0; i < count; i++) {
            int frame = (int) sourcePosition;
            double fraction = sourcePosition - frame;
            int firstOffset = sourceOffset + frame * channelCount * 2;
            int secondOffset = firstOffset + channelCount * 2;
            float first = readSigned16LE(source, firstOffset);
            float second = readSigned16LE(source, secondOffset);
            destination[destinationOffset + i] = (first + (second - first) * (float) fraction) / 32768.0f;
            sourcePosition += sourceDelta;
        }
    }

    public static void byte2floatGenericLSRC(byte[] source, int sourceOffset,
                                              int sampleSize, double sourcePosition,
                                              double sourceDelta, float[] destination,
                                              int destinationOffset, int count,
                                              int formatCode) {
        if (formatCode == CT_16SL) {
            byte2floatLSRC_16SL(source, sourceOffset, sampleSize / 2, sourcePosition,
                    sourceDelta, destination, destinationOffset, count);
            return;
        }
        int sampleBytes = sampleSize;
        for (int i = 0; i < count; i++) {
            int frame = (int) sourcePosition;
            double fraction = sourcePosition - frame;
            float first = readSample(source, sourceOffset + frame * sampleBytes, formatCode);
            float second = readSample(source, sourceOffset + (frame + 1) * sampleBytes, formatCode);
            destination[destinationOffset + i] = first + (second - first) * (float) fraction;
            sourcePosition += sourceDelta;
        }
    }

    private static float readSample(byte[] source, int offset, int formatCode) {
        if (formatCode == CT_8S) return source[offset] / 128.0f;
        if (formatCode == CT_8U) return ((source[offset] & 255) - 128) / 128.0f;
        if (formatCode == CT_16SS) {
            return (short) (((source[offset] & 255) << 8) | (source[offset + 1] & 255)) / 32768.0f;
        }
        if (formatCode == CT_16UL) {
            int value = (source[offset] & 255) | ((source[offset + 1] & 255) << 8);
            return (value - 32768) / 32768.0f;
        }
        if (formatCode == CT_16US) {
            int value = ((source[offset] & 255) << 8) | (source[offset + 1] & 255);
            return (value - 32768) / 32768.0f;
        }
        return 0.0f;
    }

    private static short readSigned16LE(byte[] source, int offset) {
        return (short) ((source[offset] & 255) | ((source[offset + 1] & 255) << 8));
    }
}
