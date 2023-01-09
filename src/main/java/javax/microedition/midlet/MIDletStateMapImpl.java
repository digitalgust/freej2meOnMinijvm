/*
 * @(#)MIDletStateMapImpl.java	1.2 02/07/24 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.midlet;

import com.sun.midp.midlet.MIDletState;
import com.sun.midp.midlet.MIDletStateMap;

/**
 * This class works around the fact that public classes can not
 * be added to a javax package by an implementaion.
 */
class MIDletStateMapImpl extends MIDletStateMap {
    /** Create a <code>MIDletStateImpl</code>. */
    MIDletStateMapImpl() {};

    /**
     * Gets the state for a given MIDlet.
     *
     * @param m valid MIDlet
     *
     * @return state for a MIDlet.
     *
     * @exception NullPointerException if m is null
     */
    protected MIDletState getStateImpl(MIDlet m) {
        return m.getProxy();
    }
}

