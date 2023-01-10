/*
 * @(#)RecordedEvent.java	1.22 02/07/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import java.io.Reader;
import java.io.Writer;


/**
 * <code>RecordedEvent</code> is a class that encapsulates the semantics
 * of a single MIDP event.
 *
 * This class is designed to automate UI testing. It is the representation
 * of a single event which will comprise a larger event sequence.
 */

class RecordedEvent {

    // constants for the types of event
    static final byte  KEY_EVENT = 1;
    static final byte  INPUT_METHOD_EVENT = 2;
    static final byte  MENU_EVENT = 3;
    static final byte  PEN_EVENT = 4;
    static final byte  COMMAND_EVENT = 5;
    static final byte  DELAY_DUMMY_EVENT = 6;

    /**
     * The byte representation of an event is :
     * type             1 byte
     * timeDelay        4 bytes
     * val1/inputString 4 bytes
     * [val2]           4 bytes 
     * [val3]           4 bytes
     * 
     */

    byte type;
    int timeDelay;

    String inputString;

    int val1;
    int val2;
    int val3;


    // --- constructors ---

    /**
     * Creates a RecordedEvent for a DELAY_DUMMY_EVENT with the
     * given time
     * This is a dummy event used to create a delay.
     *
     * @param timeDelay The delay (in ms)
     */
    RecordedEvent(int timeDelay) {
        this.type = DELAY_DUMMY_EVENT;
        this.timeDelay = timeDelay;

	lengthInBytes = 5; // this.type(1) + timeDelay(4) 
    }

    /**
     * Creates a RecordedEvent for an INPUT_METHOD_EVENT with the
     * given type and input string.
     *
     * @param timeDelay The delay (in ms) since the previous event occurred
     * @param input     The String entered by the input method
     */
    RecordedEvent(int timeDelay, String input) {
        this.type = INPUT_METHOD_EVENT;
        this.timeDelay = timeDelay;
        inputString = input;

	lengthInBytes = 5 + input.length(); // this.type(1) + timeDelay(4) 
                                            // + String.length()
    }

    /**
     * Creates a RecordedEvent for a KEY_EVENT with the
     * given type and key code.
     *
     * @param timeDelay The delay (in ms) since the previous event occurred
     * @param type      The type of key event (either PRESSED, RELEASED,
     *                  REPEATED, or TYPED)
     * @param code      The key code of the key involved
     */
    RecordedEvent(int timeDelay, int type, int code) {
	
        this.type = KEY_EVENT;
        this.timeDelay = timeDelay;
        val1 = type;
        val2 = code;

	lengthInBytes = 13; // this.type(1) + timeDelay(4) + val1(4) + val2(4)
    }

    /**
     * Creates a RecordedEvent for a PEN_EVENT with the
     * given type and coordinate location
     *
     * @param timeDelay The delay (in ms) since the previous event occurred
     * @param type      The type of the POINTER_EVENT (either PRESSED,
     *                  RELEASED, or DRAGGED)
     * @param x         The x coordinate location of the event
     * @param y         The y coordinate location of the event
     */
    RecordedEvent(int timeDelay, int type, int x, int y) {
        this.type = PEN_EVENT;
        this.timeDelay = timeDelay;
        val1 = type;
        val2 = x;
        val3 = y;

	lengthInBytes = 17; // this.type(1) + timeDelay(4)
                            // + val1(4) + val2(4) + val3(4)
    }

    /**
     * Creates a RecordedEvent for a COMMAND event with
     * the given Command type (or index)
     *
     * @param timeDelay The delay (in ms) since the previous event occurred
     * @param type      The type of the COMMAND event (either MENU_REQUESTED,
     *                  MENU_DISMISSED) or the index of the COMMAND selected
     */
    RecordedEvent(int timeDelay, int type) {

        this.type = COMMAND_EVENT;
        this.timeDelay = timeDelay;
        val1 = type;

	lengthInBytes = 9; // this.type(1) + timeDelay(4) + val1(4)
    }

    
    // --- package private ---

    /**
     * Return an array of bytes containing the byte representation 
     * of this RecordedEvent
     *
     */
    public byte[] getAsByte() {
	byte[] rawBytes = new byte[lengthInBytes];
	int indexInByteArray;
	
	// byte 0 is the type of event
	rawBytes[0] = this.type;
	
	indexInByteArray = 1;

	// byte 1-4 is the timeDelay
	indexInByteArray += intIntoBytes(rawBytes, timeDelay, 
					 indexInByteArray);

	// bytes specific to the type of event

	switch (this.type) {
	case KEY_EVENT:

	indexInByteArray += intIntoBytes(rawBytes, val1, 
					 indexInByteArray);
	indexInByteArray += intIntoBytes(rawBytes, val2, 
					 indexInByteArray);

	    break;	    

	case INPUT_METHOD_EVENT:

	    indexInByteArray += stringIntoBytes(rawBytes, inputString, 
						indexInByteArray);

	    break;

	case MENU_EVENT:

	    // NYI

	    for (int i = 0; i < (lengthInBytes-5); i++) {
		rawBytes[indexInByteArray++] = 0x00;
	    }
	    break;


	case PEN_EVENT:

	indexInByteArray += intIntoBytes(rawBytes, val1, 
					 indexInByteArray);
	indexInByteArray += intIntoBytes(rawBytes, val2, 
					 indexInByteArray);
	indexInByteArray += intIntoBytes(rawBytes, val3, 
					 indexInByteArray);

	    break;	    

	case COMMAND_EVENT:

	indexInByteArray += intIntoBytes(rawBytes, val1, 
					 indexInByteArray);

	    break;	    

	case DELAY_DUMMY_EVENT:

	    // no extra bytes needed.

	    break;

	default:
	    
	    throw new IllegalArgumentException();
	}

	return rawBytes;
    }


    /**
     * Creates a RecordedEvent from an array of bytes.
     * This is called from EventSequence to recreate
     * a RecordedEvent from storage.
     *
     * @param byteArray The array of bytes to create the
     *                  event from.
     *
     */
    static RecordedEvent recreateRecordedEvent(byte[] byteArray, int offset, 
					       int length) {

	RecordedEvent returnEvent = null;

	int timeDelay = bytesToInt(byteArray, offset+1);

	switch (byteArray[offset+0]) {
	case KEY_EVENT:
	    
	    returnEvent = new RecordedEvent(timeDelay,
					     bytesToInt(byteArray, offset+5),
					     bytesToInt(byteArray, offset+9));

	    break;	    

	case INPUT_METHOD_EVENT:
	    
	    returnEvent = new RecordedEvent(timeDelay,
					    new String(byteArray,
						       offset+5,
						       length));

	    break;

	case MENU_EVENT:



	    break;

	case PEN_EVENT:

	    returnEvent = new RecordedEvent(timeDelay,
					    bytesToInt(byteArray, offset+5),
					    bytesToInt(byteArray, offset+9),
					    bytesToInt(byteArray, offset+13));

	    break;

	case COMMAND_EVENT:

	    returnEvent = new RecordedEvent(timeDelay,
					    bytesToInt(byteArray, offset+5));
	    break;
	    
	case DELAY_DUMMY_EVENT:

	    returnEvent = new RecordedEvent(timeDelay);

	    break;

	default:
	    throw new IllegalArgumentException();
	}

	return returnEvent;

    }


    /**
     *  Return the length of the byte representation of this
     *  RecordedEvent
     *
     */
    int lengthOfByteRep() {
	return this.lengthInBytes;
    }


    byte getType() {
	return this.type;
    }

    int getTimeDelay() {
	return this.timeDelay;
    }

    // -- private

    // --- Conversion to byte arrays
    
    /**
     * Copies the 4 individual bytes of an into 
     * the array starting from the specified
     * offset
     *
     */
    private static int intIntoBytes(byte[] byteArray, 
				    int intToConvert, 
				    int indexInByteArray) {

	byteArray[indexInByteArray]   = (byte)((intToConvert >> 24) & 0xFF);
	byteArray[indexInByteArray+1] = (byte)((intToConvert >> 16) & 0xFF);
	byteArray[indexInByteArray+2] = (byte)((intToConvert >>  8) & 0xFF);
	byteArray[indexInByteArray+3] = (byte)((intToConvert >>  0) & 0xFF);

	return 4;
    }


    /**
     * Copies the byte representation of the String
     * into the array starting from the specified
     * array offset
     *
     */
    private static int stringIntoBytes(byte[] byteArray,
				       String string,
				       int offsetInByteArray) {

	System.arraycopy((Object)string.getBytes(), // src
			 0,                         // src_position
			 (Object)byteArray,         // dest
			 offsetInByteArray,         // dest_position
			 string.length());

	return string.length();
    }


    /**
     * Converts 4 bytes to an int
     */
    private static int bytesToInt(byte[] byteArray, int offsetInByteArray) {
	
	int retVal = 0;

	retVal =
	    (((int)(byteArray[offsetInByteArray])   & (0xFF)) << 24) |
	    (((int)(byteArray[offsetInByteArray+1]) & (0xFF)) << 16) |
	    (((int)(byteArray[offsetInByteArray+2]) & (0xFF)) <<  8) |
	    (((int)(byteArray[offsetInByteArray+3]) & (0xFF)) <<  0);

	return retVal;
	
    }

    private int lengthInBytes;
}
