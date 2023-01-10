/*
 * @(#)Scheduler.java	1.65 02/10/15 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midlet;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.midp.lcdui.DisplayManager;
import com.sun.midp.lcdui.DisplayManagerFactory;
import com.sun.midp.lcdui.SystemEventListener;
import com.sun.midp.lcdui.MIDletEventListener;

import com.sun.midp.main.Configuration;

import com.sun.midp.security.Permissions;

/**
 * The Scheduler starts and controls MIDlets through the lifecycle states.
 * MIDlets are created using its no-arg Constructor. Once created 
 * it is sequenced through the start, paused, and destroy states.
 * <p>
 * The Scheduler is a singleton. The Scheduler is retrieved with
 * getScheduler().  The Scheduler can be replaced by subclassing.
 * Events that affect MIDlet scheduling should notify the Scheduler.
 * Typical events are MIDlet creation, changing of MIDlet states, etc.
 * <p>
 * To replace the Scheduler, subclass it and then set the 
 * "com.sun.midp.midlet.scheduler" property to the name of the subclass.
 * When the Scheduler is first retrieved, the property is checked and the
 * instance created.  If left unset the Scheduler class is used unchanged.
 * <p>
 * The MIDlet methods are protected in the javax.microedition.midlet package
 * so the Scheduler can not call them directly.  The MIDletState object and
 * MIDletProxy subclass class allow the Scheduler to hold the state of the
 * MIDlet and to invoke methods on it.  The MIDletState instance is created
 * by the MIDlet when it is constructed. 
 * <p>
 * This default implementation of the Scheduler introduces a couple
 * of extra internally only visible states to allow processing of MIDlet
 * requested state changes in the control thread and serialized with 
 * other state changes.  The additional states are:
 * <UL>
 * <LI> <code>PAUSED_RESUME</code> - Similar to PAUSED but also indicates
 * that this MIDlet has requested to be resumed.
 * <LI> <code>DESTROY_PENDING</code> - Indicates that the MIDlet needs
 * to be DESTROYED. The MIDlet's destroyApp has not yet been called.
 * </UL>
 * The Scheduler loops, looking for MIDlets that require state changes
 * and making the requested changes including calling methods in
 * the MIDlet to make the change.
 * <p>
 * When a MIDlet is started, put in <code>ACTIVE</code> state, its display
 * is activated by the scheduler using the display manager. When a MIDlet is
 * destroyed its display is deactivated. When MIDlet is paused by the MIDlet
 * itself, its display is deactivated and it will not be restarted unless the
 * MIDlet requests itself to be restarted. When a MIDlet is paused by a UI
 * event from the display manager the display of the MIDlet is
 * not deactivated but suspended, which means it can be resumed by
 * system event such as a user selection.
 * <p>
 * It is possible that as far as the Scheduler is concerned
 * there is no <code>ACTIVE</code> MIDlet. The only case in which this should 
 * arise is when all MIDlets are PAUSED or DESTROYED. From the user
 * point of view this is quite bad. This case is the same as all off the
 * active MIDlets not have a current displayable. So display manager will
 * need to "default screen". Later when multiple MIDlet suites can be run,
 * the VM will run in the background and the the application selector will
 * run.
 * <p>
 * For the specific handling of events received  from the display manager
 * see the documenation for implemenation each of listener methods.
 *
 * @see MIDlet
 * @see MIDletState
 * @see DisplayManager
 */

public class Scheduler implements SystemEventListener, MIDletEventListener {
    /** the current MIDlet suite */
    private MIDletSuite midletSuite;
    /** array of MIDlets */
    private MIDletState[] midlets;
    /** current number of MIDlets [0..n-1] */
    private int nmidlets;       
    /**  next index to be scanned by selectForeground */
    private int scanIndex;      
    /** display manager */
    private DisplayManager displayManager;
    /** Flag to indicate if the system was shutdown. */
    private boolean systemShutdown;

    /** used to wait for MIDlet state changes */
    private static Object mutex; 
    /** the manager of all MIDlets */
    private static Scheduler scheduler;
    /** the thread on which the scheduler is active */
    private Thread schedulerThread;

    /**
     * Construct a new Scheduler object.
     */
    protected Scheduler() {
        /*
         * The fact that there is only one scheduler is a security feature.
         * Multiple schedulers are allow in the future, the constructor
         * should require security domain parameter and it should be
         * checked.
         */
        if (scheduler != null) {
            throw new Error("Only one scheduler instance allowed");
        }
        
        mutex = new Object();
        nmidlets = 0;

        // start with 5 empty slots, we will add more if needed
        midlets = new MIDletState[5];
    }

    /**
     * Get the Scheduler that manages the lifecycle states MIDlets.
     * If the instance of the Scheduler has already been created
     * it is returned.  If not it is created from the value of 
     * the "com.sun.midp.midlet.scheduler" property.  If that property
     * is null the default scheduler (this class) is instantiated.
     * The instance becomes the Scheduler for this process
     *
     * @return the MIDlet management software scheduler
     */
    public static synchronized Scheduler getScheduler() {
        /*
         * If not scheduler has been created, create one now.
         * If the scheduler class has been overridden use it.
         */
        if (scheduler == null) {
            String prop =
                Configuration.getProperty("com.sun.midp.midlet.scheduler");
            if (prop != null) {
                Class c;
                try {
                    c =  Class.forName(prop);
                } catch (Throwable ex) {
                    throw new Error("A scheduler class cannot be loaded " +
                                    "from the value of property " +
                                    "com.sun.midp.midlet.scheduler");
                }

                try {
                    scheduler = (Scheduler)c.newInstance();
                } catch (Throwable ex) {
                    throw new Error("The construction of " + prop +
                                    " instance failed");
                }
            } else {
                /* This is the default scheduler class */
                scheduler = new Scheduler();
            }
        }

        return scheduler;
    }

    /**
     * Get the display manager.
     *
     * @return the display manager
     */
    private DisplayManager getDisplayManager() {
        if (displayManager == null) {
            displayManager = DisplayManagerFactory.getDisplayManager();
        }
        
        return displayManager;
    }

    /**
     * Schedule a MIDlet from outside of the package.
     *
     * @param midlet to be registered with this scheduler
     *
     * @exception SecurityException if the suite does not have the
     *   AMS permission.
     */
    public void scheduleMIDlet(MIDlet midlet) {
        midletSuite.checkIfPermissionAllowed(Permissions.AMS);

        register(midlet);
    }

    /**
     * Register a MIDlet being constructed.
     *
     * @param midlet to be registered with this scheduler
     */
    protected void register(MIDlet midlet) {
        synchronized (mutex) {
            MIDletState state = MIDletStateMap.getState(midlet);

            /*
             * If a MIDlet of the same class is already running
             * Make the existing MIDlet current so that schedule() will run it
             */
            int i = findMIDletByClass(state);
            if (i >= 0) {
                state.setState(MIDletState.DESTROY_PENDING);
                // Fall into adding it to the list so destroyApp
                // can be called at a reasonable time.
            }

            // Grow the list if necessary
            if (nmidlets >= midlets.length) {
                MIDletState[] n = new MIDletState[nmidlets+5];
                System.arraycopy(midlets, 0, n, 0, nmidlets);
                midlets = n;
            }

            // Add it to the end of the list
            midlets[nmidlets++] = state;

            mutex.notify();
        }
    }

    /**
     * Return the mutex for synchronization and notification of changes 
     * to a MIDlets state.
     * It is used by MIDlets when they are changing states.
     * It is the mutex used for all state changes.
     * @return mutex object for synchronizing this scheduler
     */
    protected Object getMutex() {
        return mutex;
    }

    /**
     * Provides a <code>MIDletProxy</code> with a mechanism to retrieve
     * <code>MIDletSuite</code> being scheduled.
     *
     * @return MIDletSuite being scheduled
     */
    public MIDletSuite getMIDletSuite() {
        return midletSuite;
    }

    /**
     * Returns true if the current thread is the Scheduler's thread.
     *
     * @return true if the current thread is this Scheduler's thread
     */
    public boolean isDispatchThread() {
        // NOTE: This will have to be removed when Display
        // changes queued in the event handler
        return (Thread.currentThread() == schedulerThread);
    }

    /**
     * Run MIDlets until there are none.
     * Handle any pending state transitions of any MIDlet.
     * If there are none, wait for transitions.
     * If there is no foreground MIDlet select one that is ACTIVE and
     * has setCurrent != null.
     * <p>
     * If the foreground MIDlet changes from the ACTIVE_FOREGROUND state
     * it automatically looses the foreground and and new one is selected.
     *
     * @param aMidletSuite the current midlet suite
     *
     * @return true if a method ended because the last MIDlet was destroyed
     * normally,  false if the system is shutting down.
     *
     * @exception ClassNotFoundException is thrown, if the MIDlet main class is
     * not found
     * @exception InstantiationException is thrown, if the MIDlet can not be 
     * created
     * @exception IllegalAccessException is thrown, if the MIDlet is not 
     * permitted to perform a specific operation
     */
    public boolean schedule(MIDletSuite aMidletSuite) throws
           ClassNotFoundException, InstantiationException,
           IllegalAccessException {

        if (midletSuite != null) {
            throw new RuntimeException(
                "There is already a MIDlet Suite scheduled.");
        
	}

        // NOTE: This will have to be removed when Display
        // changes queued in the event handler
        schedulerThread = Thread.currentThread();

	getDisplayManager().addSystemEventListener(this);

        midletSuite = aMidletSuite;
        
        register(
           MIDletState.createMIDlet(midletSuite.getInitialMIDletClassname()));

        /*
         * Until there are no MIDlets
         * Scan all the MIDlets looking for state changes.
         */
        while (nmidlets > 0) {
            try {
                MIDletState curr;
                int state;

                synchronized (mutex) {
                    /*
                     * Find the highest priority state of any MIDlet and
                     * process, but do not hold the lock while processing
                     * to avoid deadlocks with LCDUI and event handling.
                     * Perform state changes with a lock so
                     * no state changes are lost.
                     */
                    curr = selectByPriority();
                    state = curr.getState();

                    switch (state) {
                    case MIDletState.ACTIVE:
                        // fall through
                    case MIDletState.PAUSED:
                        // Wait for some change in the state of a MIDlet
                        // that needs attention
                        try {
                            mutex.wait();
                        } catch (InterruptedException e) {
                        }

                        continue;

                    case MIDletState.PAUSED_RESUME:
                        // fall through
                    case MIDletState.ACTIVE_PENDING:
                        // Start the MIDlet
                        curr.setStateWithoutNotify(MIDletState.ACTIVE);
                        break;

                    case MIDletState.PAUSE_PENDING:
                        // The display manager wants the MIDlet paused
                        curr.setStateWithoutNotify(MIDletState.PAUSED);
                        break;

                    case MIDletState.DESTROY_PENDING:
                        curr.setStateWithoutNotify(MIDletState.DESTROYED);
                        break;

                    case MIDletState.DESTROYED:
                        unregister(curr);
                        break;
                        
                    default:
                        throw new Error("Illegal MIDletState state " +
                                    curr.getState());
                    }
                }

                /* perform work that may block outside of the mutex. */
                switch (state) {
                case MIDletState.PAUSED_RESUME:
                    getDisplayManager().activate(this, curr.getMIDlet());
                    
                    // fall through
                case MIDletState.ACTIVE_PENDING:
                    try {
                        curr.startApp();
                    } catch (Exception ex) {
                        printException("startApp threw an Exception", ex);
                        curr.setState(MIDletState.DESTROY_PENDING);
                    }
                    break;

                case MIDletState.PAUSE_PENDING:
                    try {
                        curr.pauseApp();
                    } catch (Exception ex) {
                        printException("pauseApp threw an Exception", ex);
                        curr.setState(MIDletState.DESTROY_PENDING);
                    }
                    
                    break;

                case MIDletState.DESTROY_PENDING:
                    // If the MIDlet is in the DESTROY_PENDING state
                    // call its destroyApp method to clean it up.
                    try {
                        // Tell the MIDlet to cleanup.
                        curr.destroyApp(true);
                    } catch (MIDletStateChangeException ex) {
                        // Ignore the exception
                    } catch (Exception ex) {
                        printException("destroyApp threw an Exception",
                                       ex);
                    }

                    break;

                case MIDletState.DESTROYED:
                    getDisplayManager().deactivate(curr.getMIDlet());
                    break;
                }
            } catch (Exception ex) {
                System.out.println("Exception in schedule");
                ex.printStackTrace();
            }
        }

        midletSuite.saveSettings();
        midletSuite = null;
        getDisplayManager().releaseSystemEventListener(this);

        return !systemShutdown;
    }

    /**
     * Shutdown all running MIDlets and prepare the MIDP runtime
     * to exit completely.
     */
    public void shutdown() {
        synchronized (mutex) {
            systemShutdown = true;

            for (int i = 0; i < nmidlets; i++) {
                if (midlets[i].getState() != MIDletState.DESTROYED) {
                    midlets[i].
                        setStateWithoutNotify(MIDletState.DESTROY_PENDING);
                }
            }

            mutex.notify();
        }
    }

    /**
     * Check if the named <code>MIDlet</code> has already been instantiated.
     * @param name class name of <code>MIDlet</code> to test if 
     *             currently scheduled
     * @return <code>true</code> if an instance of the MIDlet is already
     *     running
     */
    public boolean isScheduled(String name) {
	boolean found = false;
	synchronized (mutex) {
            for (int i = 0; i < nmidlets; i++) {
                if (midlets[i].getMIDlet().getClass().getName().equals(name)) {
		    found = true;
		    break;
                }
            }
        }
	return found;
    }

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
    public void pauseMIDlet(MIDlet midlet) {
        MIDletState state = MIDletStateMap.getState(midlet);
        state.setState(MIDletState.PAUSE_PENDING);
    }
 
    /**
     * Start the currently suspended state. This is a result
     * of the underlying system returning control to MIDP.
     * Any previously paused foreground MIDlet will be restarted
     * and the Display will be refreshed. The listener should not activate
     * the display of the MIDlet since this will be done automatically.
     *
     * @param midlet midlet that the event applies to
     */
    public void startMIDlet(MIDlet midlet) {
        MIDletState state = MIDletStateMap.getState(midlet);
        state.setState(MIDletState.ACTIVE_PENDING);
    }
 
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
    public void destroyMIDlet(MIDlet midlet) {
        MIDletState state = MIDletStateMap.getState(midlet);
        state.setState(MIDletState.DESTROY_PENDING);
    }

    /**
     * Look through the current MIDlets and select one to
     * be processed.
     * <p>Note: that this method is called while synchronized on "mutex".
     * @return the MIDlet to process next
     */
    private MIDletState selectByPriority() {
        MIDletState found = null; // Chosen MIDletState
        int state = -1;         // the state of the chosen MIDlet

        /*
         * Find the most desirable MIDlet based on its state
         * The higher state values are preferred because they
         * are needed for cleanup.
         */
        for (int i = nmidlets-1; i >= 0; i--) {

            // make sure index is inside current array, favoring the end
            if (scanIndex < 0 || scanIndex >= nmidlets)
                scanIndex = nmidlets-1;

            // Pick this MIDlet if the state is higher priority
            int s = midlets[scanIndex].getState();
            if (s > state) {
                found = midlets[scanIndex];
                state = s;
            }
            scanIndex--;
        }
        return found;
    }

    /**
     * Remove a MIDlet from the list if it is there,
     * otherwise ignore the request.
     * Call only while synchronized on mutex.
     * @param m the MIDlet to remove
     */
    private void unregister(MIDletState m) {
        // Find it in the list and switch the last one for it.
        for (int i = 0; i < nmidlets; i++) {
            if (m == midlets[i]) {
                // Switch the last MIDlet into that offset.
                midlets[i] = midlets[nmidlets-1];

                // null out from array and remove from map to allow for GC
                midlets[--nmidlets] = null;
                break;
            }
        }
    }

    /**
     * Find a MIDlet in the list by it class.
     * Only a single MIDlet of a class can be active at
     * a time. 
     * Must be called synchronized on the mutex.
     * @param m the MIDlet to find
     * @return the index in the array of MIDlets.
     *  return -1 if the MIDlet is not found.
     */
    private int findMIDletByClass(MIDletState m) {
        // Find it in the list
        for (int i = 0; i < nmidlets; i++) {
            if (m.getMIDlet().getClass() ==
                midlets[i].getMIDlet().getClass()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Print an exception with a reason.
     * Should be logged instead.
     * @param msg string to associate with the current exception
     * @param ex the exception to be reported 
     */
    private static void printException(String msg, Exception ex) {
        try {
            System.out.println(msg);
            if (ex.getMessage() == null) {
                System.out.println(ex);
            }
            ex.printStackTrace();
        } catch (Exception e) {
        }
    }
}
