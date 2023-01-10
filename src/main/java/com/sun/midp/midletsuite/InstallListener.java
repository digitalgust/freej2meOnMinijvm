/*
 * @(#)InstallListener.java	1.5 02/08/28 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midletsuite;

/** Enables a MIDlet to respond installation warnings and status updates. */
public interface InstallListener {
    /**
     * Called with the current state of the install so the user can be
     * asked to override the warning. To get the warning from the state
     * call {@link InstallState#getLastException()}. If false is returned,
     * the last exception in the state will be thrown and
     * {@link Installer#wasStopped()} will return true if called.
     *
     * @param state current state of the install.
     *
     * @return true if the user wants to continue, false to stop the install
     */
    public boolean warnUser(InstallState state);

    /**
     * Called with the current state of the install so the user can be
     * asked to confirm the jar download.
     * If false is returned, the an I/O exception thrown and
     * {@link Installer#wasStopped()} will return true if called.
     *
     * @param state current state of the install.
     *
     * @return true if the user wants to continue, false to stop the install
     */
    public boolean confirmJarDownload(InstallState state);

    /**
     * Called with the current status of the install. See
     * {@link Installer} for the status codes.
     *
     * @param status current status of the install.
     * @param state current state of the install.
     */
    public void updateStatus(int status, InstallState state);

    /**
     * Called with the current state of the install so the user can be
     * asked to confirm if the RMS data should be kept for new version of
     * an updated suite.
     * If false is returned, the an I/O exception thrown and
     * {@link Installer#wasStopped()} will return true if called.
     *
     * @param state current state of the install.
     *
     * @return true if the user wants to keep the RMS data for the next suite
     */
    public boolean keepRMS(InstallState state);
}
