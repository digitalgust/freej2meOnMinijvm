/*
 * @(#)DevMIDletSuiteImpl.java	1.40 02/10/14 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.dev;

import java.io.*;

import javax.microedition.io.Connector;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.Scheduler;
import com.sun.midp.midlet.MIDletInfo;

import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.io.j2me.storage.File;

import com.sun.midp.midletsuite.JadProperties;
import com.sun.midp.midletsuite.ManifestProperties;
import com.sun.midp.midletsuite.MIDletSuiteImpl;
import com.sun.midp.midletsuite.InvalidJadException;
import com.sun.midp.midletsuite.Installer;

/**
 * Implements a the required MIDletSuite functionality needed by the
 * Scheduler. The class is only need for development environments.
 */
public class DevMIDletSuiteImpl extends MIDletSuiteImpl {
    /** Buffered properties from the application descriptor */
    private JadProperties bufferedJadProps = new JadProperties();

    /** Initial midlet class name. */
    private String initialMIDletClassName = null;

    /** Storage path for this MIDlet suite */
    private String storageRoot = null;

    /** Permissions for this suite. */
    private byte[] permissions;

    /**
     * number of midlets in this suite. less than 0 mean they need to
     * counted
     */
    private int numberOfMidlets = -1;

    /** Push interrupt question for this suite. */
    private String pushInterruptQuestion;

    /** Alarm interrupt question for this suite. */
    private String alarmInterruptQuestion;

    /**
     * Creates MIDletSuite from a raw JAD.
     *
     * @param callerSecurityToken security token of the caller
     * @param jadFilename filename of the descriptor for the suite
     * @param midletClassName class name of the MIDlet to run when this
     *                        suite is scheduled
     * @param storageName name to separate this suite's storage from others
     * @param domain name of the security domain this suite is to run under,
     *               null for untrusted
     *
     * @return new MIDletSuite object
     *
     * @exception IOException is thrown if any error prevents the installation
     *   of the MIDlet suite
     * @exception InvalidJadException if the downloaded application descriptor
     *   is invalid
     */
    public static MIDletSuite create(SecurityToken callerSecurityToken,
                              String jadFilename, String midletClassName,
                              String storageName, String domain)
            throws IOException, InvalidJadException {
        return create(callerSecurityToken, jadFilename, midletClassName,
            storageName, null, null, domain, !(domain == null ||
            domain.equals(Permissions.UNTRUSTED_DOMAIN_NAME)),
            null, null);
    }

    /**
     * Creates MIDletSuite from a raw JAD.
     *
     * @param callerSecurityToken security token of the caller
     * @param jadFilename filename of the descriptor for the suite
     * @param midletClassName class name of the MIDlet to run when this
     *                        suite is scheduled
     * @param storageName name to separate this suite's storage from others
     * @param keys list of keys to MIDlet suite properties
     * @param values list of values for the keys to MIDlet suite properties
     * @param domain name of the security domain this suite is to run under,
     *               null for untrusted
     * @param trusted true if the suite is to considered trusted
     *        (not to be confused with a domain named "trusted",
     *        this used to determine if a trusted symbol should be displayed
     *        to the user and not used for permissions)
     * @param pushQuestion question to ask when a push interruption happens,
     *        can be null to use default
     * @param alarmQuestion question to ask when an alarm interruption
     *        happens, can be null to use default
     *
     * @return new MIDletSuite object
     *
     * @exception IOException is thrown if any error prevents the installation
     *   of the MIDlet suite
     * @exception InvalidJadException if the downloaded application descriptor
     *   is invalid
     */
    public static MIDletSuite create(SecurityToken callerSecurityToken,
                              String jadFilename, String midletClassName,
                              String storageName, String[] keys,
                              String[] values, String domain, boolean trusted,
                              String pushQuestion, String alarmQuestion)
            throws IOException, InvalidJadException {
        DevMIDletSuiteImpl suite;
        RandomAccessStream storage;
        InputStream jadStream;
        byte[][] temp;
        int pushSetting;

        if (domain == null) {
            domain = Permissions.UNTRUSTED_DOMAIN_NAME;
        }

        temp = Permissions.forDomain(callerSecurityToken, domain);

        if (temp[Permissions.CUR_LEVELS][Permissions.PUSH] ==
                Permissions.NEVER) {
            pushSetting = Permissions.NEVER;
        } else if (temp[Permissions.CUR_LEVELS][Permissions.PUSH] ==
                   Permissions.ALLOW) {
            pushSetting = Permissions.BLANKET;
        } else {
            pushSetting = temp[Permissions.CUR_LEVELS][Permissions.PUSH];
        }

        suite = new DevMIDletSuiteImpl(callerSecurityToken, temp, pushSetting,
                                       trusted, storageName);

        if (jadFilename != null) {
            storage = new RandomAccessStream(callerSecurityToken);
            storage.connect(jadFilename, Connector.READ);
            try {
                jadStream = storage.openInputStream();
                suite.bufferedJadProps.load(jadStream);
            } finally {
                storage.disconnect();
            }

            suite.numberOfMidlets = suite.countMIDlets();
        } else {
            // without a jad we must assume at least one MIDlet
            suite.numberOfMidlets = 1;
        }

        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                suite.bufferedJadProps.setProperty(keys[i], values[i]);
            }
        }

        suite.storageRoot = File.getStorageRoot() + storageName;

        suite.initialMIDletClassName = midletClassName;

        if (pushQuestion != null) {
            suite.pushInterruptQuestion = pushQuestion;
        } else {
            suite.pushInterruptQuestion = PUSH_INTERRUPT_QUESTION;
        }

        if (alarmQuestion != null) {
            suite.alarmInterruptQuestion = alarmQuestion;
        } else {
            suite.alarmInterruptQuestion = ALARM_INTERRUPT_QUESTION;
        }

        return suite;
    }

    /**
     * Creates MIDletSuiteImpl from a raw JAD.
     *
     * @param callerSecurityToken security token of the caller
     * @param permissions security permissions of the suite
     * @param pushSetting can this MIDlet suite interrupt other suites
     * @param trusted true if the suite is to considered trusted
     *        (not to be confused with a domain named "trusted",
     *        this only shown to the user and not used for permissions)
     * @param storageName name to separate this suite's storage from others
     */
    private DevMIDletSuiteImpl(SecurityToken callerSecurityToken,
            byte[][] permissions, int pushSetting, boolean trusted,
            String storageName) {
        super(callerSecurityToken, permissions, pushSetting, trusted,
              storageName);
    }

    /**
     * Get a property of the suite. A property is an attribute from
     * the application descriptor.
     *
     * @param key the name of the property
     * @return A string with the value of the property.
     * 		<code>null</code> is returned if no value is available for
     *          the key.
     */
    public String getProperty(String key) {
        return bufferedJadProps.getProperty(key);
    }

    /**
     * Add a property to the suite.
     *
     * @param key the name of the property
     * @param value the value of the property
     *
     * @exception SecurityException if the calling suite does not have
     *            internal API permission
     */
    public void addProperty(String key, String value) {
        MIDletSuite current = Scheduler.getScheduler().getMIDletSuite();

        if (current != null) {
            current.checkIfPermissionAllowed(Permissions.MIDP);
        }

        bufferedJadProps.addProperty(key, value);
    }

    /**
     * Provides the number of of MIDlets in this suite.
     *
     * @return number of MIDlet in the suite
     */
    public int getNumberOfMIDlets() {
        return numberOfMidlets;
    }

    /**
     * Get the classname of the initial MIDlet to run.
     *
     * @return classname of a MIDlet
     */  
    public String getInitialMIDletClassname() {
        if (initialMIDletClassName != null) {
            return initialMIDletClassName;
        }

        // Have the user select a MIDlet. The selector should not exit.
        return "com.sun.midp.dev.PersistentSelector";
    }

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
            throws InterruptedException {
        String app;

        app = getProperty(Installer.SUITE_NAME_PROP);
        if (app == null) {
            app = initialMIDletClassName;
        }

        super.checkForPermission(permission, app, resource);
    }

    /**
     * Gets the path root of any file this suite.
     * Has any needed file separators appended.
     *
     * @return storage path root
     */
    public String getStorageRoot() {
        return storageRoot;
    }

    /**
     * Get a named resource out of the JAR of this MIDlet suite.
     *
     * @param name name of the resource
     * @return raw bytes of the resource or null if not available
     */
    public byte[] getResource(String name) {
        return null;
    }

    /**
     * Get the amount of storage on the device that this suite is using.
     * This includes the JAD, JAR, management data, and RMS.
     *
     * @return number of bytes of storage the suite is using.
     */
    public int getStorageUsed() {
        return 0;
    }

    /**
     * Gets the name of CA that authorized this suite.
     *
     * @return name of a CA or null if the suite was not signed
     */
    public String getCA() {
        return null;
    }

    /**
     * Save any the settings (security or others) that the user may have
     * changed. Normally called by the scheduler after
     * the last running MIDlet in the suite is destoryed.
     * However it could be call during a suspend of the VM so
     * that persisent settings of the suite can be perserved.
     */
    public void saveSettings() {
        // we do not save the settings for classes run from the classpath
    }

    /**
     * Get the Push interrupt question the should be used when
     * interrupting this suite.
     * <p>
     * The question will have %2 where this suite name should be and
     * a %1 where the current suite name should be.
     *
     * @return push interrupt question
     */
    protected String getPushInterruptQuestion() {
        return pushInterruptQuestion;
    }

    /**
     * Get the Alarm interrupt question the should be used when
     * interrupting this suite.
     * <p>
     * The question will have %2 where this suite name should be and
     * a %1 where the current suite name should be.
     *
     * @return alarm interrupt question
     */
    protected String getAlarmInterruptQuestion() {
        return alarmInterruptQuestion;
    }

    /**
     * Get the suite name for interruption purposes.
     *
     * @return name for interrupt question
     */
    protected String getSuiteNameForInterrupt() {
        String name = getProperty(Installer.SUITE_NAME_PROP);

        if (name != null) {
            return name;
        }

        return initialMIDletClassName;
    }

    /**
     * The JAD URL of the suite. This is only for the installer.
     *
     * @return URL of the JAD can be null
     */
    public String getJadUrl() {
        return null;
    }

    /**
     * The JAR URL of the suite. This is only for the installer.
     *
     * @return URL of the JAR, never null, even in development environments
     */
    public String getJarUrl() {
        return "none";
    }
}
