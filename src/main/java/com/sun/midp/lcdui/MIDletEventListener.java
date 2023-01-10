/*
 * @(#)MIDletEventListener.java	1.4 02/07/24 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import javax.microedition.midlet.MIDlet;

/** This is the DisplayManager/Scheduler contract for MIDlet events. */
public interface MIDletEventListener {
    /**
     * Pause the current foreground MIDlet and return to the
     * AMS or "selector" to possibly run another MIDlet in the
     * currently active suite. Currently, the RI does not support
     * running multiple MIDlets, but if it did, this system
     * callback would allow it. The listener should not deactivate
     * the display of the MIDlet.
     *
     * @param midlet midlet that the event applies to
     */
    public void pauseMIDlet(MIDlet midlet);
 
    /**
     * Start the currently suspended state. This is a result
     * of the underlying system returning control to MIDP.
     * Any previously paused foreground MIDlet will be restarted
     * and the Display will be refreshed. The listener should not activate
     * the display of the MIDlet since this will be done automatically.
     *
     * @param midlet midlet that the event applies to
     */
    public void startMIDlet(MIDlet midlet);
 
    /**
     * Destroy the MIDlet given midlet. Return to the control to
     * AMS if there are no another active MIDlets in the
     * scheduled. This is a system callback which
     * allows a user to forcibly exit a running MIDlet in cases
     * where it is necessary (such as a rogue MIDlet which does
     * not provide exit capability).
     *
     * @param midlet midlet that the event applies to
     */
    public void destroyMIDlet(MIDlet midlet);
} 
