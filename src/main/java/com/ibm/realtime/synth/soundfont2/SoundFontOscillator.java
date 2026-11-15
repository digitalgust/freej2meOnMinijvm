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
package com.ibm.realtime.synth.soundfont2;

import com.ibm.realtime.synth.engine.*;

import com.ibm.realtime.synth.compat.AudioBuffer;
import com.ibm.realtime.synth.compat.ConversionTool;

public class SoundFontOscillator extends Oscillator {

	public SoundFontOscillator(SoundFontSample sample,
			SoundFontSampleData sampleData) {
		setNativeAudioFormat(sample.getSampleRate(), 16, 2, 1, true, false);
		this.nativeSamples = sampleData.getData();
		this.nativeSamplesStartPos = sample.getStart();
		this.nativeSamplesEndPos = sample.getEnd();

		this.loopStart = sample.getStartLoop();
		this.loopEnd = sample.getEndLoop();
	}

	protected void convertOneBlock(AudioBuffer buffer, int offset, int count) {
		assert(buffer.getChannelCount()==1);
		// optimization for the most common soundfont format: 16-bit signed, little endian
		if (nativeFormatCode == ConversionTool.CT_16SL) {
			ConversionTool.byte2floatLSRC_16SL(nativeSamples,0, nativeChannels,
					nativePos, nativePosDelta, buffer.getChannel(0),
					offset, count);
		} else {
			ConversionTool.byte2floatGenericLSRC(nativeSamples,
					0, nativeSampleSize, nativePos,
					nativePosDelta, buffer.getChannel(0), offset,
					count, nativeFormatCode);
		}
	}

	/**
	 * @param loopEnd The loopEnd to add.
	 */
	void addLoopEnd(double loopEnd) {
		this.loopEnd += loopEnd;
	}

	/**
	 * @param loopMode The loopMode to set, one of the Oscillator.LOOPMODE_*
	 *            constants.
	 */
	void setLoopMode(int loopMode) {
		this.loopMode = loopMode;
	}

	/**
	 * @param loopStart The loopStart to set.
	 */
	void addLoopStart(double loopStart) {
		this.loopStart += loopStart;
	}

	/**
	 * @param nativeSamplesEndPos The nativeSamplesEndPos to set.
	 */
	void addNativeSamplesEndPos(int nativeSamplesEndPos) {
		this.nativeSamplesEndPos += nativeSamplesEndPos;
	}

	/**
	 * @param nativeSamplesStartPos The nativeSamplesStartPos to set.
	 */
	void addNativeSamplesStartPos(int nativeSamplesStartPos) {
		this.nativeSamplesStartPos += nativeSamplesStartPos;
	}

}
