/*
 * @(#)AutomationHandler.java	1.7 02/07/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

/**
 * The automation handler class. This is an abstract class which
 * which provides options to record and playback a sequence of events
 * for automation purposes, as well as capture the contents of the
 * screen.
 */
public interface AutomationHandler {
    /**
     * The identifier for the 'start recording' action to be used
     * when registering a new hotkey. The default hotkey for this
     * action is 'F1'.
     */
    public static final int START_RECORDING = 0;

    /**
     * The identifier for the 'stop recording' action to be used
     * when registering a new hotkey. The default hotkey for this
     * action is 'F2'.
     */
    public static final int STOP_RECORDING  = 1;

    /**
     * The identifier for the 'capture screen' action to be used
     * when registering a new hotkey. The default hotkey for this
     * action is 'F3'.
     */
    public static final int CAPTURE_SCREEN  = 2;

    /**
     * Start recording an event sequence. Any event sequence currently
     * being recorded will be destroyed and a new event sequence will
     * be started.
     */
    public void startEventSequence();

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
    public EventSequence stopEventSequence();

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
        throws IllegalArgumentException;

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
        throws IllegalArgumentException;

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
    public boolean replayEventSequence(EventSequence sequence);

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
                                                int speed);

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
        EventSequence sequence);

    /**
     * Capture the current contents of the physical display in the form of a
     * byte array. The byte array may be some reduced form of the display, such
     * as a checksum or hash, but must be guaranteed unique for the display
     * such that no two differing displays will have the same return value
     *
     * @return The byte[] of the captured screen contents
     */
    public byte[] captureScreen();

}


