/*
 * @(#)WavPlayer.java	1.51 02/11/05 @(#)
 *
 * Copyright (c) 1996-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.mmedia;

import javax.microedition.media.*;
import javax.microedition.media.control.*;
import com.sun.mmedia.BasicPlayer;
import java.io.IOException;


/**
 * This class implements the wav (audio/x-wav) audio player.
 */
public class WavPlayer extends BasicPlayer implements Runnable {
    /**
     * the duration of this player
     */
    private long duration = TIME_UNKNOWN;

    /** 
     * Audio format parameters: sampleRate, channel number, sample size
     */
    private int sampleRate, channels, sampleSizeInBits;

    /**
     * byte rate and alignment
     */
    private int bytesPerSecond, blockAlign;

    /**
     * Last time when the player is stopped, how many samples have been
     * played and the media time at that point 
     */
    private long lastPos, origin;

    /**
     * waveout lock obj 
     */
    private Object waveLock = new Object();
    /**
     * play thread obj
     */
    private Thread playThread;
    /**
     * a status flag indicating whether this player is started 
     */
    private boolean started;
    /**
     * a status flag indicating whether this player has been deallocated 
     */
    private boolean interrupted;
    /**
     * a play lock obj 
     */
    private Object playLock = new Object();
    /**
     * In source stream, the start position of pcm data
     */
    private long startpt;
    /**
     * where the pcm data ends in source stream 
     */
    private long endpt = Long.MAX_VALUE;
    /**
     * the pointer to native wave out data structure. 
     */
    private int peer;

    /**
     * The buffer length to read from source stream every time.
     */
    private int bufLen;
    /**
     * the data buffer read from source stream 
     */
    private byte[] buffer;
    
    /**
     * a pause lock obj 
     */
    private Object pauseLock = new Object();
    /**
     * a flag indicating if this player could be paused 
     */
    private boolean canPause = true;

    /** 
     * Open the audio device in a particular format.
     * 
     * @param sampleRate the sampleRate of the intended format
     * @param bits how many bits per sample of the intended format
     * @param channels mono or stereo
     * @return the pointer to the wave out data structure, if succeeded.
     *         non-positive number if failed.
     */
    private native int nOpen(int sampleRate, int bits, int channels); 
    
    /**
     * Pass a data buffer to native code.
     *
     * @param peer the pointer to the native wave out data structure.
     * @param data the data buffer to be written to native wave out
     * @param offset the offset in data buffer.
     * @param len how many bytes of data to be written.
     * @return the acutal number of bytes has been written.
     */
    private native int nWrite(int peer, byte[] data, int offset, int len);

    /**
     * Utility functions for wave out
     *
     * @param peer the pointer to the native wave out data structure.
     * @param code functionality code
     * @param param parameters for a particular functionality.
     * @return status.
     */
    private native int nCommon(int peer, int code, int param);
    
    /**
     * Return the content type.
     *
     * @return the wav content type.
     */
    public String getContentType() {
	chkClosed(true);
	return "audio/x-wav";
    }

    /**
     * Parse the input, realize the player
     */
    protected void doRealize() throws MediaException {
	try {
	    readHeader();
	    startpt = getStrmLoc();
	} catch (IOException e) {
	    throw new MediaException(e.getMessage());
	}
    }
    
    
    /**
     * Get the duration of the media represented by this object.
     * The value returned is the media's duration
     * when played at the default rate.
     * If the duration can't be determined (for example, the media object is
     * presenting live * video)  <CODE>getDuration</CODE> returns 
     * <CODE>TIME_UNKNWON</CODE>.
     *
     * @return A <CODE>long</CODE> object representing the duration or 
     * TIME_UNKNWON.
     */
    public long doGetDuration() {
	return duration;
    }
    
    
    /**
     * Parse the Wave audio file header.
     *
     */
    protected void readHeader() throws IOException {
	if (readInt() != 0x46464952) // RIFF
	    throw new IOException("malformed wave data");
	
	readInt();
	
	if (readInt() != 0x45564157) // WAVE
	    throw new IOException("malformed wave data");
	
	// Only the required chunks 'fmt ' and 'data' are supported.
	// There are no restrictions upon the order of the chunks within 
	// a WAVE file, with the exception that the Format chunk must precede
	// the Data chunk.
	
	// Skip all chunks until you reach the 'fmt ' chunk
	while (readInt() != 0x20746D66) { // FMT
	    int size = readInt();
	    skipStrm(size);
	}
	
	// Handle Format chunk 'fmt ', formatSize
	int fmtsize = readInt();
	if (fmtsize < 16)
	    throw new IOException("bad fmt chunk");
	fmtsize -= 16;
	
	int encoding = readShort();
	if (encoding != 0x0001) // WF_PCM
	    throw new IOException("only supports PCM");

	channels = readShort();
	sampleRate = readInt();
	bytesPerSecond = readInt();
	blockAlign = readShort();
	sampleSizeInBits = readShort();

	// bytesPerSecond and blockAlign might not accurate in the file
	// need to calculate 
	bytesPerSecond = sampleRate * channels * sampleSizeInBits / 8;
	blockAlign = channels * sampleSizeInBits / 8;

	// skip the rest of the format chunk
	if (fmtsize > 0)
	    skipStrm(fmtsize);

	// Skip all chunks until you reach the 'data' chunk
	while (readInt() != 0x61746164) { // DATA
	    int size = readInt();
	    skipStrm(size);
	}
	
	// Handle Format chunk 'data'
	long dataSize = readInt();
	endpt = getStrmLoc() + dataSize;

	if ((channels * sampleSizeInBits / 8) == blockAlign) {
	    duration = (dataSize*1000000L)/bytesPerSecond;
	} else {
	    duration = TIME_UNKNOWN;
	}

	return;
    }


    /**
     * Get the resources ready.
     */
    protected void doPrefetch() throws MediaException {
	// Open the audio device.
	synchronized (waveLock) {
	    if (peer == 0) {
		peer = nOpen(sampleRate, sampleSizeInBits, channels);
		if (peer <= 0) {
		    throw new MediaException("can't open audio device");
		}

		bufLen = nCommon(peer, 9, 0);
		buffer = new byte[bufLen];

		if (getLevel() == -1)
		    setLevel(nCommon(peer, 8, 0));
	    }
	}

	if (isMuted()) {
	    nCommon(peer, 7, 0);
	} else {
	    nCommon(peer, 7, getLevel());
	}
    }


    /**
     * Start the playback.
     * @return the status if the player has been successfully started.
     */
    protected boolean doStart() {
	if (started)
	    return true;

	started = true;

	// Start the playback loop.
	synchronized (playLock) {
	    if (playThread == null) {
		playThread = new Thread(this);
		playThread.start();
	    } else
		playLock.notifyAll();
	}
	    
	nCommon(peer, 2, 0); // RESUME
	return true;
    }


    /**
     * Stop the playback loop.
     */
    protected void doStop() {
	if (!started)
	    return;
	
	started = false;
	synchronized (pauseLock) {
	    while (!canPause)
		try {
		    pauseLock.wait();
		} catch (Exception ex) {
		}
	    nCommon(peer, 1, 0); // PAUSE
	    pauseLock.notifyAll();
	}
    }


    /**
     * Deallocate the exclusing resource.
     */
    protected void doDeallocate() {
	// Interrupt the playback loop.

	// If the playThread had not been started, we'll need
	// to explicitly close the device.
	if (state == PREFETCHED && playThread == null) {
	    nCommon(peer, 5, 0); // CLOSE
	    return;
	}

	synchronized (playLock) {
	    interrupted = true;

	    // Wake up the run loop if it was stopped.
	    playLock.notifyAll();

	    // Wait for the playback loop to completely stop before 
	    // returning.  There's a maximum wait limit set here in 
	    // case anything goes wrong.
	    if (playThread != null) {
		try {
		    playLock.wait(5000);
		} catch (Exception e) {}
	    }
	}
    }


    /**
     * Close the player.
     */
    protected void doClose() {
	// Deallocate would have been called before this.
	// So all the resources should have been released.
    }

    /**
     * The worker method to actually set player's media time.
     *
     * @param now The new media time in microseconds.
     * @return The actual media time set in microseconds.
     * @exception MediaException Thrown if an error occurs
     * while setting the media time.
     */
    protected long doSetMediaTime(long now) throws MediaException {
	long ret = now;
	try {
	    long pp = (bytesPerSecond * now / 1000000L / blockAlign) *
		blockAlign + startpt;
	    if (getState() == STARTED)
		doStop();
	    nCommon(peer, 3, 0); // FLUSH
	    lastPos = nCommon(peer, 6, 0); // SAMPLES_PLAYED
	    ret = seekStrm(pp);
	    ret = (ret-startpt) * 1000000L / bytesPerSecond;
	    origin = ret;
	    if (getState() == STARTED)
		doStart();
	} catch (Exception e) {
	    throw new MediaException(e.getMessage());
	}

	return ret;
    }

    /**
     * Gets this player's current <i>media time</i>
     * in microseconds.
     * 
     * @return The current <i>media time</i> in microseconds.
     */
    public long doGetMediaTime() {
	long pos = 0;
	long mtime;
	synchronized (waveLock) {
	    if (sampleRate == 0)
		return 0;
	    pos = nCommon(peer, 6, 0); /* SAMPLES_PLAYED */
	}

	// Media time is in micro-seconds
	mtime = ((pos - lastPos) * 1000000L) / sampleRate + origin;

	return (mtime < 0 ? 0 : mtime);

    }

    /**
     * The worker method to actually obtain the control.
     *
     * @param type the class name of the <code>Control</code>.  
     * @return <code>Control</code> for the class or interface
     * name.
     */
    protected Control doGetControl(String type) {
	if ((getState() >= REALIZED) && 
	    type.equals("javax.microedition.media.control.VolumeControl")) {
	    return this;
	}
	return null;
    }
    

    /**
     * error string 
     */
    private String errMsg = null;
    
    /**
     * Read the data from the source and write them to the waveout in native.
     * @return the status.
     */
    private boolean doProcess() {
	int len = 0, wlen = 0;
	
	try {
	    len = readBytes(buffer, 0, bufLen);
	} catch (IOException ioe) {
	    errMsg = ioe.getMessage();
	    return false;
	}

	if (len < 1) {
	    synchronized (pauseLock) {
		canPause = false;
		try {
		    while (nCommon(peer, 4, 0) != 1) // DRAIN
			pauseLock.wait(20);
		} catch (Exception ex) {}
		canPause = true;
		pauseLock.notifyAll();
	    }

	    Thread.yield();
	    started = false;
	    sendEvent(PlayerListener.END_OF_MEDIA, new Long(getMediaTime()));
	    return true;
	}

	synchronized (pauseLock) {
	    canPause = false;
	    while ((wlen = nWrite(peer, buffer, 0, len)) == 0) {
		try {
		    pauseLock.wait(16);
		} catch (Exception ex) {}
	    }
	    canPause = true;
	    pauseLock.notifyAll();
	}

	if (wlen == -1) 
	    return false;
	
	return true;
    }
    

    /**
     * Main process loop driving the media flow.
     */
    public void run() {
	boolean statusOK = true;

	while (true) {

	    while (!interrupted && started && statusOK) {
		statusOK = doProcess();
		Thread.yield();
	    }

	    synchronized (playLock) {
		if (interrupted || !statusOK)
		    break;

		try {
		    playLock.wait();
		} catch (Exception ex) {}
	    }

	} // end of while (true)

	// Close the audio device, exit safely out of the process loop.

	nCommon(peer, 3, 0); // FLUSH
	synchronized (waveLock) {
	    nCommon(peer, 5, 0); // CLOSE
	    peer = 0;
	    interrupted = started = false;
	}
	lastPos = 0;

	synchronized (playLock) {
	    playThread = null;
	    // Notify the blocking deallocate that we are done with
	    // the process loop.
	    playLock.notifyAll();
	}

	if (!statusOK) {
	    if (stream != null) {
		try {
		    stream.close();
		} catch (IOException ex) {}
	    }
	    sendEvent(PlayerListener.ERROR, errMsg);
	}
    }





    /**
     * ====================================
     * Read calls to read from SourceStream
     * ====================================
     */
    /**
     * Read bytes from source stream.
     * @param array the byte array to hold the data
     * @param offset the offset in the byte array
     * @param num the number of bytes to be read
     * @return the actual number of bytes has been read
     */
    private int readBytes(byte[] array, int offset, int num) throws 
    IOException {
	if (num == 0) {
	    return 0;
	}

	long cpos = getStrmLoc();
	long available = endpt - cpos;

	if (available <= 0) {
	    return -1;
	}
	
	if (num > available) {
	    num = (int) available;
	}

	int rem = num;
	int read = 0;

	while (rem > 0) {
	    try {
		read = readStrm(array, offset, rem);
	    } catch (IOException e) {
		return -1;
	    }
	    if (read == -1) {	// End of stream
		if (rem == num) {
		    return -1;
		} else {
		    return num - rem;
		}
	    }
	    rem -= read;
	    offset += read;
	}
	return num;
    }

    /**
     * temporary buffer
     */
    private byte [] intArray = new byte[4];

    /**
     * Read an integer from source stream
     * @return the integer read.
     * @throws IOExeption if there is an error
     */
    private int readInt() throws IOException {
	if (readBytes(intArray, 0, 4) < 4)
	    throw new IOException("malformed wave data");
	return ((intArray[3] & 0xFF) << 24) |
	    ((intArray[2] & 0xFF) << 16) |
	    ((intArray[1] & 0xFF) << 8) |
	    (intArray[0] & 0xFF);
    }

    /**
     * Read a short from source stream
     * @return the short read
     * @throws IOException if there is an error
     */
    private short readShort() throws IOException {
	if (readBytes(intArray, 0, 2) < 2)
	    throw new IOException("malformed wave data");
	return (short) (((intArray[1] & 0xFF) << 8) |
		(intArray[0] & 0xFF));
    }


    /**
     * ==========================
     * Methods for VolumeControl.
     * ==========================
     */
    /**
     * The worker method to actually obtain the control.
     *
     * @param vol the volume level to be set.  
     * @return the actual level has been set.
     */
    protected int doSetLevel(int vol) {
	// 0 <= vol <= 100
	synchronized (waveLock) {
	    if (peer > 0) {
		nCommon(peer, 7, vol); // SETVOL
	    }
	}
	return vol;
    }
}
