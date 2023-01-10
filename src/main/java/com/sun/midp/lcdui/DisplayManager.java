/*
 * @(#)DisplayManager.java	1.9 02/10/14 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

import javax.microedition.midlet.MIDlet;

import com.sun.midp.security.SecurityToken;

/**
 * This class works around the fact that public classes can not
 * be added to a javax package by an implementaion.
 */
public interface DisplayManager extends DisplayEvents {
    /**
     * Add a listener for system events.
     *
     * @param l object to notify with system events such as shutdown
     */
    public void addSystemEventListener(SystemEventListener l);

    /**
     * Places the display of the given MIDlet in the list of active displays
     * and registers an object to receive events on behalf of the MIDlet.
     *
     * @param l object to notify with MIDlet events such as pause.
     * @param m MIDlet to activate
     */
    public void activate(MIDletEventListener l, MIDlet m);

    /**
     * Removes the display of the given MIDlet from the list of active displays
     * and unregisters MIDlet's event listener.
     *
     * @param m MIDlet to deactivate
     */
    public void deactivate(MIDlet m);

    /**
     * Preempt the current displayable with
     * the given displayable until donePreempting is called.
     * The preemptor should stop preempting when a destroyMIDlet event occurs.
     * The event will have a null MIDlet parameter.
     *
     * @param token security token for the calling class
     * @param l object to notify with the destroy MIDlet event.
     * @param d displayable to show the user
     * @param waitForDisplay if true this method will wait if the
     *        screen is being preempted by another thread.
     *
     * @return an preempt token object to pass to donePreempting done if prempt
     *  will happen, else null
     *
     * @exception SecurityException if the caller does not have permission
     *   the internal MIDP permission.
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public Object preemptDisplay(SecurityToken token, MIDletEventListener l,
        Displayable d, boolean waitForDisplay) throws InterruptedException;

    /**
     * Display the displayable that was being displayed before
     * preemptDisplay was called.
     *
     * @param preemptToken the token returned from preemptDisplay
     */
    public void donePreempting(Object preemptToken);

    /**
     * Release the system event listener set during the call to
     * getDisplayManger.
     *
     * @param l object that was being notified with system events
     */
    public void releaseSystemEventListener(SystemEventListener l);

    /**
     * Suspend Pause all to allow the system to use the
     * display. This will result in calling pauseApp() on the
     * all of the active MIDlets. A subsequent 'resumeAll()' call will
     * return the all of the paused midlets to active status.
     * <p>
     * Called by the system event handler within Display.
     */
    public void suspendAll();

    /**
     * Resume the currently suspended state. This is a result
     * of the underlying system returning control to MIDP.
     * Any previously paused foreground MIDlet will be restarted
     * and the Display will be refreshed.
     * <p>
     * Called by the system event handler within Display.
     * <p>
     */
    public void resumeAll();

    /**
     * Shutdown all running MIDlets and prepare the MIDP runtime
     * to exit completely.
     */
    public void shutdown();

    /**
     * Suspend the current foreground MIDlet and return to the
     * AMS or "selector" to possibly run another MIDlet in the
     * currently active suite. Currently, the RI does not support
     * running multiple MIDlets, but if it did, this system
     * callback would allow it.
     * <p>
     * Called by the system event handler within Display.
     */
    public void suspendCurrent();
 
    /**
     * Resume the currently suspended state. This is a result
     * of the underlying system returning control to MIDP.
     * Any previously paused foreground MIDlet will be restarted
     * and the Display will be refreshed.
     * <p>
     * Called by the system event handler within Display.
     */
    public void resumePrevious();

    /**
     * Kill the current foreground MIDlet and return to the
     * AMS or "selector" to possibly run another MIDlet in the
     * currently active suite. This is a system callback which
     * allows a user to forcibly exit a running MIDlet in cases
     * where it is necessary (such as a rogue MIDlet which does
     * not provide exit capability).
     * <p>
     * Called by the system event handler within Display.
     */
    public void killCurrent();

    /**
     * Called by event delivery when a screen change needs to occur.
     *
     * @param parent parent Display of the Displayable
     * @param screen The Displayable to make current in the Display
     */
    public void screenChange(Display parent, Displayable screen);

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public void initSecurityToken(SecurityToken token);

    /**
     * Get the Image of the trusted icon for this Display.
     * Only callers with the internal AMS permission can use this method.
     *
     * @return an Image of the trusted icon.
     *
     * @exception SecurityException if the suite calling does not have the
     * the AMS permission
     */
    public Image getTrustedMIDletIcon();

    /**
     * Create a display and return its internal access object.
     *
     * @param token security token for the calling class
     * @param midlet MIDlet that will own this display
     *
     * @return new display's access object
     *
     * @exception SecurityException if the caller does not have permission
     *   the internal MIDP permission.
     */
    public DisplayAccess createDisplay(SecurityToken token, MIDlet midlet);
}
