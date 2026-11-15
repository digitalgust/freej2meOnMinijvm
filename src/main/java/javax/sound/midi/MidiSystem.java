package javax.sound.midi;

import com.ibm.realtime.synth.engine.AudioPullThread;
import com.ibm.realtime.synth.engine.MidiEvent;
import com.ibm.realtime.synth.engine.Synthesizer;
import com.ibm.realtime.synth.minijvm.MiniJvmAudioSink;
import com.ibm.realtime.synth.soundfont2.Parser;
import com.ibm.realtime.synth.soundfont2.SoundFontSoundbank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MidiSystem {
    private static final String SOUNDFONT_PROPERTY = "freej2me.midi.soundfont";
    private static final String MASTER_VOLUME_PROPERTY = "freej2me.midi.masterVolume";
    private static final double DEFAULT_MASTER_VOLUME = 1.5;
//    private static final String BUNDLED_SOUNDFONT = "/midi/FM_GM_SoundFont_v0_2_1_mini.sf2";
    private static final String BUNDLED_SOUNDFONT = "/midi/FM_GM_SoundFont_v0_4_1_Balanced.sf2";
//    private static final String BUNDLED_SOUNDFONT = "/midi/TimGM6mb.sf2";
    private static SoundFontSoundbank cachedSoundbank;
    private static final Object ACTIVE_SEQUENCERS_LOCK = new Object();
    private static final List<SequencerStub> activeSequencers = new ArrayList<SequencerStub>();
    private static volatile boolean platformShutdown;

    /** Called by the emulator before a new MIDlet is launched. */
    public static void beginApplication() {
        synchronized (ACTIVE_SEQUENCERS_LOCK) {
            platformShutdown = false;
        }
    }

    /** Stops all MIDI devices owned by this emulator instance before it closes. */
    public static void shutdownAll() {
        SequencerStub[] snapshot;
        synchronized (ACTIVE_SEQUENCERS_LOCK) {
            platformShutdown = true;
            snapshot = activeSequencers.toArray(new SequencerStub[activeSequencers.size()]);
        }
        for (int i = 0; i < snapshot.length; i++) {
            try {
                snapshot[i].shutdownFromPlatform();
            } catch (Throwable error) {
                System.err.println("Unable to stop a MIDI sequencer during app shutdown: " + error);
            }
        }
    }

    /** Stops only the MIDI sequencers owned by the current MIDlet thread group. */
    public static void shutdownCurrentMidlet() {
        ThreadGroup ownerThreadGroup = Thread.currentThread().getThreadGroup();
        if (ownerThreadGroup == null) return;
        SequencerStub[] snapshot;
        synchronized (ACTIVE_SEQUENCERS_LOCK) {
            snapshot = activeSequencers.toArray(new SequencerStub[activeSequencers.size()]);
        }
        for (int i = 0; i < snapshot.length; i++) {
            if (snapshot[i].ownerThreadGroup != ownerThreadGroup) continue;
            try {
                snapshot[i].shutdownFromPlatform();
            } catch (Throwable error) {
                System.err.println("Unable to stop a MIDlet MIDI sequencer: " + error);
            }
        }
    }

    private static boolean registerActive(SequencerStub sequencer) {
        synchronized (ACTIVE_SEQUENCERS_LOCK) {
            if (platformShutdown || sequencer.platformClosed) return false;
            if (!activeSequencers.contains(sequencer)) activeSequencers.add(sequencer);
            return true;
        }
    }

    private static void unregisterActive(SequencerStub sequencer) {
        synchronized (ACTIVE_SEQUENCERS_LOCK) {
            activeSequencers.remove(sequencer);
        }
    }

    public static Sequencer getSequencer() throws MidiUnavailableException {
        return new SequencerStub();
    }

    private static synchronized SoundFontSoundbank getSoundbank()
            throws IOException, Parser.SoundFont2ParserException {
        if (cachedSoundbank != null) return cachedSoundbank;

        String path = System.getProperty(SOUNDFONT_PROPERTY);
        if (path != null && path.trim().length() > 0) {
            File file = new File(path.trim());
            if (file.isFile()) {
                cachedSoundbank = new SoundFontSoundbank(file);
                return cachedSoundbank;
            }
            System.err.println("MIDI SoundFont path does not exist: " + file.getAbsolutePath());
        }

        InputStream resource = MidiSystem.class.getResourceAsStream(BUNDLED_SOUNDFONT);
        if (resource == null) throw new IOException("bundled GM SoundFont is missing: " + BUNDLED_SOUNDFONT);
        cachedSoundbank = new SoundFontSoundbank(resource);
        return cachedSoundbank;
    }

    static class SequencerStub implements Sequencer {
        private final ThreadGroup ownerThreadGroup = Thread.currentThread().getThreadGroup();
        private MidiSequenceData sequence;
        private SoundFontSoundbank soundbank;
        private Synthesizer synthesizer;
        private MiniJvmAudioSink sink;
        private AudioPullThread audioPullThread;
        private Thread playbackWorker;
        private volatile boolean running;
        private volatile boolean opened;
        private volatile boolean workerStop;
        private volatile boolean platformClosed;
        private long playbackGeneration;
        private int loopCount;
        private long tickPosition;
        private long startTick;
        private long startMicros;
        private long firstIterationMicros;

        public void open() {
            opened = true;
        }

        public void start() {
            synchronized (this) {
                if (running || platformShutdown || platformClosed) return;
                if (sequence == null) throw new IllegalStateException("MIDI sequence is not set");
                if (!registerActive(this)) return;
                try {
                    if (soundbank == null) soundbank = getSoundbank();
                    startTick = Math.max(0, Math.min(tickPosition, sequence.getTickLength()));
                    startMicros = sequence.microsAtTick(startTick);
                    firstIterationMicros = Math.max(0, sequence.getDurationMicros() - startMicros);

                    synthesizer = new Synthesizer(soundbank);
                    synthesizer.getParams().setMasterVolume(getConfiguredMasterVolume());
                    synthesizer.setRenderThreadCount(0);
                    synthesizer.setFixedDelayNanos(0);
                    synthesizer.start();

                    sink = new MiniJvmAudioSink();
                    audioPullThread = new AudioPullThread(synthesizer.getMixer(), sink);
                    audioPullThread.setSliceTimeMillis(5.0);
                    audioPullThread.addListener(synthesizer);
                    synthesizer.setMasterClock(audioPullThread);

                    running = true;
                    workerStop = false;
                    final long generation = ++playbackGeneration;
                    final MiniJvmAudioSink playbackSink = sink;
                    final MidiSequenceData playbackSequence = sequence;
                    final Synthesizer playbackSynth = synthesizer;
                    enqueueStartState(startTick);
                    enqueueIteration(generation, playbackSynth, playbackSequence, 0, startTick, startMicros);
                    audioPullThread.start();
                    sink.startDevice();

                    playbackWorker = new Thread(new Runnable() {
                        public void run() { runPlaybackWorker(generation, playbackSink, playbackSequence, playbackSynth); }
                    }, "FreeJ2ME MIDI playback");
                    playbackWorker.setDaemon(true);
                    playbackWorker.start();
                } catch (Throwable error) {
                    System.err.println("MIDI synthesizer could not start: " + error);
                    releasePlayback(false);
                }
            }
        }

        public void stop() {
            synchronized (this) {
                if (running) tickPosition = currentTickPosition();
            }
            releasePlayback(false);
        }

        public void close() {
            stop();
            opened = false;
        }

        public boolean isRunning() {
            if (!running) return false;
            long elapsedMicros = getElapsedMicros();
            long totalMicros = firstIterationMicros;
            if (loopCount < 0) return true;
            totalMicros += sequence.getDurationMicros() * (long) loopCount;
            return elapsedMicros < totalMicros;
        }

        public void setLoopCount(int count) {
            loopCount = count;
        }

        public void setSequence(Sequence sequence) {
            throw new IllegalArgumentException("MIDI Sequence objects are not supported by this FreeJ2ME player; pass an SMF stream");
        }

        public void setSequence(InputStream stream) throws IOException {
            stop();
            sequence = MidiSequenceData.read(stream);
            tickPosition = 0;
        }

        public long getTickPosition() {
            synchronized (this) {
                if (!running || sequence == null) return tickPosition;
                tickPosition = currentTickPosition();
                return tickPosition;
            }
        }

        public long getTickLength() {
            return sequence == null ? 0 : sequence.getTickLength();
        }

        public void setTickPosition(long tick) {
            boolean restart;
            long newPosition;
            synchronized (this) {
                restart = running;
                if (sequence == null) {
                    tickPosition = Math.max(0, tick);
                    return;
                }
                newPosition = Math.max(0, Math.min(tick, sequence.getTickLength()));
                tickPosition = newPosition;
            }
            if (restart) {
                releasePlayback(false);
                synchronized (this) { tickPosition = newPosition; }
                start();
            }
        }

        private void enqueueStartState(long fromTick) {
            if (fromTick <= 0) return;
            for (MidiSequenceData.Event event : sequence.getEvents()) {
                if (event.tick >= fromTick) break;
                if (event.isChannelState()) {
                    synthesizer.midiInReceived(new MidiEvent(null, 0, event.channel,
                            event.status, event.data1, event.data2));
                }
            }
        }

        private synchronized void enqueueIteration(long generation, Synthesizer currentSynth,
                                                   MidiSequenceData currentSequence,
                                                   long baseMicros, long fromTick, long fromMicros) {
            if (currentSynth == null || currentSequence == null || !isCurrentPlayback(generation)) return;
            long baseNanos = baseMicros * 1000L;
            int order = 0;
            for (MidiSequenceData.Event event : currentSequence.getEvents()) {
                if (!isCurrentPlayback(generation)) return;
                if (event.tick < fromTick) continue;
                long relativeMicros = event.micros - fromMicros;
                if (relativeMicros < 0) continue;
                long time = baseNanos + relativeMicros * 1000L + order++;
                currentSynth.midiInReceived(new MidiEvent(null, time, event.channel,
                        event.status, event.data1, event.data2));
            }
        }

        private void runPlaybackWorker(long generation, MiniJvmAudioSink currentSink,
                                       MidiSequenceData currentSequence, Synthesizer currentSynth) {
            long iterationEndMicros = firstIterationMicros;
            int additionalLoopsScheduled = 0;
            while (isCurrentPlayback(generation)) {
                boolean canLoop = loopCount < 0 || additionalLoopsScheduled < loopCount;
                long wakeMicros = canLoop
                        ? Math.max(0, iterationEndMicros - Math.min(150000L, currentSequence.getDurationMicros() / 3))
                        : iterationEndMicros + 250000L;
                if (!waitForAudioTime(wakeMicros, generation, currentSink)) return;
                if (!isCurrentPlayback(generation)) return;
                if (!canLoop) {
                    synchronized (this) {
                        if (playbackGeneration != generation) return;
                        tickPosition = currentSequence.getTickLength();
                    }
                    releasePlayback(true, generation);
                    return;
                }
                enqueueIteration(generation, currentSynth, currentSequence, iterationEndMicros, 0, 0);
                iterationEndMicros += currentSequence.getDurationMicros();
                additionalLoopsScheduled++;
            }
        }

        private boolean waitForAudioTime(long targetMicros, long generation, MiniJvmAudioSink currentSink) {
            while (isCurrentPlayback(generation)) {
                if (currentSink == null) return false;
                if (currentSink.getAudioTime().getNanoTime() >= targetMicros * 1000L) return true;
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException interrupted) {
                    if (!isCurrentPlayback(generation)) return false;
                }
            }
            return false;
        }

        private boolean isCurrentPlayback(long generation) {
            return running && !workerStop && playbackGeneration == generation;
        }

        private long getElapsedMicros() {
            MiniJvmAudioSink currentSink = sink;
            return currentSink == null ? 0 : currentSink.getAudioTime().getNanoTime() / 1000L;
        }

        private long currentTickPosition() {
            if (sequence == null) return tickPosition;
            long elapsed = getElapsedMicros();
            if (loopCount < 0) {
                long duration = Math.max(1, sequence.getDurationMicros());
                return sequence.tickAtMicros(elapsed < firstIterationMicros
                        ? startMicros + elapsed : (elapsed - firstIterationMicros) % duration);
            }
            if (elapsed >= firstIterationMicros) {
                long repeatedElapsed = elapsed - firstIterationMicros;
                if (loopCount > 0 && sequence.getDurationMicros() > 0) {
                    return sequence.tickAtMicros(repeatedElapsed % sequence.getDurationMicros());
                }
                return sequence.getTickLength();
            }
            return sequence.tickAtMicros(startMicros + elapsed);
        }

        private void releasePlayback(boolean completed) {
            releasePlayback(completed, -1L);
        }

        private void releasePlayback(boolean completed, long expectedGeneration) {
            MiniJvmAudioSink oldSink;
            AudioPullThread oldPull;
            Synthesizer oldSynth;
            Thread oldWorker;
            synchronized (this) {
                if (expectedGeneration >= 0 && playbackGeneration != expectedGeneration) return;
                if (!running && sink == null && audioPullThread == null && synthesizer == null) {
                    unregisterActive(this);
                    return;
                }
                running = false;
                workerStop = true;
                playbackGeneration++;
                oldWorker = playbackWorker;
                oldSink = sink;
                oldPull = audioPullThread;
                oldSynth = synthesizer;
                playbackWorker = null;
                sink = null;
                audioPullThread = null;
                synthesizer = null;
                if (completed && sequence != null) tickPosition = sequence.getTickLength();
            }
            unregisterActive(this);
            if (oldWorker != null && oldWorker != Thread.currentThread()) {
                oldWorker.interrupt();
                joinBriefly(oldWorker, 150L);
            }
            if (oldPull != null) {
                try { oldPull.stop(); } catch (Throwable error) { System.err.println("Unable to stop MIDI mixing thread: " + error); }
            }
            if (oldSink != null) {
                try { oldSink.close(); } catch (Throwable error) { System.err.println("Unable to close MIDI audio device: " + error); }
            }
            if (oldSynth != null) {
                if (oldPull == null || !oldPull.isStarted()) {
                    try { oldSynth.close(); } catch (Throwable error) { System.err.println("Unable to close MIDI synthesizer: " + error); }
                } else {
                    System.err.println("MIDI mixing thread did not stop; keeping its synthesizer alive to avoid closing it while in use.");
                }
            }
        }

        private void shutdownFromPlatform() {
            platformClosed = true;
            releasePlayback(false);
        }

        private void joinBriefly(Thread thread, long timeoutMillis) {
            if (thread == null || thread == Thread.currentThread()) return;
            try {
                thread.join(timeoutMillis);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static double getConfiguredMasterVolume() {
        String configured = System.getProperty(MASTER_VOLUME_PROPERTY);
        if (configured == null || configured.trim().length() == 0) {
            return DEFAULT_MASTER_VOLUME;
        }
        try {
            double volume = Double.parseDouble(configured.trim());
            if (volume >= 0.0 && volume <= 4.0) return volume;
        } catch (Throwable ignored) {
        }
        System.err.println("Invalid MIDI master volume '" + configured
                + "'; using " + DEFAULT_MASTER_VOLUME + ". Set "
                + MASTER_VOLUME_PROPERTY + " to a value from 0 to 4.");
        return DEFAULT_MASTER_VOLUME;
    }
}
