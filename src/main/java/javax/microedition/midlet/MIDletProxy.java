/*
 * @(#)MIDletProxy.java	1.5 02/07/24 @(#)
 *
 * Copyright (c) 1998-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.midlet;

import com.sun.midp.midlet.MIDletState;
import com.sun.midp.midlet.MIDletStateMap;

/**
 * a proxy object that extends the state of the MIDlet. This class provides
 * access to startApp,pauseApp and destroyApp methods.
 */

class MIDletProxy extends MIDletState {
    /** Sets up the MIDlet state map. */
    static {
        MIDletStateMap.setMapImpl(new MIDletStateMapImpl());
    }

    /**
     * Protected constructor for subclasses.
     * If any MIDlet is constructed it should be registered
     * with Scheduler. That will allow them to be managed even if
     * the application creates them itself.
     * @param m the MIDlet that will be accessed from this proxy object.
     */
    MIDletProxy(MIDlet m) {
	super(m);
    }

    /**
     * Forwards startApp method to the MIDlet from the scheduler.
     *
     * @exception <code>MIDletStateChangeException</code>  is thrown if the
     *		<code>MIDlet</code> cannot start now but might be able
     *		to start at a later time.
     */
    protected void startApp() throws MIDletStateChangeException {
	midlet.startApp();
    }

    /**
     * Forwards pauseApp method to the MIDlet from the scheduler.
     *
     */
    protected void pauseApp() {
	midlet.pauseApp();
    }

    /**
     * Forwards destoryApp method to the MIDlet from the scheduler.
     *
     * @param unconditional the flag to pass to destroy
     *
     * @exception <code>MIDletStateChangeException</code> is thrown
     *		if the <code>MIDlet</code>
     *		wishes to continue to execute (Not enter the <i>Destroyed</i>
     *          state).
     *          This exception is ignored if <code>unconditional</code>
     *          is equal to <code>true</code>.
     */
    protected void destroyApp(boolean unconditional)
	throws MIDletStateChangeException {
	midlet.destroyApp(unconditional);
    }
}
