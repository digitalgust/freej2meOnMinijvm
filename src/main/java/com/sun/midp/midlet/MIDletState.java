/*
 * @(#)MIDletState.java	1.19 02/09/11 @(#)
 *
 * Copyright (c) 1998-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midlet;

import javax.microedition.lcdui.Display;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.midp.lcdui.DisplayAccess;
import com.sun.midp.lcdui.DisplayManager;
import com.sun.midp.lcdui.DisplayManagerFactory;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * MIDletState holds the current state of the MIDlet and forwards
 * updates to it.  It holds the reference to the MIDlet itself
 * and the Display object used to communicate with the MIDlet's
 * Display.
 * <p>
 * When each MIDlet is constructed it creates a MIDletProxy object
 * (subclassed from MIDletState).  The MIDletProxy is in the
 * javax.microedition.midlet package so that it can invoke the
 * control methods of MIDlets (startApp, destroyApp, pauseApp).
 * Invocations from the Scheduler to the MIDlet are forwarded using 
 * corresponding methods.
 * <p>
 * All state changes are synchronized using the mutex retrieved from
 * the Scheduler. 
 * NotifyPaused, ResumeRequest, and NotifyDestroyed methods invoked on the
 * MIDlet cause the appropriate state change.  The Scheduler is aware
 * of changes by waiting on the mutex.
 * <p>
 * The getAppProperty method from the MIDlet is sent here and
 * relayed to the Scheduler.
 */

public abstract class MIDletState {

    /*
     * Implementation state; the states are in priority order.
     * That is, a higher number indicates a preference to be
     * selected for scheduling sooner.  This allows the scheduler
     * to make one pass over the known MIDlets and pick the 
     * "best" MIDlet to schedule.
     */

    /**
     * State of the MIDlet is Paused; it should be quiescent
     */
    static final int PAUSED = 0;

    /**
     * State of the MIDlet is Active
     */
    static final int ACTIVE = 1;

    /**
     * State of the MIDlet is Paused but Resume has been requested
     */
    static final int PAUSED_RESUME = 2;

    /**
     * State of the MIDlet when resumed by the display manager
     */
    static final int ACTIVE_PENDING = 3;

    /**
     * State of the MIDlet when paused by the display manager
     */
    static final int PAUSE_PENDING = 4;

    /**
     * State of the MIDlet with destroy pending
     */
    static final int DESTROY_PENDING = 5;

    /**
     * State of the MIDlet is Destroyed
     */
    static final int DESTROYED = 6;

    /** This class has a different security domain than the application. */
    private static SecurityToken classSecurityToken;

    /** Lock for creating a MIDlet. */
    private static Object createMIDletLock = new Object();

    /** Lock for creating a MIDlet, default to false. */
    private static boolean allowedToCreateMIDlet;

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class
     */
    public static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken != null) {
            return;
        }

        classSecurityToken = token;
    }

    /**
     * Create a MIDlet without throwing a security exception.
     *
     * @param classname name of MIDlet class
     *
     * @return newly created MIDlet
     *
     * @exception ClassNotFoundException is thrown, if the MIDlet main class is
     * not found
     * @exception InstantiationException is thrown, if the MIDlet can not be 
     * created
     * @exception IllegalAccessException is thrown, if the MIDlet is not 
     * permitted to perform a specific operation
     */
    static MIDlet createMIDlet(String classname) throws
           ClassNotFoundException, InstantiationException,
           IllegalAccessException {
        Class midletClass;
        Object midlet;

        synchronized (createMIDletLock) {
            try {
                allowedToCreateMIDlet = true;

                midletClass = Class.forName(classname);
                midlet = midletClass.newInstance();
                if (midlet instanceof MIDlet) {
                    return (MIDlet)midlet;
                }

                throw new InstantiationException("Class not a MIDlet");
            } finally {
                allowedToCreateMIDlet = false;
            }
        }
    }

    /**
     * The applications current state.
     */
    private int state;

    /**
     * The lock for changes to the state.
     */
    private Object mutex;

    /**
     * The controller of MIDlets.
     */
    private Scheduler scheduler;

    /**
     * The MIDlet for which this is the state.
     */
    protected MIDlet midlet;	

    /** 
     * The Display for this MIDlet.
     */
    protected Display display;

    /** 
     * The controller of Displays.
     */
    protected DisplayManager displayManager;

    /**
     * Protected constructor for subclasses.
     * If any MIDlet is constructed it should be registered
     * with Scheduler. That allows them to be managed even if
     * an application creates one itself.
     *
     * @param m the MIDlet this is the state for;
     * Must not be <code>null</code>.
     *
     * @exception SecurityException if is constructor is not being called in
     * the context of <code>createMIDlet</code> and the suite does not
     * have the AMS permission.
     */
    protected MIDletState(MIDlet m) {
        DisplayAccess accessor;

	midlet = m;
	state = PAUSED_RESUME;	// So it will be made active soon
	scheduler = Scheduler.getScheduler();
	mutex = scheduler.getMutex();

        synchronized (createMIDletLock) {
            if (!allowedToCreateMIDlet) {
                MIDletSuite suite = scheduler.getMIDletSuite();

                if (suite != null) {
                    suite.checkIfPermissionAllowed(Permissions.AMS);
                }
            }
        }

	// Force the creation of the Display
        displayManager = DisplayManagerFactory.getDisplayManager();
        accessor = displayManager.createDisplay(classSecurityToken, midlet);
        display = accessor.getDisplay();

        if (scheduler.getMIDletSuite().isTrusted()) {
            accessor.setTrustedIcon(classSecurityToken, true);
        }
    }

    /**
     * Get the MIDlet for which this holds the state.
     *
     * @return the MIDlet; will not be null.
     */
    public MIDlet getMIDlet() {
	return midlet;
    }

    /**
     * Get the Display for this MIDlet.
     *
     * @return the Display of this MIDlet.
     */
    public Display getDisplay() {
	return display;
    }

    /**
     * Signals the <code>MIDlet</code> to start and enter the <i>ACTIVE</i>
     * state.
     * In the <i>ACTIVE</I> state the <code>MIDlet</code> may hold resources.
     * The method will only be called when
     * the <code>MIDlet</code> is in the <i>PAUSED</i> state.
     * <p>
     * Two kinds of failures can prevent the service from starting,
     * transient and non-transient.  For transient failures the
     * <code>MIDletStateChangeException</code> exception should be thrown.
     * For non-transient failures the <code>notifyDestroyed</code>
     * method should be called.
     *
     * @exception MIDletStateChangeException  is thrown
     * if the <code>MIDlet</code> cannot start now but might be able to
     * start at a later time.
     */
    protected abstract void startApp() throws MIDletStateChangeException;

    /**
     *
     * Signals the <code>MIDlet</code> to stop and enter the <i>PAUSED</i>
     * state.
     * In the <i>PAUSED</i> state the <code>MIDlet</code> must release shared
     * resources
     * and become quiescent. This method will only be called
     * called when the <code>MIDlet</code> is in the <i>ACTIVE</i> state. <p>
     *
     */
    protected abstract void pauseApp();


    /**
     * Signals the <code>MIDlet</code> to terminate and enter the
     * <i>DESTROYED</i> state.
     * In the destroyed state the <code>MIDlet</code> must release
     * all resources and save any persistent state. This method may
     * be called from the <i>PAUSED</i> or
     * <i>ACTIVE</i> states. <p>
     * <code>MIDlet</code>s should
     * perform any operations required before being terminated, such as
     * releasing resources or saving preferences or
     * state. <p>
     *
     * <b>NOTE:</b> The <code>MIDlet</code> can request that it not enter
     * the <i>DESTROYED</i>
     * state by throwing an <code>MIDletStateChangeException</code>. This
     * is only a valid response if the <code>unconditional</code>
     * flag is set to <code>false</code>. If it is <code>true</code>
     * the <code>MIDlet</code> is assumed to be in the <i>DESTROYED</i>
     * state regardless of how this method terminates. If it is not an
     * unconditional request, the <code>MIDlet</code> can signify that it
     * wishes
     * to stay in its current state by throwing the 
     * <code>MIDletStateChangeException</code>.
     * This request may be honored and the <code>destroy()</code>
     * method called again at a later time.
     *
     * @param unconditional If true when this method is called,
     * the <code>MIDlet</code> must cleanup and release all resources.
     * If false the <code>MIDlet</code> may throw 
     * <CODE>MIDletStateChangeException</CODE>
     * to indicate it does not want to be destroyed at this time.
     *
     * @exception MIDletStateChangeException is thrown if the
     * <code>MIDlet</code> wishes to continue to execute 
     * (Not enter the <i>DESTROYED</i> state).
     * This exception is ignored if <code>unconditional</code>
     * is equal to <code>true</code>.
     */
    protected abstract void destroyApp(boolean unconditional)
	throws MIDletStateChangeException;


    /**
     *
     * Used by a <code>MIDlet</code> to notify the application management
     * software that it has entered into the
     * <i>DESTROYED</i> state.  The application management software will not
     * call the MIDlet's <code>destroyApp</code> method, and all resources
     * held by the <code>MIDlet</code> will be considered eligible for
     * reclamation.
     * The <code>MIDlet</code> must have performed the same operations
     * (clean up, releasing of resources etc.) it would have if the
     * <code>MIDlet.destroyApp()</code> had been called.
     *
     */
    public final void notifyDestroyed() {
	synchronized (mutex) {
	    state = DESTROYED;
	    mutex.notify();
	}
    }

    /**
     * Used by a <code>MIDlet</code> to notify the application management
     * software that it has entered into the <i>PAUSED</i> state.
     * Invoking this method will
     * have no effect if the <code>MIDlet</code> is destroyed,
     * or if it has not yet been started. <p>
     * It may be invoked by the <code>MIDlet</code> when it is in the
     * <i>ACTIVE</i> state. <p>
     *
     * If a <code>MIDlet</code> calls <code>notifyPaused()</code>, in the
     * future its <code>startApp()</code> method may be called make
     * it active again, or its <code>destroyApp()</code> method may be
     * called to request it to destroy itself.
     */
    public final void notifyPaused() {
        int oldState;

	synchronized (mutex) {
            oldState = state;

            // do not notify the scheduler, since there is nothing to do
            setStateWithoutNotify(PAUSED);
	}

        // do work outside of the mutex
        if (oldState == ACTIVE) {
            displayManager.deactivate(getMIDlet());
        }
    }

    /**
     * Provides a <code>MIDlet</code> with a mechanism to retrieve
     * <code>MIDletSuite</code> for this MIDlet.
     *
     * @return MIDletSuite for this MIDlet
     */
    public final MIDletSuite getMIDletSuite() {
	return scheduler.getMIDletSuite();
    }

    /**
     * Used by a <code>MIDlet</code> to notify the application management
     * software that it is
     * interested in entering the <i>ACTIVE</i> state. Calls to
     * this method can be used by the application management software to
     * determine which applications to move to the <i>ACTIVE</i> state.
     * <p>
     * When the application management software decides to activate this  
     * application it will call the <code>startApp</code> method.
     * <p> The application is generally in the <i>PAUSED</i> state when this is
     * called.  Even in the paused state the application may handle
     * asynchronous events such as timers or callbacks.
     */

    public final void resumeRequest() {
        setState(PAUSED_RESUME);
    }

    /**
     * Requests that the device handle (e.g. display or install)
     * the indicated URL.
     *
     * <p>If the platform has the appropriate capabilities and
     * resources available, it SHOULD bring the appropriate
     * application to the foreground and let the user interact with
     * the content, while keeping the MIDlet suite running in the
     * background. If the platform does not have appropriate
     * capabilities or resources available, it MAY wait to handle the
     * URL request until after the MIDlet suite exits. In this case,
     * when the requesting MIDlet suite exits, the platform MUST then
     * bring the appropriate application to the foreground to let the
     * user interact with the content.</p>
     *
     * <p>This is a non-blocking method. In addition, this method does
     * NOT queue multiple requests. On platforms where the MIDlet
     * suite must exit before the request is handled, the platform
     * MUST handle only the last request made. On platforms where the
     * MIDlet suite and the request can be handled concurrently, each
     * request that the MIDlet suite makes MUST be passed to the
     * platform software for handling in a timely fashion.</p>
     *
     * <p>If the URL specified refers to a MIDlet suite (either an
     * Application Descriptor or a JAR file), the request is
     * interpreted as a request to install the named package. In this
     * case, the platform's normal MIDlet suite installation process
     * SHOULD be used, and the user MUST be allowed to control the
     * process (including cancelling the download and/or
     * installation). If the MIDlet suite being installed is an
     * <em>update</em> of the currently running MIDlet suite, the
     * platform MUST first stop the currently running MIDlet suite
     * before performing the update. On some platforms, the currently
     * running MIDlet suite MAY need to be stopped before any
     * installations can occur.</p>
     *
     * <p>If the URL specified is of the form
     * <code>tel:&lt;number&gt;</code>, as specified in <a
     * href="http://rfc.net/rfc2806.html">RFC2806</a>, then the
     * platform MUST interpret this as a request to initiate a voice
     * call. The request MUST be passed to the &quot;phone&quot;
     * application to handle if one is present in the platform.</p>
     *
     * <p>Devices MAY choose to support additional URL schemes beyond
     * the requirements listed above.</p>
     *
     * <p>Many of the ways this method will be used could have a
     * financial impact to the user (e.g. transferring data through a
     * wireless network, or initiating a voice call). Therefore the
     * platform MUST ask the user to explicitly acknowlege each
     * request before the action is taken. Implementation freedoms are
     * possible so that a pleasant user experience is retained. For
     * example, some platforms may put up a dialog for each request
     * asking the user for permission, while other platforms may
     * launch the appropriate application and populate the URL or
     * phone number fields, but not take the action until the user
     * explicitly clicks the load or dial buttons.</p>
     *
     * @return true if the MIDlet suite MUST first exit before the
     * content can be fetched.
     *
     * @param URL The URL for the platform to load.
     *
     * @exception ConnectionNotFoundException if
     * the platform cannot handle the URL requested.
     *
     * @since MIDP 2.0
     */
    public native final boolean platformRequest(String URL);

    /**
     * Change the state and notify.
     * Check to make sure the new state makes sense.
     * Changes to the status are protected by the mutex.
     * Any change to the state notifies the mutex.
     *
     * @param newState new state of the MIDlet
     */
    void setState(int newState) {
	synchronized (mutex) {
            setStateWithoutNotify(newState);
            mutex.notify();
	}
    }

    /**
     * Get the status of the specified permission.
     * If no API on the device defines the specific permission 
     * requested then it must be reported as denied.
     * If the status of the permission is not known because it might
     * require a user interaction then it should be reported as unknown.
     *
     * @param permission to check if denied, allowed, or unknown.
     * @return 0 if the permission is denied; 1 if the permission is allowed;
     * 	-1 if the status is unknown
     */
    public int checkPermission(String permission) {
        return getMIDletSuite().checkPermission(permission);
    }        

    /**
     * Change the state without notifing the scheduler.
     * Check to make sure the new state makes sense.
     * <p>
     * To be called only by the scheduler or MIDletState while holding
     * the scheduler mutex.
     *
     * @param newState new state of the MIDlet
     */
    void setStateWithoutNotify(int newState) {
        switch (state) {
        case DESTROYED:
            // can't set any thing else
            return;
        
        case DESTROY_PENDING:
            if (newState != DESTROYED) {
                // can only set DESTROYED
                return;
            }
            
            break;

        case PAUSED:
            if (newState == PAUSE_PENDING) {
                // already paused by app
                return;
            }

            break;

        case ACTIVE:
            if (newState == PAUSED_RESUME || newState == ACTIVE_PENDING) {
                // already active
                return;
            }
        }

        state = newState;
    }

    /**
     * Get the state.
     *
     * @return current state of the MIDlet.
     */
    int getState() {
	synchronized (mutex) {
	    return state;
	}
    }
}
