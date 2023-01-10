/*
 * @(#)MIDletStateMap.java	1.4 02/07/24 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midlet;

import javax.microedition.midlet.MIDlet;

/**
 * This class works around the fact that public classes can not
 * be added to a javax package by an implementaion.
 */
public abstract class MIDletStateMap {
    /** Implementation of the MIDlet state map. */
    private static MIDletStateMap mapImpl;

    /**
     * Set the MIDlet state map implementation if one has not been set.
     *
     * @param map Implementation of a MIDletMap
     *
     * @exception NullPointerException if m is null
     */
    public static void setMapImpl(MIDletStateMap map) {
        if (mapImpl != null) {
            return;
        }

        mapImpl = map;
    }

    /**
     * Get the <code>MIDletState</code> for a given midlet.
     *
     * @param m valid MIDlet
     *
     * @return state of an object
     *
     * @exception NullPointerException if m is null
     */
    public static MIDletState getState(MIDlet m) {
	return mapImpl.getStateImpl(m);
    }

    /**
     * Gets the state for a given MIDlet.
     *
     * @param m valid MIDlet
     *
     * @return state for a MIDlet.
     *
     * @exception NullPointerException if m is null
     */
    protected abstract MIDletState getStateImpl(MIDlet m);
}

