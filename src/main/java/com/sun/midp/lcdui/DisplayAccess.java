/*
 * @(#)DisplayAccess.java	1.26 02/09/11 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Displayable;

import com.sun.midp.security.*;

/**
 * Public interface for an object that is used to provide internal access
 * to a Display object, across package boundaries.  The object implements
 * this interface, and is created inside the same package as Display,  so
 * that it has access to private instance data of Display as necessary.
 */
public interface DisplayAccess extends DisplayEvents {
    /**
     * Get the Display object that is associated with this DisplayAccess.
     * @return Display The Display object.
     */
    Display getDisplay();

    /**
     * Notifies the display that there is a change in its foreground
     * status.
     *
     * @param hasForeground A flag indicating the foreground status.
     */
    void foregroundNotify(boolean hasForeground);

    /**
     * Get the flag indicating that the most recent call to setCurrent
     * was for a non-null Displayable.
     * This allows the implementation of the hint provided
     * to the display manager to determine which MIDlet to make the foreground.
     *
     * @return <code>true</code> if the MIDlet has a screen to display;
     * otherwise <code>false</code> if MIDlet has indicated that it
     * does not need the display.
     */
    boolean wantsForeground();

    // API's for accessing Display from Games Package

    /**
     * Called to get key mask of all the keys that were pressed.
     * @return keyMask  The key mask of all the keys that were pressed.
     */
    int getKeyMask();

    /**
     * Flushes the entire off-screen buffer to the display.
     * @param screen The Displayable 
     * @param offscreen_buffer The image buffer 
     * @param x The left edge of the region to be flushed
     * @param y The top edge of the region to be flushed
     * @param width The width of the region to be flushed
     * @param height The height of the region to be flushed
     */
    void flush(Displayable screen, Image offscreen_buffer,
	       int x, int y, int width, int height);

    /**
     * Set the trusted icon for this Display. When ever this display is in
     * the foreground the given icon will be displayed in the area reserved for
     * the trusted icon. Setting the icon to null will clear the trusted
     * icon. Only callers with the internal MIDP permission can use this method.
     *
     * @param token security token of the call that has internal MIDP
     *              permission
     * @param drawTrusted true to draw the trusted icon
     */
    void setTrustedIcon(SecurityToken token, boolean drawTrusted);
}
