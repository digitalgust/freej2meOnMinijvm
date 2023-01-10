/*
 * @(#)Configuration.java	1.6 02/09/03 @(#)
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.main;

/** access the implementation configuration file parameters. */
public class Configuration {
    /** Don't let anyone instantiate this class */
    private Configuration() {
    }
    /**
     * Gets the implementation property indicated by the specified key.
     *
     * @param      key   the name of the implementation property.
     * @return     the string value of the implementation property,
     *             or <code>null</code> if there is no property with that key.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static String getProperty(String key) {
	// If key is null, then a NullPointerException is thrown.
	// If key is blank, then throw a specific IllegalArgumentException
        if (key.length() ==  0) {
            throw new IllegalArgumentException("key can't be empty");
        }
        return getProperty0(key);
    }
    /**
     * Gets the implementation property indicated by the specified key or
     * returns the specifid default value.
     *
     * @param      key   the name of the implementation property.
     * @param      def   the default value for the property if not
     *                  specified in the configuration files or command 
     *                  line over rides.
     * @return     the string value of the implementation property,
     *             or <code>null</code> if there is no property with that key.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static String getPropertyDefault(String key, String def) {
	String result = getProperty(key);

	return (result != null ? result : def);
    }
    /**
     * native interface to the configuration parameter storage.
     *
     * @param      key   the name of the implementation property.
     * @return     the string value of the implementation property,
     *             or <code>null</code> if there is no property with that key.
     */
    private native static String getProperty0(String key);
}
