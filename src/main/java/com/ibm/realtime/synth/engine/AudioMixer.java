/*
 * (C) Copyright IBM Corp. 2005, 2008
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ibm.realtime.synth.engine;

import com.ibm.realtime.synth.compat.AudioBuffer;

import java.util.*;

import static com.ibm.realtime.synth.utils.Debug.*;

/**
 * An object that takes an arbitrary number of input audio streams and renders
 * them to the output buffer.
 * 
 * @author florian
 */
public class AudioMixer implements AudioInput, Serviceable {

	public static boolean DEBUG_MIXER = false;

	/**
	 * Collection of currently active input streams
	 */
	private LinkedList<AudioInput> streams;

	/**
	 * Create an instance of a mixer
	 */
	public AudioMixer() {
		streams = new LinkedList<AudioInput>();
	}

	private AudioInput[] localStreams = new AudioInput[16];
	private AudioInput[] localRenderStreams = new AudioInput[16];

	/**
	 * The actual mixing function type 1 (mix into the provided buffer)
	 */
	public void read(AudioTime time, AudioBuffer buffer, int offset, int count) {
		int streamCount = copyStreamsToLocalArray();
		readStreams(localStreams, streamCount, time, buffer, offset, count);
	}

	private void readStreams(AudioInput[] snapshot, int streamCount,
			AudioTime time, AudioBuffer buffer, int offset, int count) {
		// Read the stable snapshot with an indexed loop. Avoid allocating a
		// LinkedList iterator for every 5 ms audio slice.
		boolean cleanupRequired = false;
		for (int i = 0; i < streamCount; i++) {
			AudioInput stream = snapshot[i];
			// read from this source stream, it will add to
			// the buffer
			stream.read(time, buffer, offset, count);
			if (stream.done()) cleanupRequired = true;
		}
		// Remove completed notes promptly, but don't allocate a list iterator on
		// every slice when all voices are still active.
		if (cleanupRequired) cleanUp();
	}

	/**
	 * The actual mixing function type 2 (return a mixed buffer)
	 */
	public AudioBuffer read(AudioTime time, int sampleCount, int channelCount, float sampleRate) {
		AudioBuffer returnBuffer = new AudioBuffer(channelCount, sampleCount, sampleRate);
		// Keep an independent snapshot because this API can be called separately
		// from the type-1 pull path. Reuse the supplied output buffer for every
		// voice instead of allocating one AudioBuffer per voice and slice.
		AudioInput[] streamsCopy;
		int streamCount;
		synchronized (streams) {
			streamCount = streams.size();
			streamsCopy = (AudioInput[]) streams.toArray(
					new AudioInput[streamCount]);
		}
		readStreams(streamsCopy, streamCount, time, returnBuffer, 0, sampleCount);
		return returnBuffer;
	}

	/**
	 * Cleans the list of streams from streams that are already done. Should be
	 * called from time to time.
	 */
	public void cleanUp() {
		synchronized (streams) {
			Iterator<AudioInput> it = streams.listIterator(0);
			while (it.hasNext()) {
				AudioInput ai = it.next();
				if (ai.done()) {
					it.remove();
					if (DEBUG_MIXER) {
						debug("Mixer.cleanUp: removed audio stream -- now "
								+ streams.size() + "streams.");
					}
				}
			}
		}
	}

	/**
	 * @return false -- the mixer never finishes playback
	 */
	public boolean done() {
		return false;
	}

	public void addAudioStream(AudioInput stream) {
		synchronized (streams) {
			streams.add(stream);
		}
		if (DEBUG_MIXER) {
			debug("Mixer: added " + stream + " -- now " + streams.size()
					+ "streams.");
		}
	}

	/**
	 * Remove an audio stream -- should not be used for ending a note, will
	 * cause clicks because of missing release phase. Mainly used internally,
	 * and for debugging.
	 * 
	 * @param stream the stream to remove from this mixer
	 */
	public void removeAudioStream(AudioInput stream) {
		synchronized (streams) {
			streams.remove(stream);
		}
		if (DEBUG_MIXER) {
			debug("Mixer: removed " + stream + " -- now " + streams.size()
					+ "streams.");
		}
	}

	/**
	 * Remove all audio streams. Mainly used for debugging.
	 */
	public void clear() {
		if (DEBUG_MIXER) {
			debug("Mixer: removing all "+streams.size()+" audio streams.");
		}
		synchronized (streams) {
			streams.clear();
		}
	}

	/**
	 * @return the current number of streams
	 */
	public final int getCount() {
		return streams.size();
	}

	public final List<AudioInput> getAudioStreams() {
		// TODO: optimization of the code that calls this method!
		List<AudioInput> list;
		synchronized (streams) {
			list = new ArrayList<AudioInput>(streams);
		}
		return list;
	}

	/**
	 * @return an array with the current audio streams
	 */
	public final AudioInput[] getAudioStreamsArray() {
		synchronized (streams) {
			return (AudioInput[]) streams.toArray(new AudioInput[streams.size()]);
		}
	}

	/**
	 * Fills the array with the list of audio streams. If template is null or
	 * too small to fit all elements, a new array is created and returned. If
	 * template contains more entries than audio streams, then the entry after
	 * the last valid entry is set to null.
	 * 
	 * @param template
	 * @return the array of audio streams
	 */
	public final AudioInput[] getAudioStreams(AudioInput[] template) {
		synchronized (streams) {
			if (template == null) {
				template = new AudioInput[streams.size()];
			}
			template = (AudioInput[]) streams.toArray(template);
		}
		return template;
	}

	/**
	 * Returns an array of all lines that are instance of the NoteInput class.
	 * The returned array may be larger than the actual number of contained
	 * NoteInput objects. The excess elements in the array are set to null.
	 * 
	 * @return the array of NoteInput objects
	 */
	public final NoteInput[] getNoteInputs() {
		NoteInput[] result;
		synchronized (streams) {
			int size = streams.size();
			result = new NoteInput[size];
			int i = 0;
			for (AudioInput ai : streams) {
				if (!ai.done() && (ai instanceof NoteInput)) {
					result[i++] = (NoteInput) ai;
				}
			}
		}
		return result;
	}

	/**
	 * Fills an array with all lines that are instance of the Renderable
	 * interface. If renderables does not have enough elements, a negative
	 * number is returned, which is the negative number of required elements.
	 * 
	 * @return the number of Renderable objects written to the array, or, if the
	 *         array is too small, the negative minimum size for the renderables
	 *         array
	 */
	public final int getRenderables(Renderable[] renderables) {
		synchronized (streams) {
			int size = streams.size();
			if (renderables.length < size) {
				return -size;
			}
			if (localRenderStreams.length < size) {
				localRenderStreams = new AudioInput[size + 8];
			}
			streams.toArray(localRenderStreams);
			int renderableCount = 0;
			for (int i = 0; i < size; i++) {
				AudioInput ai = localRenderStreams[i];
				if (ai instanceof Renderable && !ai.done()) {
					renderables[renderableCount++] = (Renderable) ai;
				}
			}
			return renderableCount;
		}
	}

	public final Renderable[] getRenderables() {
		synchronized (streams) {
			int size = streams.size();
			if (localRenderStreams.length < size) {
				localRenderStreams = new AudioInput[size + 8];
			}
			streams.toArray(localRenderStreams);
			Renderable[] renderables = new Renderable[size];
			int renderableCount = 0;
			for (int i = 0; i < size; i++) {
				AudioInput ai = localRenderStreams[i];
				if (ai instanceof Renderable && !ai.done()) {
					renderables[renderableCount++] = (Renderable) ai;
				}
			}
			if (renderableCount == renderables.length) {
				return renderables;
			}
			Renderable[] compact = new Renderable[renderableCount];
			System.arraycopy(renderables, 0, compact, 0, renderableCount);
			return compact;
		}
	}

	private int copyStreamsToLocalArray() {
		synchronized (streams) {
			int size = streams.size();
			if (localStreams.length < size) {
				localStreams = new AudioInput[size + 8];
			}
			streams.toArray(localStreams);
			return size;
		}
	}

	/**
	 * Remove the last <num> streams. This method is mainly for debugging
	 * purposes.
	 */
	public void removeLast(int num) {
		synchronized (streams) {
			int index = streams.size() - num;
			if (index < 0) index = 0;
			Iterator<AudioInput> it = streams.listIterator(index);
			while (it.hasNext()) {
				it.next();
				it.remove();
			}
		}
	}

	// interface Serviceable
	/**
	 * Should be called regularly to do some maintenance work.
	 */
	public void service() {
		cleanUp();
	}

}
