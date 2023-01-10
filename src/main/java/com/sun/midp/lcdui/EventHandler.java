/*
 * @(#)EventHandler.java	1.35 02/08/21 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

/**
 * The Event Handler interface.  The constant values constitute the
 * protocol between the runtime and the Event Delivery system.
 * The protocol typically consists of a "Major ID," followed optionally
 * by a "Minor ID" and other data.
 */
public interface EventHandler {

    /**
     * Major ID for an event on a key.
     */
    static final int KEY_EVENT     =  1;

    /**
     * Major ID for a pointer event.
     */
    static final int PEN_EVENT     =  2;

    /**
     * Major ID for an Abstract Command.
     */
    static final int COMMAND_EVENT =  3;

    /**
     * Major ID for a "system" event. This type of
     * an event is for identifying things like
     * interruptions by the underlying device to
     * stop the VM, pause the current MIDlet, suspend
     * operations, kill the current MIDlet, etc.
     */
    static final int SYSTEM_EVENT  =  4;

    /**
     * Major ID for an multimedia EOM event.
     * --- by hsy
     */
    static final int MM_EOM_EVENT  =  8;

    /**
     * Minor ID indicating a press, either on a key or a pointer.
     */
    static final int PRESSED  = 1;  // key, pen

    /**
     * Minor ID indicating a release, either of a key or a pointer.
     */
    static final int RELEASED = 2;  // key, pen

    /**
     * Minor ID indicating a key repeat.
     */
    static final int REPEATED = 3;  // key

    /**
     * Minor ID indicating a character typed (internal).
     */
    static final int TYPED    = 4;

    /**
     * Minor ID indicating a IME string typed (internal).
     */
    static final int IME      = 5;

    /**
     * Minor ID indicating a pointer drag.
     */
    static final int DRAGGED  = 3;  // pen

    /**
     * Minor ID indicating that command event requires posting a menu.
     */
    static final int MENU_REQUESTED   = -1;

    /**
     * Minor ID indicating that command event is dismissing a menu.
     */
    static final int MENU_DISMISSED   = -2;

    /**
     * The value returned from getSystemKey if the keyCode is the POWER key.
     */
    static final int SYSTEM_KEY_POWER = 1;

    /**
     * The value returned from getSystemKey if the keyCode is SEND.
     */
    static final int SYSTEM_KEY_SEND  = 2;

    /**
     * The value returned from getSystemKey if the keyCode is END.
     */
    static final int SYSTEM_KEY_END   = 3;

    /**
     * The value returned from getSystemKey if the keyCode is CLEAR.
     */
    static final int SYSTEM_KEY_CLEAR = 4;

    /**
     * Minor ID indicating a SYSTEM_EVENT to suspend all current
     * activity
     */
    static final int SUSPEND_ALL        = 1;

    /**
     * Minor ID indicating a SYSTEM_EVENT to resume the currently
     * suspended MIDlet (that is, the followup to a previous
     * SUSPEND_CURRENT event)
     */
    static final int RESUME_ALL         = 2;

    /**
     * Minor ID indicating a SYSTEM_EVENT to stop all MIDlets
     * (active and paused) and exit the system
     */
    static final int SHUTDOWN           = 3;

    /**
     * Minor ID indicating a SYSTEM_EVENT to pause the currently
     * active MIDlet (and optionally return to the selector)
     */
    static final int SUSPEND_CURRENT    = 4;

    /**
     * Minor ID indicating a SYSTEM_EVENT to resume the currently
     * paused MIDlet
     */
    static final int RESUME_PREVIOUS    = 5;

    /**
     * Minor ID indicating a SYSTEM_EVENT to kill the currently
     * active MIDlet (and optionally return to the selector)
     */
    static final int KILL_CURRENT       = 6;

    /**
     * Get the system-specific key code corresponding to the given gameAction.
     * @param gameAction A game action
     * @return int The keyCode associated with that action
     */
    int  getKeyCode(int gameAction);

    /**
     * Get the abstract gameAction corresponding to the given keyCode.
     * @param keyCode A system-specific keyCode
     * @return int gameAction The abstract game action associated with
     *      the keyCode
     */
    int  getGameAction(int keyCode);

    /**
     * Get the informative key string corresponding to the given keyCode.
     * @param keyCode A system-specific keyCode
     * @return String a string name for the key, or null if no name is
     *     available
     */
    String getKeyName(int keyCode);

    /**
     * Get the abstract system key that corresponds to keyCode.
     * @param keyCode A system-specific keyCode
     * @return int 0 The SYSTEM_KEY_ constant for this keyCode, or 0 if none
     */
    int getSystemKey(int keyCode);

    /**
     * Set the current set of active Abstract Commands.
     * @param itemCommands The list of Item Commands that should be active
     * @param numItemCommands The number of Item commands in the list
     * @param commands The list of Commands that should be active
     * @param numCommands The number of commands in the list
     */
    void updateCommandSet(Command[] itemCommands, int numItemCommands,
			  Command[] commands, int numCommands);

    /**
     * Returns true if the current thread is the EventHandler's dispatch
     *         thread.
     * @return  boolean True if the current thread is this EventHandler's
     *                  dispatch thread
     */
    boolean isDispatchThread();

    /**
     * Called to force the event handler to clear whatever system screen
     * has interrupted the current Displayable and allow the foreground
     * Display to resume painting.
     */
    void clearSystemScreen();

    /**
     * Called to schedule a screen change to the given Displayable
     * as soon as possible
     *
     * @param parent parent Display of the Displayable
     * @param d The Displayable to change to
     */
    void scheduleScreenChange(Display parent, Displayable d);

    /**
     * Called to schedule a repaint of the current Displayable
     * as soon as possible
     *
     * @param x     The x coordinate of the origin of the repaint rectangle
     * @param y     The y coordinate of the origin of the repaint rectangle
     * @param w     The width of the repaint rectangle
     * @param h     The height of the repaint rectangle
     * @param target An optional target Object, which may have been the
     *               original requestor for the repaint
     */
    void scheduleRepaint(int x, int y, int w, int h, Object target);

    /**
     * Called to schedule a serial callback of a Runnable object passed
     * into Display's callSerially() method.
     */
    void scheduleCallSerially();

    /**
     * Called to schedule an invalidation of a Form
     *
     * @param src the Item which may be causing the invalidation
     */
    void scheduleInvalidate(Item src);

    /**
     * Called to schedule an ItemStateChanged notification
     *
     * @param src the Item which has changed
     */
    void scheduleItemStateChanged(Item src);

    /**
     * Called to service any pending repaint operations
     */
    void serviceRepaints();

}
