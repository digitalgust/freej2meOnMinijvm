/*
 * @(#)Events.java	1.5 02/07/24 @(#)
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import java.io.IOException;

/**
 * This class establishes the connection to the event queue in the virtual
 * machime, and allows the Java code to read the event data from the queue.
 */
public class Events {

    /**
     * Open the connection to the native event queue.
     *
     * @throws IOException if an I/O error occurs.
     */
    public native void open() throws IOException;

    /**
     * Reads the next 4 bytes from the event queue, and return as 
     * an integer.
     *
     * @return     the next four bytes of the event queue,
     *             interpreted as an <code>int</code>.
     * @exception  IOException   if an I/O error occurs.
     */
    public native int readInt() throws IOException;

    /**
     * Reads from the event queue a String object.
     *
     * @return     a string
     * @exception  IOException   if an I/O error occurs.
     */
    public native String readUTF() throws IOException;
}
