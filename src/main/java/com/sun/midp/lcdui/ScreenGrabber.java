/*
 * @(#)ScreenGrabber.java	1.9 02/09/11 @(#)
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

/**
 * <code>ScreenGrabber</code> is a class that returns a hash of the 
 * pixels on the MIDP display drawing region.  If two screens have 
 * the same pixels and color depth, the result of a 
 * <code>getScreenDigest</code> call on each screen should be 
 * identical.
 *
 * This class is designed to automate UI testing, allowing a screen 
 * capture of a current test run to be compared against the stored
 * value of a previous run.
 */ 

public class ScreenGrabber { 

    /** 
     * A singleton ScreenGrabber instance is used to save space.
     */
    private static ScreenGrabber sg;
    
    /**
     * default constructor
     */
    private ScreenGrabber() {}
    
    /**
     * Use to obtain a reference to a ScreenGrabber instance.
     *
     * @return a ScreenGrabber instance
     */
    public static ScreenGrabber getInstance() {
	if (sg == null)
	    sg = new ScreenGrabber();
	return sg;
    }
    
    /**
     * This method returns a digest of the pixelmap of the current MIDP
     * display area.  For now, this is defined as the drawing area, as 
     * well as the status bar at the top of the screen and the area of 
     * the screen devoted to scroll arrows and softkey menu labels.  
     * It does not digest the entire MIDP window, i.e. the handset 
     * graphics. 
     *
     * WARNING: 
     * Minimal safety checks are done within the native code called by 
     * this method to determine that the native display data has
     * been initialized.  This method should only be called from 
     * within a MIDlet with a valid <code>Display</code> instance.  
     *
     * @return A digest of the current screen's pixmap. 
     *         Returns null on error.
     */
    public byte[] getData() {

	byte[] data = new byte[4];

	int digest = sysGetScreenDigest();
	if (digest == 0) 
	    return null;

	data[0] = (byte)((digest >> 24) & 0xff);
	data[1] = (byte)((digest >> 16) & 0xff);
	data[2] = (byte)((digest >> 8) & 0xff);
	data[3] = (byte)(digest & 0xff);

	return data;
    }

    /**
     * Native method <code>sysGetScreenDigest</code> is 
     * defined in file <code>screenGrabber.c</code>
     *
     * @return A CRC32 digest of the MIDP display area.
     *         Returns 0 if an error occurs 
     */
    private static native int sysGetScreenDigest();
} 
