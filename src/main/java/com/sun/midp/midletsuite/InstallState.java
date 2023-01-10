/*
 * @(#)InstallState.java	1.6 02/07/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midletsuite;

/**
 * Holds the state of an installation, so it can restarted after it has
 * been stopped.
 */
public interface InstallState {
    /**
     * Gets the last recoverable exception that stopped the install.
     * Non-recoverable exceptions are thrown and not saved in the state.
     *
     * @return last exception that stopped the install
     */
    public InvalidJadException getLastException();

    /**
     * Gets the unique name that the installed suite was stored with.
     *
     * @return storage name that can be used to load the suite
     */
    public String getStorageName();

    /**
     * Sets the username to be used for HTTP authentication.
     *
     * @param theUsername 8 bit username, cannot contain a ":"
     */
    public void setUsername(String theUsername);

    /**
     * Sets the password to be used for HTTP authentication.
     *
     * @param thePassword 8 bit password
     */
    public void setPassword(String thePassword);

    /**
     * Sets the username to be used for HTTP proxy authentication.
     *
     * @param theUsername 8 bit username, cannot contain a ":"
     */
    public void setProxyUsername(String theUsername);

    /**
     * Sets the password to be used for HTTP proxy authentication.
     *
     * @param thePassword 8 bit password
     */
    public void setProxyPassword(String thePassword);

    /**
     * Gets a property of the application to be installed.
     * First from the JAD, then if not found, the JAR manifest.
     *
     * @param key key of the property
     *
     * @return value of the property or null if not found
     */
    public String getAppProperty(String key);

    /**
     * Gets the URL of the JAR.
     *
     * @return URL of the JAR
     */
    public String getJarUrl();

    /**
     * Gets the expected size of the JAR.
     *
     * @return size of the JAR in K bytes
     */
    public int getJarSize();
}
