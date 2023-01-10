/*
 * @(#)SystemEventListener.java	1.3 02/07/24 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

/** This is the DisplayManager/Scheduler contract for system events. */
public interface SystemEventListener {
    /**
     * Shutdown all running MIDlets and prepare the MIDP runtime
     * to exit completely.
     */
    public void shutdown();
} 
