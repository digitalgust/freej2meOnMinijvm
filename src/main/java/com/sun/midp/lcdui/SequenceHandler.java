/*
 * @(#)SequenceHandler.java	1.4 02/07/24 @(#)
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

/**
 * The SequenceHandler interface is used by the AutomationHandler
 * to perform a callbacks which pertain to certain 'hotkey'
 * executions, such as ending an event sequence or capturing the
 * current screen contents.
 */
public interface SequenceHandler {

    /**
     * Called by the AutomationHandler when an event sequence
     * is completed via a hotkey. The EventSequence Object will be the
     * same object that would be returned as if
     * AutomationHandler.stopEventSequence() had been called.
     *
     * @param sequence The completed event sequence
     */
    public void handleEventSequence(EventSequence sequence);

    /**
     * Called by the AutomationHandler when a screen capture
     * is performed via a hotkey. The byte[] will be the same array that
     * would be returned as if AutomationHandler.captureScreen()
     * had been called.
     *
     * @param capture The byte[] representation of the screen contents
     */
    public void handleScreenCapture(byte[] capture);
}


