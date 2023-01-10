/*
 * @(#)CommandAccess.java	1.9 02/07/24 @(#)
 *
 * Copyright (c) 1999-2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

/* This is an interface that allows Java code to get at the private    */
/* data fields of a Command.  It's only temporary, because (I believe) */
/* the access problems were fixed in a later version of the MIDP spec. */
package com.sun.midp.lcdui;

import javax.microedition.lcdui.Command;

/**
 * Special class to handle access to Command objects.
 */
public interface CommandAccess {
    /**
     * Get the label of the given Command.
     *
     * @param c The Command to retrieve the label of
     * @return String The label of the Command
     */
    String getLabel(Command c);
    /**
     * Get the type of the given Command.
     *
     * @param c The Command to retrieve the type of
     * @return int The type of the Command
     */
    int    getType(Command c);
    /**
     * Get the priority of the given Command.
     *
     * @param c The Command to retrieve the priority of
     * @return int The priority of the Command
     */
    int    getPriority(Command c);
}
