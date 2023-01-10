/*
 * @(#)AutomatedEventHandler.java	1.33 02/08/01 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

/**
 * This is the automated event handler for the LCDUI.
 */
public class AutomatedEventHandler extends DefaultEventHandler
             implements AutomationHandler {

    // constructor

    public AutomatedEventHandler() {
	super();

	// we initialize all hotkeys to an invalid key code
	hotKey_StartRecording  = hotKey_StopRecording  = 
	                         hotKey_CaptureScreen  = 0;

	/** FIXME */ // check that the above keycode(0) is invalid.


	// set the static variable that points to ourself.
	thisReference = this;


    }

    // from interface AutomationHandler

    /**
     * Start recording an event sequence. Any event sequence currently
     * being recorded will be destroyed and a new event sequence will
     * be started.
     */
    public void startEventSequence() {

	initializeEventSequence();

	recordingInProgress = true;

	// set the clock
	this.millis = System.currentTimeMillis();
    }

    /**
     * Stop recording an event sequence. This will stop recording the
     * current event sequence and capture the current contents of the
     * screen. It will then return an EventSequence object which is
     * the representation of the entire event sequence as well as the
     * screen capture resulting at the end of the sequence. If no events
     * occurred during the capture, the sequence will simply be a timed
     * delay with a screen capture. The timed delay will be the time delay
     * between the call to startEventSequence() and stopEventSequence().
     *
     * If stopEventSequence() is called without previously calling
     * startEventSequence(), an EventSequence will be returned which is
     * essentially "empty" and only contains the current contents of
     * the captured screen.
     *
     * @return EventSequence The sequence of events recorded as well as
     *                       the screen capture recorded at the end of
     *                       the sequence of events.
     */
    public EventSequence stopEventSequence() {

	if (recordingInProgress != true) {

	    // If stopEventSequence() is called without previously
	    // calling startEventSequence(), an EventSequence 
	    // will be returned which is essentially "empty" 

	    initializeEventSequence();
	}

	int time = (int)(System.currentTimeMillis() - this.millis);
	
	// this is a delay event
	RecordedEvent event = new RecordedEvent(time);
	eventSequence.appendEvent(event);
	
	// no need to set the clock
	// we won't we using it in recording anymore
	
	recordingInProgress = false;

	// append screen capture to event sequence
	byte[] s_cap = (ScreenGrabber.getInstance()).getData();
	eventSequence.setScreenCapture(s_cap);
	
	return eventSequence;
    }


    /**
     * Establish a SequenceHandler to handle the completion of EventSequences.
     * and screen captures which occur as a result of a 'hotkey' press.
     * If a sequence handler is set, that handler will be called when any
     * event sequence is completed, either as a result of stopEventSequence()
     * being called, or as a result of some "hot key" being pressed which
     * the event handler recognizes as a signal to stop event recording.
     * The EventSequence object passed to the handler will be the same as
     * the return result of the stopEventSequence() method. If a handler has
     * already been set, and this method is called again the old handler will
     * be discarded and the new handler will be used. If 'null' is passed,
     * any previous handler will be discarded.
     *
     * @param handler The SequenceHandler which will get the callback
     *                whenever an event sequence is completed.
     * @throws IllegalArgumentException If this particular port or platform
     *                                  cannot support hotkey activation, this
     *                                  method will throw an exception
     */
    public void registerSequenceHandler(SequenceHandler handler)
        throws IllegalArgumentException {
	sequenceHandler = handler;
    }


    /**
     * Establish a specific key code for the given action to serve as
     * a 'hotkey'. The action must be one of START_RECORDING, STOP_RECORDING,
     * or CAPTURE_SCREEN.
     *
     * @param action The action to register the hotkey for, one of
     *               START_RECORDING, STOP_RECORDING, or CAPTURE_SCREEN.
     * @param keyCode The key code of the new hotkey
     * @throws IllegalArgumentException If this particular port or platform
     *                                  cannot support hotkey activation, this
     *                                  method will throw an exception. An
     *                                  exception will also be thrown
     *                                  if the requested action or key code is
     *                                  invalid.
     */
    public void registerHotKey(int action, int keyCode)
        throws IllegalArgumentException {

	switch (action) {
	case START_RECORDING : 
	    hotKey_StartRecording = keyCode;
	    break;
	case STOP_RECORDING  :
	    hotKey_StopRecording  = keyCode;
            break;
        case CAPTURE_SCREEN  :
	    hotKey_CaptureScreen  = keyCode;
            break;
	}

    }


    /**
     * Replay a given event sequence. This will replay a sequence of
     * events represented in the given EventSequence object. It will
     * then capture the contents of the screen at the end of the event
     * sequence and compare it to the screen capture that is included
     * in the EventSequence object.
     *
     * @param sequence The EventSequence object to replay and test
     * @return True if the screen capture at the end of replaying
     *         the given event sequence matches that which is stored
     *         in the given EventSequence object.
     */
    public boolean replayEventSequence(EventSequence sequence) {

	// NOTE: Do we check if an event sequence is currently being recorded
	// replayed, before starting replay?
	sequence.initializeReplay();

	EventPlaybackThread eventPlaybackThread 
	    = new EventPlaybackThread(sequence, 100);

	eventPlaybackThread.start();

	// hang around until replay is finished.
	synchronized (thisReference) {
            try {
		thisReference.wait();
            } catch (InterruptedException e) {
		// ERROR!
                e.printStackTrace();
            }
	} 

	return lastScreenCaptureComparisonTrue;
    }

    class EventPlaybackThread extends Thread {
	EventSequence sequence;
	int           timeFactor;

	public EventPlaybackThread(EventSequence s, int tf) {
	    this.sequence   = s;
	    this.timeFactor = tf;
	}

	public void run() {
	    boolean playbackDone = false;
	    int t;

	    while (!playbackDone) {

		int nextObjectType = sequence.getNextObjectType();
	
		switch (nextObjectType) {
		case EventSequence.EVENT_START_MARKER :
	    
		    RecordedEvent event = sequence.getNextEvent();
		    t = event.getTimeDelay();

		    synchronized (this) {
			try {
			    sleep((t * timeFactor)/100);
			} catch (Exception e) {
			    System.out.println("Exception while sleep()-ing ");
			}
		    }

		    // get type of event
		    switch (event.getType()) {
		    case RecordedEvent.KEY_EVENT:
			keyEvent(event.val1, null, event.val2);
			break;
		    case RecordedEvent.INPUT_METHOD_EVENT:
			keyEvent(IME, event.inputString, 0);
			break;
		    case RecordedEvent.MENU_EVENT:

			break;
		    case RecordedEvent.PEN_EVENT:
			pointerEvent(event.val1, event.val2, event.val3);
			break;
		    case RecordedEvent.COMMAND_EVENT:
			commandEvent(event.val1);
			break;
		    case RecordedEvent.DELAY_DUMMY_EVENT:
			// This is a dummy event.
			// since the implementation has already delayed
			// (outside the switch .. case block),
			// do nothing.
			break;
		    default:
			System.out.println("unknown event type");
			// Unknown event type.
			// Should throw Exception?
			// Should stop replay?
			// Should continue with replay?
			break;
		    }

		    break;

		case EventSequence.CAPTURE_START_MARKER :

		    byte[] stored_s_cap = sequence.getCapture();

		    byte[] this_s_cap = 
			(ScreenGrabber.getInstance()).getData();

		    if (byteMatch(this_s_cap, stored_s_cap)) {
			// screen captures match
			lastScreenCaptureComparisonTrue = true;
		    } else {
			// screen captures did not match
			lastScreenCaptureComparisonTrue = false;
			// finish playback
			playbackDone = true;
		    }

		    break;

		case EventSequence.END_OF_EVENT_SEQUENCE : 
		    playbackDone = true;
		    break;

		default : 
		    // unknown error.
		    playbackDone = true;
		}

	    } 

	    synchronized (thisReference) {
		thisReference.notify();
	    }

	}
	
    }

    /**
     * Replay a given event sequence. This will replay a sequence of
     * events represented in the given EventSequence object. It will
     * then capture the contents of the screen at the end of the event
     * sequence and compare it to the screen capture that is included
     * in the EventSequence object.
     *
     * @param sequence The EventSequence object to replay and test
     * @param speed The factor by which to modify the replay speed of
     *              the event sequence. This is on a scale of 100,
     *              meaning a value of 100 would be normal speed,
     *              50 would be half speed, and 200 would be double
     *              speed.
     *
     * @return True if the screen capture at the end of replaying
     *         the given event sequence matches that which is stored
     *         in the given EventSequence object.
     */
    public boolean replayEventSequence(EventSequence sequence,
                                                int speed) {

	// NOTE: Do we check if an event sequence is currently being recorded/
	// replayed, before starting replay?
	sequence.initializeReplay();

	EventPlaybackThread eventPlaybackThread 
	    = new EventPlaybackThread(sequence, speed);

	eventPlaybackThread.start();

	// hang around until replay is finished.
	synchronized (thisReference) {
            try {
		thisReference.wait();
            } catch (InterruptedException e) {
		// ERROR!
                e.printStackTrace();
            }
	} 

	return lastScreenCaptureComparisonTrue;
    }


    /**
     * Update the screen capture for this event sequence. Over time, the
     * screen captures in a stored event sequence may become out of date.
     * Calling this method will run the given event sequence, capture
     * the resulting screen, and return the same EventSequence object
     * with the updated screen value.
     *
     * @param sequence The EventSequence to run and update the screen for
     * @return The updated EventSequence with the new captured screen value
     */
    public EventSequence updateScreenForSequence(
                                  EventSequence sequence) {

	return eventSequence;
    }


    /**
     * Capture the current contents of the physical display in the form of a
     * byte array. The byte array may be some reduced form of the display, such
     * as a checksum or hash, but must be guaranteed unique for the display
     * such that no two differing displays will have the same return value
     *
     * @return The byte[] of the captured screen contents
     */
    public byte[] captureScreen() {
	byte x[] = (ScreenGrabber.getInstance()).getData();

	return x;
    }


    // method to get this AutomatedEventHandler's reference
    public static AutomationHandler getAutomationHandler() {
	return thisReference;
    }

    
    // override DefaultEventHandler's implementation

    // these methods do what is needed by AutomatedEventHandler
    // and then call super's method.

    /**
     * Process a key event
     *
     * @param type The type of key event
     * @param str The String associated with an input method event
     * @param code The keycode of the key event
     */
    void keyEvent(int type, String str, int code) {

	// we have a key event
	// do we need to take a hot key action on this?
	if (type == 1 && // key press
            (code == hotKey_StartRecording ||
            code == hotKey_StopRecording   ||
	    code == hotKey_CaptureScreen)) {

		// process the hot key

		if (code == hotKey_StartRecording) {
		    startEventSequence();
		}

		if (code == hotKey_StopRecording) {
		    stopEventSequence();
		    // note : this method also performs a screen capture
		    // does this have any implications?
		    if (sequenceHandler != null) {
			sequenceHandler.handleEventSequence(eventSequence);
		    }
		}

		if (code == hotKey_CaptureScreen) {
		    byte s_cap[] = captureScreen();
		    if (sequenceHandler != null) {
			sequenceHandler.handleScreenCapture(s_cap);
		    }
		}

	} else {

	    if (recordingInProgress) {

		if (type == IME) {

		    int time = (int)(System.currentTimeMillis() - this.millis);

		    // this is an input method event
		    RecordedEvent event = new RecordedEvent(time, str);
		    eventSequence.appendEvent(event);

		    // set the clock
		    this.millis = System.currentTimeMillis();

		} else {

		    int time = (int)(System.currentTimeMillis() - this.millis);

		    // this is a KEY_EVENT
		    RecordedEvent event = new RecordedEvent(time, type, code);
		    eventSequence.appendEvent(event);

		    // set the clock
		    this.millis = System.currentTimeMillis();

		}

	    }

	}

	super.keyEvent(type, str, code);

    }

    /**
     * Process a pointer event
     *
     * @param type The type of pointer event
     * @param x The x coordinate location of the event
     * @param y The y coordinate location of the event
     */
    void pointerEvent(int type, int x, int y) {

	if (recordingInProgress) {

	    int time = (int)(System.currentTimeMillis() - this.millis);

	    RecordedEvent event = new RecordedEvent(time, type, x, y);
	    eventSequence.appendEvent(event);

	    // set the clock
	    this.millis = System.currentTimeMillis();

	}

	super.pointerEvent(type, x, y);

    }

    /**
     * Process a command event
     *
     * @param type The type of Command event to process
     */
    void commandEvent(int type) {
	if (recordingInProgress) {

	    int time = (int)(System.currentTimeMillis() - this.millis);

	    RecordedEvent event = new RecordedEvent(time, type);
	    eventSequence.appendEvent(event);

	    // set the clock
	    this.millis = System.currentTimeMillis();

	}

	super.commandEvent(type);
    }


    // -- private ---

    /**
     * Initialize the event sequence for recording.
     * Currently, this allocates a new EventSequence
     * 
     */
    private void initializeEventSequence() {
	eventSequence = new EventSequence();
    }

    /**
     * Checks if two byte arrays match.
     * <P />
     * @param a first byte array
     * @param b second byte array
     * @return true if the sequence of bytes in a matches those in b,
     *         false otherwise
     */ 
    private static boolean byteMatch(byte[] a, byte[] b) {
        if (a.length != b.length)
	    return false;

        int len = a.length;

        for (int i = 0; i < len; i++) {
            if (a[i] != b[i])
                return false;
        }

        return true;
    }


    // reference to ourself
    static AutomationHandler thisReference;

    EventSequence eventSequence;

    SequenceHandler sequenceHandler;

    int hotKey_StartRecording;
    int hotKey_StopRecording;
    int hotKey_CaptureScreen;

    // was the last screen capture a true match?
    boolean lastScreenCaptureComparisonTrue;

    // are we recording?
    boolean recordingInProgress;

    // time
    long millis;
}
