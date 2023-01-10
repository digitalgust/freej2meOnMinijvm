/*
 * @(#)DefaultEventHandler.java	1.77 02/09/16 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.media.PlayerListener;

import com.sun.midp.main.Configuration;
import com.sun.mmedia.BasicPlayer;

/**
 * This is the default event handler for the LCDUI.
 */
public class DefaultEventHandler implements EventHandler {

// ------- Private Data Members ------- //

    /**
     * The eventThread is a single thread which processes
     * all the events originating in both the VM and profile layers.
     */
    private Thread eventThread;
    /**
     * The low level vm event handler routine. This handler reads
     * event types off the event stream and marshals the arguments
     * to the proper event routines.
     */
    private VMEventHandler vmEventHandler;
    /**
     * The high level queued event handler routine. This handler
     * processes all the events in the event queue and marshals
     * the arguments to the proper event routines.
     */
    private QueuedEventHandler queuedEventHandler;
    /**
     * Queue which contains all the events of the system. Both those
     * which originate from the VM as well as those which originate
     * from application or MIDP code.
     */
    private EventQueue eventQueue;
    /**
     * The DisplayManager object handling the display events.
     */
    private DisplayManager displayManager;
    /**
     * This is a special lock object for synchronizing event
     * deliveries between the different event threads
     */
    protected Object eventLock;

    // These are for handling Abstract Command menus.

    /**
     * This field is true if the current display is the menu screen.
     */
    private boolean inMenu; // = false;

// ------- Public Methods -------- //

    /**
     * Get the system-specific key code corresponding to the given gameAction.
     *
     * @param gameAction A game action
     * @return The keyCode associated with that action
     */
    public native int  getKeyCode(int gameAction);

    /**
     * Get the abstract gameAction corresponding to the given keyCode.
     *
     * @param keyCode A system-specific keyCode
     * @return gameAction The abstract game action associated with the keyCode
     */
    public native int  getGameAction(int keyCode);

    /**
     * Get the abstract system key that corresponds to keyCode.
     *
     * @param keyCode A system-specific keyCode
     * @return The SYSTEM_KEY_ constant for this keyCode, or 0 if none
     */
    public native int  getSystemKey(int keyCode);

    /**
     * Get the informative key string corresponding to the given keyCode.
     *
     * @param keyCode A system-specific keyCode
     * @return a string name for the key, or null if no name is available
     */
    public native String getKeyName(int keyCode);

    /**
     * Set the current set of active Abstract Commands.
     *
     * @param itemCommands The list of Item Commands that should be active
     * @param numItemCommands The number of Item commands in the list
     * @param commands The list of Commands that should be active
     * @param numCommands The number of commands in the list
     */
    public native void updateCommandSet(Command[] itemCommands, 
					int numItemCommands,
					Command[] commands, int numCommands);

    /**
     * Returns true if the current thread is the EventHandler's dispatch
     * thread.
     *
     * @return true if the current thread is this EventHandler's
     *         dispatch thread
     */
    public boolean isDispatchThread() {
        return (Thread.currentThread() == eventThread);
    }

    /**
     * Called to force the event handler to clear whatever system screen
     * has interrupted the current Displayable and allow the foreground
     * Display to resume painting.
     */
    public void clearSystemScreen() {
        inMenu = false;
        dismissMenu();
    }

    /**
     * Called to schedule a screen change to the given Displayable
     * as soon as possible
     *
     * @param parent parent Display of the Displayable
     * @param d The Displayable to change to
     */
    public void scheduleScreenChange(Display parent, Displayable d) {
        eventQueue.push(parent, d);
    }

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
    public void scheduleRepaint(int x, int y, int w, int h, Object target) {
        eventQueue.push(x, y, w, h, target);
    }

    /**
     * Called to schedule a serial callback of a Runnable object passed
     * into Display's callSerially() method.
     */
    public void scheduleCallSerially() {
        eventQueue.push();
    }

    /**
     * Called to schedule an invalidation of a Form
     *
     * @param src the Item which may have caused the invalidation
     */
    public void scheduleInvalidate(Item src) {
        eventQueue.push(src, true);
    }

    /**
     * Called to schedule a call to an ItemStateChangeListener
     *
     * @param src the Item which has changed
     */
    public void scheduleItemStateChanged(Item src) {
        eventQueue.push(src, false);
    }

    /**
     * Called to service any pending repaint operations
     */
    public void serviceRepaints() {
        try {
            eventQueue.serviceRepaints();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

// -------- Constructor --------- //

    /**
     * The constructor for the default event handler for LCDUI.
     */
    public DefaultEventHandler() {
        displayManager = DisplayManagerFactory.getDisplayManager();
        eventLock = new Object();

        // FIRE UP THE HIGH LEVEL EVENT THREAD
        try {
            queuedEventHandler = new QueuedEventHandler();
            eventThread = new Thread(queuedEventHandler);
            eventThread.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // FIRE UP THE LOW LEVEL VM EVENT READER
        try {
            vmEventHandler = new VMEventHandler();
            (new Thread(vmEventHandler)).start();
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

// ------- Package Private Event Handling Routines -------- //

// These will be utilized by the Automated Event Handler subclass //

    /**
     * Process a key event
     *
     * @param type The type of key event
     * @param str The String associated with an input method event
     * @param code The keycode of the key event
     */
    void keyEvent(int type, String str, int code) {
        try {
            if (type == IME) {
                synchronized (eventLock) {
                    displayManager.inputMethodEvent(str);
                }
            } else if (getSystemKey(code) == SYSTEM_KEY_END &&
                    type == RELEASED) {
                synchronized (eventLock) {
                    displayManager.killCurrent();
                }
            } else if (inMenu) {
                // native method, no need to synchronize
                inMenu = menuKeyEvent(type, code);
            } else {
                synchronized (eventLock) {
                    displayManager.keyEvent(type, code);
                }
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    /**
     * Process a pointer event
     *
     * @param type The type of pointer event
     * @param x The x coordinate location of the event
     * @param y The y coordinate location of the event
     */
    void pointerEvent(int type, int x, int y) {
        try {
            if (inMenu) {
                // native method, no need to synchronize
                inMenu = menuPointerEvent(type, x, y);
            } else {
                synchronized (eventLock) {
                    displayManager.pointerEvent(type, x, y);
                }
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    /**
     * Process a command event
     *
     * @param type The type of Command event to process
     */
    void commandEvent(int type) {
        try {
            synchronized (eventLock) {
                if (type == MENU_REQUESTED) {
                    displayManager.suspendPainting();
                    paintMenu();
                    inMenu = true;
                } else {
                    if (inMenu) {
                        displayManager.resumePainting();
                    }
                    inMenu = false;
                    if (type >= 0) {
                        displayManager.commandAction(type);
                    }
                }
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }
    /**
     * Process a system event
     *
     * @param type The type of system event to process, such as
     *             suspend, pause, kill, exit
     */
    void systemEvent(int type) {
        try {
            synchronized (eventLock) {
                switch (type) {
                    case SUSPEND_ALL:
                    displayManager.suspendAll();
                    // NOTE: A real port would not use 'incomingCall()'
                    // and the line below would be removed.
                    // It exists solely in the RI to simulate the fact
                    // that the system is doing something which has
                    // suspended MIDP
                    incomingCall();
                    break;
                    case RESUME_ALL:
                    displayManager.resumeAll();
                    break;
                    case SHUTDOWN:
                    displayManager.shutdown();
                    case SUSPEND_CURRENT:
                    displayManager.suspendCurrent();
                    break;
                    case RESUME_PREVIOUS:
                    displayManager.resumePrevious();
                    break;
                    case KILL_CURRENT:
                    displayManager.killCurrent();
                    break;
                    default:
                    break;
                }
            } // synchronized
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    /**
     * Process a multimedia event
     *
     * @param playerID The player indentity
     * @param curMTime The current time in milliseconds
     */
    void multiMediaEvent(int playerID, int curMTime) {
        try {
            BasicPlayer p = BasicPlayer.get(playerID);
            if (p != null) {
                p.sendEvent(PlayerListener.END_OF_MEDIA, new Long(curMTime));
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    /**
     * Process a screen change event
     *
     * @param parent parent Display of the Displayable
     * @param d The Displayable to make current
     */
    void screenChangeEvent(Display parent, Displayable d) {
        try {
            synchronized (eventLock) {
                displayManager.screenChange(parent, d);
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    /**
     * Process a repaint event
     *
     * @param x1 The x origin coordinate
     * @param y1 The y origin coordinate
     * @param x2 The lower right x coordinate
     * @param y2 The lower right y coordinate
     * @param target The optional paint target
     */
    void repaintScreenEvent(int x1, int y1, int x2, int y2, Object target) {
        try {
            synchronized (eventLock) {
                displayManager.repaint(x1, y1, x2, y2, target);
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    /**
     * Process all pending call serially's
     */
    void callSeriallyEvent() {
        try {
            synchronized (eventLock) {
                displayManager.callSerially();
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    /**
     * Process a Form invalidation
     *
     * @param src the Item which may have caused the invalidation
     */
    void validateEvent(Item src) {
        try {
            synchronized (eventLock) {
                displayManager.callInvalidate(src);
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    /**
     * Process an item state change
     *
     * @param src the Item which has changed
     */
    void itemStateChangedEvent(Item src) {
        try {
            synchronized (eventLock) {
                displayManager.callItemStateChanged(src);
            }
        } catch (Throwable t) {
            handleThrowable(t);
        }
    }

    /**
     * Handle an undefined VM Event. Designed for subclasses to
     * easily override and process new event types.
     *
     * @param event The unkown event type
     * @param queue The event queue to read subsequent values from
     *              pertaining to the event
     */
    void unknownVMEvent(int event, Events queue) {
        System.err.println("Unknown VM Event: " + event);
    }

    /**
     * Handle an unexpected exception while processing an event
     *
     * @param t the Throwable to handle
     */
    static void handleThrowable(Throwable t) {
        if (t != null) {
            System.err.println("\nError occurred while dispatching event:");
            t.printStackTrace();
        }
    }

// -------- Native Command Menu Handling Routines --------- //

    /**
     * Native method to draw the command menu on the screen
     */
    native void paintMenu();

    /**
     * Native method to dismiss the current menu in the case of setCurrent()
     * being called while the Display is suspended by a system screen.
     */
    native void dismissMenu();

    /**
     * Handle the key event when the menu is the current display.
     *
     * @param  type one of PRESSED, RELEASED, or REPEATED
     * @param  code the key code of the key event
     * @return true if the event is the current display is the menu screen.
     */
    native boolean menuKeyEvent(int type, int code);

    /**
     * Handle the pointer event when the menu is the current display.
     *
     * @param  type one of PRESSED, RELEASE, or DRAGGED
     * @param  x    the x co-ordinate of the pointer event
     * @param  y    the y co-ordinate of the pointer event
     * @return true if the event is the current display is the menu screen.
     */
    native boolean menuPointerEvent(int type, int x, int y);

    /**
     * Simulate an incoming phone call in the RI. This method
     * would be removed in an actual port. It exists solely to
     * simulate one possible behavior of a system whereby MIDP
     * needs to be suspended for the device to handle some task.
     */
    native void incomingCall();

// --------- Low Level VM Event Handler ------- //

    /**
     * The VMEventHandler opens a private stream from the
     * VM and marshals the events from the stream to the
     * event queue.
     */
    class VMEventHandler implements Runnable {
        /** The private stream of events from the VM */
        Events queue;

        /**
         * Signal this handler to continue on after waiting
         * for the event queue to process a pending event
         */
        public synchronized void proceed() {
            try {
                notify();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        /**
         * Process events from the VM event stream
         */
        public synchronized void run() {
            try {
                queue = new Events();

                // connect to the native event queue
                queue.open();
            } catch (Throwable t) {
                t.printStackTrace();
            }

            for (;;) {
                try {
                    // What this does is push each new event type
                    // to the queue and then stops. When the queue
                    // is processed, the remaining options associated
                    // with the event are read from the stream and
                    // then this thread is resumed to read the next
                    // new event type.
                    eventQueue.push(queue.readInt());

                    try {
                        wait();
                    } catch (Throwable t) {
                        // TO DO: Do something more useful with this
                        t.printStackTrace();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
             } // for
        } // run()
    } // VMEventHandler

// -------- High Level Profile Event Handler -------- //

    /**
     * The QueuedEventHandler processes all events,
     * those from the underlying VM as well as those
     * coming from the application and MIDP layers.
     */
    class QueuedEventHandler implements Runnable {

        /**
         * Create a new default QueuedEventHandler
         */
        public QueuedEventHandler() {
            eventQueue = new EventQueue();
        }

        /**
         * Signal this handler there is a need to process
         * a pending event.
         */
        public synchronized void process() {
            try {
                notify();
            } catch (Throwable t) {
                // TO DO: Do something more useful with this
                t.printStackTrace();
            }
        }

        /**
         * Waits for an event if there are no events pending.
         */
        public synchronized void tryToSleep() {
            if (eventQueue.nextScreen == null &&
                eventQueue.paintX1 == -1 &&
                    !eventQueue.callSeriallyPending &&
                        !eventQueue.invalidatePending &&
                            eventQueue.changedItem == null &&
                                eventQueue.vmEvent == 0) {
                try {
                    wait();
                } catch (Throwable t) {
                    // TO DO: Do something more useful with this
                    t.printStackTrace();
                }
            }
        }

        /**
         * Process events from the EventQueue
         */
        public void run() {
            int x1, y1, x2, y2, type = 0;
            Display parentOfNextScreen = null;
            Displayable nextScreen = null;
            boolean call = false;
            boolean validate = false;
            Item validateItem = null;
            Item changedItem = null;
            x1 = y1 = x2 = y2 = -1;
            Object target = null;

            for (;;) {

                try {
                    tryToSleep();

                    if (eventQueue.vmEvent != 0) {
                        switch (eventQueue.vmEvent) {
                        case KEY_EVENT:
                            type = vmEventHandler.queue.readInt();
                            if (type == IME) {
                                keyEvent(type,
                                         vmEventHandler.queue.readUTF(), 0);
                            } else {
                                keyEvent(type, null,
                                         vmEventHandler.queue.readInt());
                            }
                            break;

                        case PEN_EVENT:
                            pointerEvent(vmEventHandler.queue.readInt(),
                                         vmEventHandler.queue.readInt(),
                                         vmEventHandler.queue.readInt());
                            break;

                        case COMMAND_EVENT:
                            commandEvent(vmEventHandler.queue.readInt());
                            break;

                        case SYSTEM_EVENT:
                            systemEvent(vmEventHandler.queue.readInt());
                            break;

                        case MM_EOM_EVENT:
                            multiMediaEvent(vmEventHandler.queue.readInt(),
                                            vmEventHandler.queue.readInt());
                            break;

                        default:
                            unknownVMEvent(eventQueue.vmEvent,
                                           vmEventHandler.queue);
                        }

                        eventQueue.vmEvent = 0;
                        vmEventHandler.proceed();
                   }

                    synchronized (eventQueue.qLock) {
                        // We first check for a screen change.
                        if (eventQueue.nextScreen != null) {
                            parentOfNextScreen = eventQueue.parentOfNextScreen;
                            nextScreen = eventQueue.nextScreen;
                            eventQueue.nextScreen = null;
                            // On a screen change, we flush any pending
                            // repaints, since changing the screen will
                            // repaint the entire thing
                            eventQueue.paintX1 = eventQueue.paintY1 =
                                eventQueue.paintX2 = eventQueue.paintY2 = -1;
                            eventQueue.paintTarget = null;
                        } else if (eventQueue.paintX1 != -1) {
                            // We then check for a repaint
                            x1 = eventQueue.paintX1;
                            x2 = eventQueue.paintX2;
                            y1 = eventQueue.paintY1;
                            y2 = eventQueue.paintY2;
                            target = eventQueue.paintTarget;
                            eventQueue.paintX1 = eventQueue.paintY1 =
                                eventQueue.paintX2 = eventQueue.paintY2 = -1;
                            eventQueue.paintTarget = null;
                        }
                        // We last check for a callSerially()
                        if (eventQueue.callSeriallyPending) {
                            call = true;
                            eventQueue.callSeriallyPending = false;
                        }

                        if (eventQueue.invalidatePending) {
                            validate = true;
                            validateItem = eventQueue.invalidItem;
                            eventQueue.invalidItem = null;
                            eventQueue.invalidatePending = false;
                        }

                        if (eventQueue.changedItem != null) {
                            changedItem = eventQueue.changedItem;
                            eventQueue.changedItem = null;
                        }
                    }

                    if (nextScreen != null) {
                        screenChangeEvent(parentOfNextScreen,
                                          nextScreen);
                        parentOfNextScreen = null;
                        nextScreen = null;
                    }
                    if (x1 != -1) {
                        repaintScreenEvent(x1, y1, x2, y2, target);
                        x1 = y1 = x2 = y2 = -1;
                        target = null;
                    }
                    if (call) {
                        callSeriallyEvent();
                        call = false;
                    }
                    if (validate) {
                        validateEvent(validateItem);
                        validateItem = null;
                        validate = false;
                    }
                    if (changedItem != null) {
                        itemStateChangedEvent(changedItem);
                        changedItem = null;
                    }

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } // for (;;)
        } // run()
    } // QueuedEventHandler()

    /**
     * The EventQueue queues all event-related data and notifies
     * the QueuedEventHandler when events need processing.
     */
    class EventQueue {
        /** The latest vm event */
        int vmEvent;
        /** The parent Display of the next Displayable. */
        Display parentOfNextScreen;
        /** The next displayable to show */
        Displayable nextScreen;
        /** A flag to process all call serially's */
        boolean callSeriallyPending;
        /** A flag to perform an invalidation of a Form */
        boolean invalidatePending;
        /** An Item can be the cause of an invalidation */
        Item invalidItem;
        /** An Item whose state has changed */
        Item changedItem;
        /** The dirty region for any pending repaint */
        int paintX1, paintY1, paintX2, paintY2;
        /** The optional target for the repaint */
        Object paintTarget;
        /** The lock for manipulating queue data */
        Object qLock;

        /**
         * Create a new default EventQueue
         */
        public EventQueue() {
            qLock = new Object();
            paintX1 = paintY1 = paintX2 = paintY2 = -1;
        }

        /**
         * Service any pending repaints. If there is a pending
         * repaint, process it immediately, otherwise return.
         */
        public void serviceRepaints() {
            int x1, y1, x2, y2;
            Object target;

            synchronized (qLock) {
                if (paintX1 == -1) {
                    return;
                }
                x1 = paintX1;
                y1 = paintY1;
                x2 = paintX2;
                y2 = paintY2;
                target = paintTarget;
                paintX1 = paintY1 = paintX2 = paintY2 = -1;
                paintTarget = null;
            }

            repaintScreenEvent(x1, y1, x2, y2, target);
        }

        /**
         * Push an event from the vm event stream
         *
         * @param vmEvent The integral vm event value
         */
        public void push(int vmEvent) {
            synchronized (qLock) {
                this.vmEvent = vmEvent;
            }
            queuedEventHandler.process();
        }

        /**
         * Push a callSerially event
         */
        public void push() {
            synchronized (qLock) {
                callSeriallyPending = true;
            }
            queuedEventHandler.process();
        }

        /**
         * Push either a Form invalidation event, or an
         * ItemStateChanged event
         *
         * @param src the Item src to the event
         * @param b true if it is an invalidation event
         */
        public void push(Item src, boolean b) {
            synchronized (qLock) {
                if (b) {
                    invalidatePending = true;
                    invalidItem = src;
                } else {
                    changedItem = src;
                }
            }
            queuedEventHandler.process();
        }

        /**
         * Push a screen change
         *
         * @param parent parent Display of the Displayable
         * @param d The Displayable to change the current screen to
         */
        public void push(Display parent, Displayable d) {
            synchronized (qLock) {
                parentOfNextScreen = parent;
                nextScreen = d;
            }
            queuedEventHandler.process();
        }

        /**
         * Push a repaint
         *
         * @param x The x origin coordinate
         * @param y The y origin coordinate
         * @param w The width
         * @param h The height
         * @param target The optional paint target
         */
        public void push(int x, int y, int w, int h, Object target) {

            try {
                w += x; // convert from width, height to absolute
                h += y; //  x2, y2
		if (x < 0) x = 0;
		if (y < 0) y = 0;

                synchronized (qLock) {
                    if (paintX1 == -1) {
                        // If we have no pending repaint
                        // just store the region
			paintX1 = x;
			paintY1 = y;
			paintX2 = w;
			paintY2 = h;
			paintTarget = target;
                    } else {
                        // If there is a pending repaint
                        // union the dirty regions
                        if (paintX1 > x) {
                            paintX1 = x;
                        }
                        if (paintY1 > y) {
                            paintY1 = y;
                        }
                        if (paintX2 < w) {
                            paintX2 = w;
                        }
                        if (paintY2 < h) {
                            paintY2 = h;
                        }
                        paintTarget = null;
                    }
                } // synchronized
            } catch (Throwable t) {
                t.printStackTrace();
            }
            queuedEventHandler.process();
        }

    } // ProfileEventHandler
}
