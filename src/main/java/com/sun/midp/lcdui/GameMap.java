/*
 * @(#)GameMap.java	1.9 02/10/11 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Displayable;

/**
 * A class that maps between DisplayAccess objects and Displayable, GameCanvas.
 * In future versions of the MIDP spec, GameCanvas may have been
 * moved to lcdui package, in which case this class isn't needed.
 */
public class GameMap {
    /**
     * The Displayable associated with the DisplayAccess
     */
    static private Displayable displayable;
    /**
     * The DisplayAccess associated with the GameCanvas
     */
    static private DisplayAccess displayAccess;



    static final private Object lock = new Object();
    /**
     * Associate the given Displayable and DisplayAccess.  This is a
     * one-way association.
     *
     * @param c The GameCanvas to store
     * @param d The DisplayAccess associated with the GameCanvas
     */
    public static void register(Displayable c, DisplayAccess d) {
        synchronized (lock) {
	    displayable = c;
	    displayAccess = d;
	}
    }


    /**
     * Get the DisplayAccess object for this Displayable.
     * @param c The Displayable to get the DisplayAccess for
     * @return DisplayAccess The DisplayAccess associated with the MIDlet
     */
    public static DisplayAccess get(Displayable c) {
        synchronized (lock) {
  	    if (c == displayable) {
                return displayAccess;
  	    } else {
                return null;
	    }
        }
    }

}
