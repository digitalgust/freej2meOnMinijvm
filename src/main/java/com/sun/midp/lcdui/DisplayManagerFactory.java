/*
 * @(#)DisplayManagerFactory.java	1.6 02/09/11 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Display;
import com.sun.midp.security.SecurityToken;

/**
 * This class works around the fact that public classes can not
 * be added to a javax package by an implementaion.
 */
public class DisplayManagerFactory {
    /** The real implementation of the display manager. */
    private static DisplayManager managerImpl;

    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken displayClassSecurityToken;
    /**
     * Set the implemetation of the display manager, if one
     * is not already set.
     * <p>
     * This implementaion class will be in the as the Display class for
     * security. But needs placed here to be visible to com.sun.midp classes.
     *
     * @param dm reference to the system display manager
     */
    public static void SetDisplayManagerImpl(DisplayManager dm) {
        if (managerImpl != null) {
            return;
        }

        managerImpl = dm;
	managerImpl.initSecurityToken(displayClassSecurityToken);
    };

    /**
     * Return a reference to the singleton display manager object.
     *
     * @return display manager reference.
     */
    public static DisplayManager getDisplayManager() {
        if (managerImpl != null) {
            return managerImpl;
        }

        /**
         * The display manager implementation is a private class of Display
         * and is create in the class init of Display, we need to call a
         * static method of display to get the class init to run, because
         * some classes need to get the display manager to create a display
         */
        try {
            // this will yield a null pointer exception on purpose
            Display.getDisplay(null);
        } catch (NullPointerException npe) {
            // this is normal for this case, do nothing
        }

        return managerImpl;
    };

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public static void initSecurityToken(SecurityToken token) {
	if (displayClassSecurityToken != null) {
	    return;
	}
	
	displayClassSecurityToken = token;
    }
}
