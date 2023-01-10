/*
 * @(#)CommandState.java	1.10 02/09/03 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.main;

/** Holds the state for MIDlet Suite management commands. */
class CommandState {
    /** status of the last command */
    int status;
    /** the initial command so we can loop (autotest) */
    int initialCommand;
    /** the next command to after the initial command, set by the processor */
    int nextCommand;
    /** overwrite the existing version of the MIDlet suite when installing */
    boolean forceOverwrite;
    /** location of the MIDlet suite to install */
    String suiteURL;
    /** the storage name given to a suite when installed */
    String suiteStorageName;
    /** remove the suite after running it */
    boolean runOnce;
    /** repeat the install-run-remove process until the suite is not found */
    boolean autotest;
    /** filename of local descriptor, for development systems */
    String descriptorName;
    /** what midlet to run in the suite, by name, instead of class or number */
    String midletName;
    /** Class name of MIDlet, for development systems, instead of name. */
    String midletClassName;
    /** Did we display the Java logo yet? */
    boolean logoDisplayed;
    /** Message for the exception for the manager to display. */
    String runExceptionMessage;
    /** Name of the security domain. */
    String securityDomain;
    /** what midlet to run in the suite, by number, instead of class or name */
    String midletNumber;
    /** Force the removal of old RMS data when updating. */
    boolean removeRMS;
}
