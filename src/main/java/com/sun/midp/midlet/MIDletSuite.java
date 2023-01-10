/*
 * @(#)MIDletSuite.java	1.31 02/10/14 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midlet;

import com.sun.midp.security.Permissions;

/**
 * Represents a MIDlet suite.
 */
public interface MIDletSuite {
    /**
     * Get a property of the suite. A property is an attribute from
     * either the application descriptor or JAR Manifest.
     *
     * @param key the name of the property
     * @return A string with the value of the property.
     * 		<code>null</code> is returned if no value is available for
     *          the key.
     */
    public String getProperty(String key);

    /**
     * Add a property to the suite.
     *
     * @param key the name of the property
     * @param value the value of the property
     *
     * @exception SecurityException if the calling suite does not have
     *            internal API permission
     */
    public void addProperty(String key, String value);

    /**
     * Provides the number of of MIDlets in this suite.
     *
     * @return number of MIDlet in the suite
     */
    public int getNumberOfMIDlets();

    /**
     * Get the classname of the initial MIDlet to run.
     *
     * @return classname of a MIDlet
     */  
    public String getInitialMIDletClassname();

    /**
     * Check to see the suite has the ALLOW level for specific permission.
     * This is used for by internal APIs that only provide access to
     * trusted system applications.
     * <p>
     * Only trust this method if the object has been obtained from the
     * Scheduler of the suite.
     *
     * @param permission permission ID from
     *      {@link com.sun.midp.security.Permissions}
     *
     * @exception SecurityException if the suite is not
     *            allowed to perform the specified action.
     */
    public void checkIfPermissionAllowed(int permission);

    /**
     * Check for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     *
     * @param permission ID of the permission to check for,
     *      the ID must be from
     *      {@link com.sun.midp.security.Permissions}
     * @param resource string to insert into the question, can be null
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public void checkForPermission(int permission, String resource)
        throws InterruptedException;

    /**
     * Get the status of the specified permission.
     * If no API on the device defines the specific permission 
     * requested then it must be reported as denied.
     * If the status of the permission is not known because it might
     * require a user interaction then it should be reported as unknown.
     *
     * @param permission to check if denied, allowed, or unknown.
     * @return 0 if the permission is denied; 1 if the permission is allowed;
     * 	-1 if the status is unknown
     */
    public int checkPermission(String permission);

    /**
     * Gets the path root of any file this suite.
     * Has any needed file separators appended.
     *
     * @return storage path root
     */
    public String getStorageRoot();

    /**
     * Gets the unique name of vendor and suite.
     *
     * @return storage name
     */
    public String getStorageName();

    /**
     * Get a named resource out of the JAR of this MIDlet suite.
     *
     * @param name name of the resource
     * @return raw bytes of the resource or null if not available
     */
    public byte[] getResource(String name);

    /**
     * Get the amount of storage on the device that this suite is using.
     * This includes the JAD, JAR, management data, and RMS.
     *
     * @return number of bytes of storage the suite is using.
     */
    public int getStorageUsed();

    /**
     * The URL that the suite was downloaded from.
     *
     * @return URL of the JAD, or JAR for a JAR only suite, never null,
     * even in development environments
     */
    public String getDownloadUrl();

    /**
     * Gets the name of Certificate Authority (CA) that authorized this suite.
     *
     * @return name of a CA or null if the suite was not signed
     */
    public String getCA();

    /**
     * Gets push setting for interrupting other MIDlets.
     * Reuses the Permissions.
     *
     * @return push setting for interrupting MIDlets the value
     *        will be permission level from {@link Permissions}
     */
    public int getPushInterruptSetting();

    /**
     * Gets list of permissions for this suite.
     *
     * @return array of permissions from {@link Permissions}
     */
    public byte[][] getPermissions();

    /**
     * Save any the settings (security or others) that the user may have
     * changed. Normally called by the scheduler after
     * the last running MIDlet in the suite is destoryed.
     * However it could be call during a suspend of the VM so
     * that persisent settings of the suite can be perserved.
     */
    public void saveSettings();

    /**
     * Ask the user want to interrupt the current MIDlet with
     * a new MIDlet that has received network data.
     *
     * @param connection connection to place in the permission question or
     *        null for alarm
     *
     * @return true if the use wants interrupt the current MIDlet, else false
     */
    public boolean permissionToInterrupt(String connection);

    /**
     * Indicates if this suite is trusted.
     * (not to be confused with a domain named "trusted",
     * this used to determine if a trusted symbol should be displayed
     * to the user and not used for permissions)
     *
     * @return true if the suite is trusted false if not
     */
    public boolean isTrusted();

    /**
     * Indicates if the named MIDlet is registered in the suite
     * with MIDlet-&lt;n&gt; record in the manifest or
     * application descriptor.
     * @param midletName class name of the MIDlet to be checked
     *
     * @return true if the MIDlet is registered
     */
    public boolean isRegistered(String midletName);

    /**
     * The JAD URL of the suite. This is only for the installer.
     *
     * @return URL of the JAD can be null
     */
    public String getJadUrl();

    /**
     * The JAR URL of the suite. This is only for the installer.
     *
     * @return URL of the JAR, never null, even in development environments
     */
    public String getJarUrl();
}
