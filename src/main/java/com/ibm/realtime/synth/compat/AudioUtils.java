/*
 * Small audio math helpers used by the android-synth engine.
 */
package com.ibm.realtime.synth.compat;

public final class AudioUtils {
    private AudioUtils() { }

    public static long samples2nanos(long samples, double sampleRate) {
        return sampleRate <= 0 ? 0 : (long) ((samples * 1000000000.0) / sampleRate);
    }

    public static long nanos2samples(long nanos, double sampleRate) {
        return (long) ((nanos * sampleRate) / 1000000000.0);
    }

    public static long samples2micros(long samples, double sampleRate) {
        return sampleRate <= 0 ? 0 : (long) ((samples * 1000000.0) / sampleRate);
    }

    public static long micros2samples(long micros, double sampleRate) {
        return (long) ((micros * sampleRate) / 1000000.0);
    }

    public static long millis2samples(double millis, double sampleRate) {
        return (long) ((millis * sampleRate) / 1000.0);
    }

    public static long seconds2samples(double seconds, double sampleRate) {
        return (long) (seconds * sampleRate);
    }

    public static double samples2seconds(long samples, double sampleRate) {
        return sampleRate <= 0 ? 0 : samples / sampleRate;
    }

    public static boolean isAlmost(double first, double second) {
        return Math.abs(first - second) < 0.000001;
    }

    public static double decibel2linear(double decibels) {
        return Math.pow(10.0, decibels / 20.0);
    }

    public static double getSamplerateFactorFromRelativeNote(double semitones) {
        return Math.pow(2.0, semitones / 12.0);
    }
}
