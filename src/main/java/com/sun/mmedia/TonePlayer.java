/*
 * @(#)TonePlayer.java	1.47 02/08/16 @(#)
 *
 * Copyright (c) 1996-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.mmedia;

import javax.microedition.media.*;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import javax.microedition.media.control.*;
import java.util.*;

/**
 * The tone player to play tone sequences.
 */

public class TonePlayer extends BasicPlayer implements ToneControl {
    /**
     * Initialize the audio device for this tone seq
     * @param pID the global player ID of this tone player
     * @return the pointer to the tone seq data structure.
     */
    native private int toneInit(int pID);

    /**
     * Pass the tone seq to the native code.
     *
     * @param ad the pointer to the tone seq data structure
     * @param toneseq the tone seq to be set.
     *
     */
    native private void toneSetSeq(int ad, int[] toneseq);
    
    /**
     * Utility native functions.
     *
     * @param ad the pointer to the tone seq data structure
     * @param code function code.
     * @param param the parameter for a particular function
     * @return succeed or not.
     */
    native private int toneCommon(int ad, int code, int param);

    /**
     * the tone seq of this tone player 
     */
    private int[] toneSeq = new int[0];
    /**
     * the pointer to the native tone seq data structure.
     */
    private int ad;
    /**
     * the duration of this tone player in milliseconds 
     */
    private int curTime = -1;
    /**
     * a flag indicating whether the tone seq has changed 
     */
    private boolean seqChanged = true;

    /**
     * Return the content type.
     *
     * @return the wav content type.
     */
    public String getContentType() {
	chkClosed(true);
	return "audio/x-tone-seq";
    }

    /**
     * the worker method to realize the player
     */
    protected void doRealize() throws MediaException {
	curTime = 0;
	// if no source stream, player is created from TON_DEVICE_LOCATOR
	// simply return it.
	if (stream == null)
	    return;
	
	// read the whole sequence from the source stream
	int chunksize = 128;
	byte[] tmpseqs = new byte[chunksize];
	byte[] seqs = null;
	// make use of BAOS, since it takes care of growing buffer 
	ByteArrayOutputStream baos = new ByteArrayOutputStream(chunksize);
	
	try {
	    int read;
	    
	    while ((read = stream.read(tmpseqs, 0, chunksize)) != -1) {
		baos.write(tmpseqs, 0, read);
	    }
	    
	    seqs = baos.toByteArray();
	    baos.close();
	    tmpseqs = null;
	    System.gc();
	    
	} catch (IOException ex) {
	    throw new MediaException("fail to read from source");
	}
	this.setSequence(seqs);
    }

    /**
     * the worker method to prefetch the player.
     */
    protected void doPrefetch() throws MediaException {
	if (ad <= 0) {
	    ad = toneInit(pID);
	    if (ad <= 0)
		throw new MediaException("prefetch failed");
	    
	    if (getLevel() == -1) {
		setLevel(100);
	    } else { 
		toneCommon(ad, 7, getLevel());
	    }

	    if (isMuted()) {
		toneCommon(ad, 7, 0);  // SET_VOLUME
	    }
	}
    }
    
    /**
     * Obtain the duration of this player.
     *
     * @return the duration
     */
    public long doGetDuration() {
	if (curTime >= 0)
	    return (long)(curTime * 1000L);
	else 
	    return TIME_UNKNOWN;
    }
    
    /**
     * The worker method to start the player.
     *
     * @return a flag whether the player has been successfully started
     */
    protected boolean doStart() {
	if (seqChanged) {
	    toneSetSeq(ad, toneSeq);
	    seqChanged = false;
	}

	toneCommon(ad, 2, 0); // START
	return true;
    }
    
    /**
     * The worker method to stop the player
     */
    protected void doStop() {
	toneCommon(ad, 1, 0); // PAUSE
    }
    
    /** 
     * The worker method to deallocate the player
     */
    protected void doDeallocate() {
	toneCommon(ad, 1, 0); // PAUSE
	toneCommon(ad, 5, 0); // CLOSE 
	ad = 0;
    }
    
    /**
     * The worker method to close the player
     */
    protected void doClose() {
    }
    
    /**
     * The worker method to actually set player's media time.
     *
     * @param now The new media time in microseconds.
     * @return The actual media time set in microseconds.
     * @throws  MediaException if an error occurs
     * while setting the media time.
     */
    protected long doSetMediaTime(long now) throws MediaException {
	int milli_now = (int)(now /1000);

	if (getState() == STARTED)
	    doStop();
	milli_now = toneCommon(ad, 16, milli_now); // SET_CUR_TIME
	if (getState() == STARTED)
	    doStart();

	return (milli_now * 1000L);
    }
    
    /**
     * Gets this player's current <i>media time</i>
     * in microseconds.
     * 
     * @return The current <i>media time</i> in microseconds.
     */
    public long doGetMediaTime() {
	return (toneCommon(ad, 15, 0)*1000L); // GET_CUR_TIME
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
            (type.equals("javax.microedition.media.control.ToneControl") || 
             type.equals("javax.microedition.media.control.VolumeControl")))
	    return this;
	return null;
    }
    
    /** 
     * Sets the tone sequence.<p>
     * 
     * @param sequence The sequence to set.
     * @exception IllegalArgumentException Thrown if the sequence is 
     * <code>null</code> or invalid.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * that this control belongs to is in the <i>PREFETCHED</i> or
     * <i>STARTED</i> state.
     *
     *	bad tone seq error code:
     * 1: Mismatched BLOCK_START and BLOCK_END
     * 2: Try to play a block before it is defined
     * 3: Nested block definition
     * 4: Bad parameters, either note is out of range, or note's duration is
     *    non-positive.
     * 5: Bad tempo setting	  
     * 6: bad version number
     * 7: negative block number
     * 8: bad resolution setting
     * 9: bad multiplier setting
     * 10: bad volume setting
     * 11: REPEAT is not followed by tone event
     * 12: can't define VERSION, TEMPO and RESOLUTIOn in the middle of the 
     *     sequence
     */
    public void setSequence(byte[] sequence) {
	if (this.getState() >= Player.PREFETCHED)
	    throw new 
		IllegalStateException("cannot set seq after prefetched");
	int tempo = 120;
	int resolution = 64;
	int frac = 1;
	int p = 0; 
	try {
	    Stack sp = new Stack();
	    Hashtable blens = new Hashtable();
	    Hashtable pblks = new Hashtable();
	    boolean inblk = false;
	    int found = 0, thisblen = 0, len;
	    byte note;
	    int i;
	    int startseq = 2;
	    len = 0;
	    int tmp = 0;

	    if (sequence[0] != VERSION || sequence[1] != 1) {
		reportErr(6);
	    }
	    if (sequence[startseq] == TEMPO) {
		if (sequence[startseq+1] < 5) {
		    reportErr(5);
		}
		tempo = (sequence[startseq+1] & 0x7f) << 2;
		startseq += 2;
	    }

	    if (sequence[startseq] == RESOLUTION) {
		if (sequence[startseq+1] <= 0)
		    reportErr(8);
		resolution = sequence[startseq+1];
		startseq += 2;
	    }

	    frac = tempo * resolution;
	    for (i = startseq; i < sequence.length; i += 2) {
		note = sequence[i];
		if (note < REPEAT || 
		    ((note >= 0 || note == SILENCE) && sequence[i+1] <= 0)) {
		    reportErr(4);
		}

		switch (note) {
		case BLOCK_START:
		    if (!inblk) {
			if (sequence[i+1] < 0)
			    reportErr(7);
			found = sequence[i+1];
			inblk = true;
			pblks.put(new Integer(found), new Integer(i));
			thisblen = 0;
			continue;
		    } else {
			reportErr(3);
		    }
		    break;

		case BLOCK_END:
		    if (inblk) {// blk end
			if (sequence[i+1] == found) {
			    inblk = false;
			    blens.put(new Integer(found), 
				      new Integer(thisblen));
			} else {
			    reportErr(1);
			}
			continue;
		    } else {
			reportErr(1);
		    }
		    break;
		    
		case REPEAT:
		    if (sequence[i+1] < 2)
			reportErr(9);
		    note = sequence[i+2];
		    if (!(note == SILENCE || note >= 0))
			reportErr(11);
		    break;
		   
		case SET_VOLUME:
		    if (sequence[i+1] < 0 || sequence[i+1] > 100)
			reportErr(10);
		    len += 2;
		    break;
		    
		case PLAY_BLOCK:
		    if (blens.get(new Integer(sequence[i+1])) == null) 
			reportErr(2);
		    
		    tmp = ((Integer)(blens.get(new Integer(sequence[i+1])))).
			intValue();	
		    if (inblk) {
			thisblen += tmp;	
		    } else {
			len += tmp;
		    }
		    break;

		case VERSION:
		case TEMPO:
		case RESOLUTION:
		    reportErr(12);
		    break;
		default: 
		    // SILENCE or normal tone
		    if (inblk) {
			thisblen += 2;
		    } else {
			len += 2;
		    }
		} // switch
	    } // end of for(i)
	    
	    if (inblk) {
		reportErr(1);
	    }
	    
	    // valid tone seq
	    toneSeq = new int[len];

	    curTime = 0;
	    p = 0;

	    i = startseq;
	    int mul = 1;

	    while (i < sequence.length) {
		note =  sequence[i];
		switch (note) {
		case BLOCK_START: // blk definition, start
		    do {
			i += 2;
		    } while (sequence[i] != BLOCK_END);
		    break;
		    
		case PLAY_BLOCK:  // play_blk
		    sp.push(new Integer(i+2));
		    i = ((Integer)pblks.get(new Integer(sequence[i+1]))).
			intValue() + 2;
		    continue;

		case BLOCK_END: // end playing blk
		    i = ((Integer)(sp.pop())).intValue();
		    continue;

		case SET_VOLUME:
		    // 0 <= sequence[i+1] <= 100
		    toneSeq[p++] = SET_VOLUME;
		    toneSeq[p++] = (sequence[i+1] & 0x7f);
		    
		    break;
		    
		case REPEAT:
		    // 2 <= sequence[i+1] <= 127
		    mul = sequence[i+1];
		    break;

		default: // regular tone or SILENCE
		    toneSeq[p++] = sequence[i];
		    // dur as milliseconds
		    toneSeq[p++] += (sequence[i+1]&0x7f) * mul * 240000 / frac;
		    curTime += toneSeq[p-1];
		    mul = 1;
		} // switch
		
		i += 2;
	    } // while
	} catch (IllegalArgumentException ex) {
	    throw ex;
	} catch (Exception ex) {
	    throw new IllegalArgumentException(ex.getMessage());
	}

	seqChanged = true;
    }
    
    /**
     * internal utility method to throw IAE
     * @param code the error code.  
     *
     */

    private void reportErr(int code) {
	throw new IllegalArgumentException("bad tone param: err code " + code);
    }

    /**
     * ==========================
     * Methods for VolumeControl.
     * =========================
     */
    /**
     * The worker method to actually obtain the control.
     *
     * @param vol the volume level to be set.  
     * @return the actual level has been set.
     */
    protected int  doSetLevel(int vol) {
	// native code takes care of the conversion from 100 to 127.
	toneCommon(ad, 7, vol); // SET_VOL
	return (vol);
    }
}
