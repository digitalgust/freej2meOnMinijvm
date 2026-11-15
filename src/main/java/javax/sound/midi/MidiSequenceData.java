package javax.sound.midi;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class MidiSequenceData {
    static final class Event {
        final long tick;
        final long micros;
        final int status;
        final int channel;
        final int data1;
        final int data2;

        Event(long tick, long micros, int status, int channel, int data1, int data2) {
            this.tick = tick;
            this.micros = micros;
            this.status = status;
            this.channel = channel;
            this.data1 = data1;
            this.data2 = data2;
        }

        boolean isChannelState() {
            return status == 0xb0 || status == 0xc0 || status == 0xd0 || status == 0xe0;
        }
    }

    private static final class RawEvent {
        final long tick;
        final long order;
        final int status;
        final int channel;
        final int data1;
        final int data2;

        RawEvent(long tick, long order, int status, int channel, int data1, int data2) {
            this.tick = tick;
            this.order = order;
            this.status = status;
            this.channel = channel;
            this.data1 = data1;
            this.data2 = data2;
        }
    }

    private static final class TempoChange {
        final long tick;
        final int microsPerQuarter;
        long micros;

        TempoChange(long tick, int microsPerQuarter) {
            this.tick = tick;
            this.microsPerQuarter = microsPerQuarter;
        }
    }

    private final List<Event> events;
    private final List<TempoChange> tempoChanges;
    private final int division;
    private final long tickLength;
    private final long durationMicros;
    private final boolean smpte;
    private final double smpteTicksPerSecond;

    private MidiSequenceData(List<Event> events, List<TempoChange> tempoChanges,
                             int division, long tickLength, long durationMicros,
                             boolean smpte, double smpteTicksPerSecond) {
        this.events = events;
        this.tempoChanges = tempoChanges;
        this.division = division;
        this.tickLength = tickLength;
        this.durationMicros = durationMicros;
        this.smpte = smpte;
        this.smpteTicksPerSecond = smpteTicksPerSecond;
    }

    static MidiSequenceData read(InputStream input) throws IOException {
        if (input == null) throw new NullPointerException("input");
        DataInputStream in = new DataInputStream(input);
        if (in.readInt() != 0x4d546864) throw new IOException("missing MIDI header");
        int headerLength = in.readInt();
        if (headerLength < 6 || headerLength > 1024) throw new IOException("invalid MIDI header length");
        int format = in.readUnsignedShort();
        int trackCount = in.readUnsignedShort();
        int division = in.readUnsignedShort();
        if (headerLength > 6) skipFully(in, headerLength - 6);
        if (format > 1) throw new IOException("MIDI format 2 is not supported");
        if (trackCount < 1 || trackCount > 256) throw new IOException("invalid MIDI track count");

        List<RawEvent> rawEvents = new ArrayList<RawEvent>();
        List<TempoChange> tempos = new ArrayList<TempoChange>();
        long maxTick = 0;
        long eventOrder = 0;
        for (int track = 0; track < trackCount; track++) {
            int chunkId = in.readInt();
            int chunkLength = in.readInt();
            if (chunkId != 0x4d54726b || chunkLength < 0 || chunkLength > 64 * 1024 * 1024) {
                throw new IOException("invalid MIDI track chunk");
            }
            byte[] trackBytes = new byte[chunkLength];
            in.readFully(trackBytes);
            TrackCursor cursor = new TrackCursor(trackBytes);
            long tick = 0;
            int runningStatus = 0;
            while (cursor.hasRemaining()) {
                tick += cursor.readVariableLength();
                int first = cursor.peekUnsigned();
                int status;
                if (first < 0x80) {
                    if (runningStatus == 0) throw new IOException("MIDI running status without status byte");
                    status = runningStatus;
                } else {
                    status = cursor.readUnsigned();
                    if (status < 0xf0) runningStatus = status;
                    else runningStatus = 0;
                }

                if (status == 0xff) {
                    int metaType = cursor.readUnsigned();
                    int length = cursor.readVariableLength();
                    if (length > cursor.remaining()) throw new EOFException("truncated MIDI meta event");
                    if (metaType == 0x51 && length == 3) {
                        int tempo = (cursor.readUnsigned() << 16)
                                | (cursor.readUnsigned() << 8) | cursor.readUnsigned();
                        if (tempo > 0) tempos.add(new TempoChange(tick, tempo));
                    } else {
                        cursor.skip(length);
                    }
                    if (metaType == 0x2f) break;
                    continue;
                }

                if (status == 0xf0 || status == 0xf7) {
                    cursor.skip(cursor.readVariableLength());
                    continue;
                }
                if (status >= 0xf0) {
                    int length = systemDataLength(status);
                    cursor.skip(length);
                    continue;
                }

                int command = status & 0xf0;
                int channel = status & 0x0f;
                int data1 = cursor.readDataByte();
                int data2 = (command == 0xc0 || command == 0xd0) ? 0 : cursor.readDataByte();
                rawEvents.add(new RawEvent(tick, eventOrder++, command, channel, data1, data2));
            }
            if (tick > maxTick) maxTick = tick;
        }

        if ((division & 0x8000) != 0) {
            int framesPerSecond = -(byte) (division >>> 8);
            int ticksPerFrame = division & 0xff;
            if (framesPerSecond == 0 || ticksPerFrame == 0) throw new IOException("invalid SMPTE division");
            double ticksPerSecond = (framesPerSecond == 29 ? 30000.0 / 1001.0 : framesPerSecond)
                    * ticksPerFrame;
            List<Event> events = new ArrayList<Event>(rawEvents.size());
            for (RawEvent event : rawEvents) {
                long micros = (long) ((event.tick * 1000000.0) / ticksPerSecond);
                events.add(new Event(event.tick, micros, event.status, event.channel, event.data1, event.data2));
            }
            sortEvents(events);
            long duration = (long) ((maxTick * 1000000.0) / ticksPerSecond);
            return new MidiSequenceData(events, new ArrayList<TempoChange>(), division,
                    maxTick, duration, true, ticksPerSecond);
        }

        if (division == 0) throw new IOException("invalid MIDI ticks per quarter note");
        Collections.sort(rawEvents, new Comparator<RawEvent>() {
            public int compare(RawEvent a, RawEvent b) {
                if (a.tick != b.tick) return a.tick < b.tick ? -1 : 1;
                if (a.order == b.order) return 0;
                return a.order < b.order ? -1 : 1;
            }
        });
        Collections.sort(tempos, new Comparator<TempoChange>() {
            public int compare(TempoChange a, TempoChange b) {
                if (a.tick == b.tick) return 0;
                return a.tick < b.tick ? -1 : 1;
            }
        });
        if (tempos.isEmpty() || tempos.get(0).tick != 0) tempos.add(0, new TempoChange(0, 500000));

        long currentTick = 0;
        long currentMicros = 0;
        int tempo = 500000;
        int tempoIndex = 0;
        for (TempoChange change : tempos) {
            long delta = change.tick - currentTick;
            if (delta > 0) currentMicros += (long) ((delta * (double) tempo) / division);
            currentTick = change.tick;
            change.micros = currentMicros;
            tempo = change.microsPerQuarter;
        }

        List<Event> events = new ArrayList<Event>(rawEvents.size());
        currentTick = 0;
        currentMicros = 0;
        tempo = 500000;
        for (RawEvent event : rawEvents) {
            while (tempoIndex < tempos.size() && tempos.get(tempoIndex).tick <= event.tick) {
                TempoChange change = tempos.get(tempoIndex++);
                if (change.tick > currentTick) {
                    currentMicros += (long) (((change.tick - currentTick) * (double) tempo) / division);
                    currentTick = change.tick;
                }
                currentMicros = change.micros;
                tempo = change.microsPerQuarter;
            }
            if (event.tick > currentTick) {
                currentMicros += (long) (((event.tick - currentTick) * (double) tempo) / division);
                currentTick = event.tick;
            }
            events.add(new Event(event.tick, currentMicros, event.status,
                    event.channel, event.data1, event.data2));
        }

        long durationMicros = microsAtTick(maxTick, tempos, division);
        return new MidiSequenceData(events, tempos, division, maxTick,
                durationMicros, false, 0.0);
    }

    long getTickLength() { return tickLength; }
    long getDurationMicros() { return durationMicros; }
    List<Event> getEvents() { return events; }

    long microsAtTick(long tick) {
        if (tick <= 0) return 0;
        if (smpte) return (long) ((tick * 1000000.0) / smpteTicksPerSecond);
        return microsAtTick(tick, tempoChanges, division);
    }

    long tickAtMicros(long micros) {
        if (micros <= 0) return 0;
        if (smpte) return (long) ((micros * smpteTicksPerSecond) / 1000000.0);
        long tick = 0;
        long time = 0;
        int tempo = 500000;
        for (TempoChange change : tempoChanges) {
            if (change.micros > micros) break;
            tick = change.tick;
            time = change.micros;
            tempo = change.microsPerQuarter;
        }
        return tick + (long) (((micros - time) * (double) division) / tempo);
    }

    private static long microsAtTick(long tick, List<TempoChange> tempos, int division) {
        long previousTick = 0;
        long micros = 0;
        int tempo = 500000;
        for (TempoChange change : tempos) {
            if (change.tick > tick) break;
            if (change.tick > previousTick) {
                micros += (long) (((change.tick - previousTick) * (double) tempo) / division);
                previousTick = change.tick;
            }
            tempo = change.microsPerQuarter;
        }
        if (tick > previousTick) micros += (long) (((tick - previousTick) * (double) tempo) / division);
        return micros;
    }

    private static void sortEvents(List<Event> events) {
        Collections.sort(events, new Comparator<Event>() {
            public int compare(Event a, Event b) {
                if (a.tick == b.tick) return 0;
                return a.tick < b.tick ? -1 : 1;
            }
        });
    }

    private static int systemDataLength(int status) {
        if (status == 0xf1 || status == 0xf3) return 1;
        if (status == 0xf2) return 2;
        return 0;
    }

    private static void skipFully(DataInputStream input, int count) throws IOException {
        int remaining = count;
        while (remaining > 0) {
            int skipped = input.skipBytes(remaining);
            if (skipped <= 0) {
                if (input.read() < 0) throw new EOFException("truncated MIDI header");
                skipped = 1;
            }
            remaining -= skipped;
        }
    }

    private static final class TrackCursor {
        private final byte[] data;
        private int position;

        TrackCursor(byte[] data) { this.data = data; }
        boolean hasRemaining() { return position < data.length; }
        int remaining() { return data.length - position; }
        int peekUnsigned() throws EOFException {
            if (!hasRemaining()) throw new EOFException("truncated MIDI track");
            return data[position] & 255;
        }
        int readUnsigned() throws EOFException {
            int value = peekUnsigned();
            position++;
            return value;
        }
        int readDataByte() throws IOException {
            int value = readUnsigned();
            if ((value & 0x80) != 0) throw new IOException("invalid MIDI data byte");
            return value;
        }
        int readVariableLength() throws IOException {
            int value = 0;
            for (int i = 0; i < 4; i++) {
                int next = readUnsigned();
                value = (value << 7) | (next & 0x7f);
                if ((next & 0x80) == 0) return value;
            }
            throw new IOException("MIDI variable length value is too long");
        }
        void skip(int count) throws IOException {
            if (count < 0 || count > remaining()) throw new EOFException("truncated MIDI event");
            position += count;
        }
    }
}
