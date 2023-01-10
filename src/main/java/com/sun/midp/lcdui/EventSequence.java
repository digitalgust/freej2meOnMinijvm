/*
 * @(#)EventSequence.java	1.20 02/07/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import java.io.*;
import java.util.Vector;

/**
 * <code>EventSequence</code> is a class that encapsulates the semantics
 * of a single MIDP event.
 *
 * This class is designed to automate UI testing. It is the representation
 * of a sequence of events and the corresponding screen capture taken at
 * the end of those events.
 */

public class EventSequence {

    private byte[] capture;

    /**
     * An input stream of bytes represents events
     * that have been recorded previously and that are
     * passed to this EventSequence
     */
    private ByteArrayInputStream  inputStream;

    /**
     * The byte array associated with the event sequence.
     */
    private byte[] byteArray;

    /**
     * The output stream represents events that are 
     * to be recorded in this EventSequence
     */
    private ByteArrayOutputStream outputStream;


    EventSequence() {

	// set up the empty input stream and byte array
	byteArray = new byte[0];
	inputStream = new ByteArrayInputStream(byteArray);

	outputStream = new ByteArrayOutputStream();
    }

    void appendEvent(RecordedEvent event) {

	try {

	    outputStream.write(EVENT_START_MARKER);
	    outputStream.write(event.lengthOfByteRep());
	    outputStream.write(event.getAsByte());

	} catch (java.io.IOException e) {
	    System.out.println("EXCEPTION!");
	    // EXCEPTION!
	}

    }

    void setScreenCapture(byte[] capture) {

        this.capture = capture;

	    outputStream.write(CAPTURE_START_MARKER);
	    outputStream.write(capture.length);

	for (int byteCounter = 0; byteCounter < capture.length; 
	    byteCounter++) {
		outputStream.write(capture[byteCounter]);

	}

    }

    /**
     * Recreate a stored event sequence and its associated screen
     * capture from the given input stream.
     *
     * @param input The byte[] which was obtained from a previous
     *              EventSequence from its getEventSequence() method.
     */
    public EventSequence(byte[] input) {
	byteArray = input;
	inputStream = new ByteArrayInputStream(byteArray);
	
	outputStream = new ByteArrayOutputStream();
    }
    
    /**
     * Create a new EventSequence Object based on a set of other
     * EventSequences. This will create a composite EventSequence
     * object which will be treated as a single sequence by the
     * AutomatedHandler when testing. That is, a call to
     * replayEventSequence() with a composite EventSequence Object
     * will test each sub sequence and its screen capture. If any
     * sub sequence fails, it will return false. If all of the sub
     * sequences pass, it will return true.
     *
     * @param sequences An array of EventSequence objects to construct
     *                  a new composite EventSequence from
     */
    public EventSequence(EventSequence[] sequences) {
	try {
	    for (int i = 0; i < sequences.length; i++) {
		this.outputStream.write(sequences[i].toByteArray());
	    }
	} catch (IOException e) {
	    // System.out.println("Exception in writing to stream");
	}
    }
    
    /**
     * Create a new EventSequence Object based on a set of other
     * EventSequences. This will create a composite EventSequence
     * object which will be treated as a single sequence by the
     * AutomatedHandler when testing. That is, a call to
     * replayEventSequence() with a composite EventSequence Object
     * will test each sub sequence and its screen capture. If any
     * sub sequence fails, it will return false. If all of the sub
     * sequences pass, it will return true.
     *
     * @param sequences A Vector of EventSequence objects to construct
     *                  a new composite EventSequence from
     */
    public EventSequence(Vector sequences) {
	
	int length = sequences.size();
	EventSequence q;
	
	try {
	    for (int i = 0; i < length; i++) {
		q = (EventSequence)sequences.elementAt(i);
		this.outputStream.write(q.toByteArray());
	    }
	} catch (IOException e) {
	    // System.out.println("Exception in writing to stream");
	}
    }
    
    /**
     * Store this EventSequence and its corresponding screen
     * capture to the designated output stream.
     *
     * @return The contents of this event sequence as an array
     *         of bytes. This array of bytes can be passed to
     *         the constructor to re-create the event sequence.
     */
    public byte[] toByteArray() {
	// the entire eventSequence consists of the original input sequence
	// null if there was no original input sequence
	// plus the events that were appended
	
	// therefore when we are returning the byte representation,
	// we actually do three things.
	// 1. append any existing bytes in the output array to the input array.
	// 2. reset the output array
	// 3. return the bytearray
	
	byte[] tempByteArray = new byte[byteArray.length + 
					outputStream.toByteArray().length];
	
	// Copy existing bytes in the byteArray to tempByteArray
	System.arraycopy(byteArray,
			 0,
			 tempByteArray,
			 0,
			 byteArray.length);
	
	// Append bytes from the output streams' byte array
	// to tempByteArray
	// byteArray.length is the offset to start from in the dst
	System.arraycopy(outputStream.toByteArray(),
			 0,
			 tempByteArray,
			 byteArray.length,
			 outputStream.toByteArray().length);
	
	// now tempByteArray holds all the bytes!
	// make it the byteArray!
	byteArray = tempByteArray;
	
	// reset the output stream
	outputStream.reset();
	
	return byteArray;
    }
    
    /**
     * Append the given EventSequence to this sequence and create
     * a composite event sequence as a result.
     *
     * @param sequence The EventSequence to append to the end of
     *                 this sequence.
     */
    public void appendSequence(EventSequence sequence) {
	try {
	    this.outputStream.write(sequence.toByteArray());
	} catch (IOException e) {
	    // System.out.println("Exception in writing to stream");
	}
    }


    // --- Retrieve Individual Events and Screen Captures ---

    void initializeReplay() {
	
	byte[] tempByteArray = new byte[byteArray.length + 
					outputStream.toByteArray().length];

         // Copy existing bytes in the byteArray to tempByteArray
	 System.arraycopy(byteArray,
			  0,
			  tempByteArray,
			  0,
			  byteArray.length);

	 // Append bytes from the output streams' byte array
	 // to tempByteArray
	 // byteArray.length is the offset to start from in the dst
	 System.arraycopy(outputStream.toByteArray(),
			  0,
			  tempByteArray,
			  byteArray.length,
			  outputStream.toByteArray().length);

	 // now tempByteArray holds all the bytes!
	 // make it the byteArray!
	 byteArray = tempByteArray;

	 // reset the output stream
	 outputStream.reset();
	 
	 inputStream = new ByteArrayInputStream(byteArray);
	 // input stream now points at 0 index in the byteArray
    }

    int getNextObjectType() {
	
	return (inputStream.read());
	// returns -1 if end of input stream is reached.
	
    }

    RecordedEvent getNextEvent() {
	
	RecordedEvent event = null;
	
	int length = inputStream.read();

	if (length == 0xFF) {
	    // if the length byte's value is 0xFF,
	    // then the length is greater than 127 and
	    // needs to be read as an int.

	    // using this as most events' length will
	    // be small engouh, that a byte is sufficient 
	    // to represent it.
	    
	}

	byte[] eventByteArray = new byte[length];

	inputStream.read(eventByteArray, 0, length);

	event = RecordedEvent.recreateRecordedEvent(eventByteArray, 
						    0, length);

	return event;
    }

    byte[] getCapture() {

	int length = inputStream.read();
	
	byte[] captureByteArray = new byte[length];

	inputStream.read(captureByteArray, 0, length);

	return captureByteArray;
	 
    }

    // encoding
    static final int EVENT_START_MARKER   =  0;
    static final int CAPTURE_START_MARKER =  1;
    static final int END_OF_EVENT_SEQUENCE = -1;
}
