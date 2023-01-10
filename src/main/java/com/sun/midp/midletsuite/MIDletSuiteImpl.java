/*
 * @(#)MIDletSuiteImpl.java	1.52 02/10/14 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midletsuite;

import java.io.*;
import java.util.*;

import javax.microedition.io.Connector;

import javax.microedition.lcdui.*;

import javax.microedition.midlet.*;

import com.sun.midp.io.*;

import com.sun.midp.lcdui.*;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.Scheduler;
import com.sun.midp.midlet.MIDletInfo;

import com.sun.midp.io.j2me.storage.File;
import com.sun.midp.io.j2me.storage.RandomAccessStream;

/**
 * Implements a the required MIDletSuite functionality needed by the
 * Scheduler.
 */
public class MIDletSuiteImpl implements MIDletSuite {
    /** Interrupt dialog title for push. */
    static final String PUSH_INTERRUPT_DIALOG_TITLE = "Can &1 Interrupt?";

    /** Interrupt question for push. */
    protected static final String PUSH_INTERRUPT_QUESTION =
        "Information is arriving for %1. " +
        "Is it OK to exit %2 and launch %1?";

    /** Interrupt question for alarms. */
    protected static final String ALARM_INTERRUPT_QUESTION =
        "%1 needs to start itself to check to see if it has received " +
        "information. Is it OK to exit %2 and launch %1?";

    /** This class has a different security domain than the application. */
    private static SecurityToken classSecurityToken;

    /** Buffered properties from the application descriptor. */
    private JadProperties bufferedJadProps;

    /** Buffered properties from the JAR manifest. */
    private ManifestProperties bufferedJarProps;

    /** Security token for this suite. */
    private SecurityToken securityToken;

    /** Permissions for this suite. */
    private byte[][] permissions;

    /** Can this MIDlet suite interrupt other suites. */
    private int pushInterruptSetting;

    /** The storage path of this suite. */
    private String storageRoot;

    /** The storage name of this suite. */
    private String storageName;

    /** The CA that authorized this suite. */
    private String ca;

    /** Indicates if this suite is trusted. */
    private boolean trusted;

    /** Initial midlet class name. */
    private String initialMIDletClassName;

    /**
     * Number of midlets in this suite. less than 0 mean they need to
     * counted.
     */
    private int numberOfMidlets = -1;

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class
     */
    static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken != null) {
            return;
        }

        classSecurityToken = token;
    }

    /**
     * Constructor for development subclass.
     *
     * @param callerSecurityToken security token for the calling class
     * @param suitePermissions security token of the suite
     * @param pushSetting can this MIDlet suite interrupt other suites
     * @param trustedFlag true if the suite is to considered trusted
     *        (not to be confused with a domain named "trusted",
     *        this only shown to the user and not used for permissions)
     * @param theStorageName name to separate this suite's storage from others
     */
    protected MIDletSuiteImpl(SecurityToken callerSecurityToken,
            byte[][] suitePermissions, int pushSetting, boolean trustedFlag,
            String theStorageName) {

        callerSecurityToken.checkIfPermissionAllowed(Permissions.MIDP);

        permissions = suitePermissions;
        securityToken = new SecurityToken(classSecurityToken, permissions);
        pushInterruptSetting = pushSetting;
        trusted = trustedFlag;
        storageName = theStorageName;
    }

    /**
     * Constructs MIDletSuiteImpl from an installed MIDlet Suite.
     *
     * @param callerSecurityToken security token for the calling class
     * @param theStorageRoot root path of any files for this suite
     * @param theStorageName unique vendor and suite name identifying this
     *        suite
     * @param theCA name of CA that authorized this suite
     * @param midletToRun the name of the initial MIDlet in this suite to run,
     *        can be null
     */
    MIDletSuiteImpl(SecurityToken callerSecurityToken,
                    String theStorageRoot, String theStorageName,
		    String theCA, String midletToRun) {
        callerSecurityToken.checkIfPermissionAllowed(Permissions.MIDP);

        storageRoot = theStorageRoot;

        storageName = theStorageName;

        ca = theCA;

        readSettings();

        securityToken = new SecurityToken(classSecurityToken, permissions);

        if (midletToRun != null) {
            initialMIDletClassName = getMIDletClassName(midletToRun);
        }
    }

    /**
     * Constructs MIDletSuiteImpl from an installed MIDlet Suite.
     *
     * @param callerSecurityToken security token for the calling class
     * @param theStorageRoot root path of any files for this suite
     * @param theStorageName unique vendor and suite name identifying this
     *        suite
     * @param theCA name of CA that authorized this suite
     * @param midletToRun the number of the initial MIDlet in this suite
     */
    MIDletSuiteImpl(SecurityToken callerSecurityToken,
                    String theStorageRoot, String theStorageName,
		    String theCA, int midletToRun) {
        this(callerSecurityToken, theStorageRoot, theStorageName,
             theCA, null);

        String temp;

        temp = getProperty("MIDlet-" + midletToRun);
        if (temp == null) {
            return;
        }

        initialMIDletClassName = new MIDletInfo(temp).classname;
    }

    /**
     * Gets a property of the suite. A property is an attribute from
     * either the application descriptor or JAR Manifest.
     *
     * @param key the name of the property
     * @return A string with the value of the property.
     * 		<code>null</code> is returned if no value is available for
     *          the key.
     */
    public String getProperty(String key) {
        String prop;

        if (bufferedJadProps == null) {
            getPropertiesFromStorage();
            if (bufferedJadProps == null) {
                return null;
            }
        }

        // check the JAD first
        prop = bufferedJadProps.getProperty(key);
        if (prop != null) {
            return prop;
        }

        if (bufferedJarProps == null) {
            return null;
        }

        return bufferedJarProps.getProperty(key);
    }

    /**
     * Adds a property to the suite.
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

        if (bufferedJadProps != null) {
            bufferedJadProps.addProperty(key, value);
            return;
        }

        bufferedJarProps.addProperty(key, value);
    }

    /**
     * Provides the number of of MIDlets in this suite.
     *
     * @return number of MIDlet in the suite
     */
    public int getNumberOfMIDlets() {
        if (numberOfMidlets <= 0) {
            numberOfMidlets = countMIDlets();
        }

        return numberOfMidlets;
    }

    /**
     * Gets the classname of the initial MIDlet to run.
     *
     * @return classname of a MIDlet
     */  
    public String getInitialMIDletClassname() {
        if (initialMIDletClassName != null) {
            return initialMIDletClassName;
        }

        if (getNumberOfMIDlets() == 1) {
            return new MIDletInfo(getProperty("MIDlet-1")).classname;
        }

        // Have the user select a MIDlet.
        return "com.sun.midp.midlet.Selector";
    }

    /**
     * Checks to see the suite has the ALLOW level for specific permission.
     * This is used for by internal APIs that only provide access to
     * trusted system applications.
     *
     * @param permission permission ID from com.sun.midp.security.Permissions
     *
     * @exception SecurityException if the suite is not ALLOWed the permission
     */
    public void checkIfPermissionAllowed(int permission) {
        securityToken.checkIfPermissionAllowed(permission);
    }

    /**
     * Checks for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     *
     * @param permission ID of the permission to check for,
     *      the ID must be from
     *      {@link com.sun.midp.security.Permissions}
     * @param resource string to insert into the permission question,
     *        can be null
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public void checkForPermission(int permission, String resource)
            throws InterruptedException {
        checkForPermission(permission,
            getProperty(Installer.SUITE_NAME_PROP), resource);
    }

    /**
     * Checks for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     *
     * @param permission ID of the permission to check for,
     *      the ID must be from
     *      {@link com.sun.midp.security.Permissions}
     * @param name name of the suite
     * @param resource string to insert into the question, can be null
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    protected void checkForPermission(int permission, String name,
            String resource) throws InterruptedException {
        String protocolName = null;

        try {
            int colon = resource.indexOf(':');

            if (colon != -1) {
                protocolName = resource.substring(0, colon);
            }
        } catch (Exception e) {
            // ignore
        }

        securityToken.checkForPermission(permission,
                                         Permissions.getTitle(permission),
                                         Permissions.getQuestion(permission),
                                         name, resource, protocolName);
    }

    /**
     * Gets the status of the specified permission.
     * If no API on the device defines the specific permission 
     * requested then it must be reported as denied.
     * If the status of the permission is not known because it might
     * require a user interaction then it should be reported as unknown.
     *
     * @param permission to check if denied, allowed, or unknown
     * @return 0 if the permission is denied; 1 if the permission is allowed;
     * 	-1 if the status is unknown
     */
    public int checkPermission(String permission) {
        return securityToken.checkPermission(permission);
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
     * Gets the unique name of vendor and suite.
     *
     * @return storage name
     */
    public String getStorageName() {
        return storageName;
    }

    /**
     * Get a named resource out of the JAR of this MIDlet suite.
     *
     * @param name name of the resource
     * @return raw bytes of the resource or null if not available
     */
    public byte[] getResource(String name) {
        if (name.charAt(0) == '/') {
            // the jar reader does not remove the leading '/'
            name = name.substring(1, name.length());
        }

        try {
            return JarReader.readJarEntry(classSecurityToken,
                                          getStorageRoot() +
                                          Installer.JAR_FILENAME,
                                          name);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets the amount of storage on the device that this suite is using.
     * This includes the JAD, JAR, management data, and RMS.
     *
     * @return number of bytes of storage the suite is using
     */
    public int getStorageUsed() {
        File file = new File(classSecurityToken);
        RandomAccessStream stream =
            new RandomAccessStream(classSecurityToken);
        Vector files;
        int storageUsed = 0;

        files = file.filenamesThatStartWith(getStorageRoot());
        for (int i = 0; i < files.size(); i++) {
            try {
                stream.connect((String)files.elementAt(i), Connector.READ);
                try {
                    storageUsed += stream.getSizeOf();
                } finally {
                    stream.disconnect();
                }
            } catch (IOException ioe) {
                // just move on to the next file
            }
        }

        return storageUsed;
    }

    /**
     * Gets the URL that the suite was downloaded from.
     *
     * @return URL of the JAD, or JAR for a JAR only suite, never null,
     * even in development environments
     */
    public String getDownloadUrl() {
        String url = getJadUrl();

        if (url != null) {
            return url;
        }

        return getJarUrl();
    }


    /**
     * Gets the name of CA that authorized this suite.
     *
     * @return name of a CA or null if the suite was not signed
     */
    public String getCA() {
        return ca;
    }

    /**
     * Counts the number of MIDlets from its properties.
     *
     * @return number of midlet in the suite
     */
    protected int countMIDlets() {
        int i;

        for (i = 1; getProperty("MIDlet-" + i) != null; i++);

        return i - 1;
    }

    /**
     * Retrieves the classname for a given MIDlet name.
     * <p>
     *
     * @param midletName the name of the MIDlet to find
     * @return the classname of the MIDlet. <code>null</code> if the
     *         MIDlet cannot be found
     */
    protected String getMIDletClassName(String midletName) {
        String midlet;
        MIDletInfo midletInfo;

        for (int i = 1; ; i++) {
            midlet = getProperty("MIDlet-" + i);
            if (midlet == null) {
		/*
		 * If the name was a class name use it. 
		 * (Temporary implemention - overloading the 
		 * name as MIDlet name or class name could be in
		 * conflict. Longer term solution would expand
		 * Installer.execute() semantics to allow a class
		 * name to run, rather than just the indirection
		 * via MIDlet info.)
		 */
		try {
		    Class.forName(midletName);
		    return midletName;
		} catch (Exception e) {}

                return null; // We went past the last MIDlet
            }

            midletInfo = new MIDletInfo(midlet);
            if (midletInfo.name.equals(midletName)) {
                return midletInfo.classname;
            }
        }
    }

    /**
     * Gets properites from a symbolically named installed package.
     * The properties are the attributes in the application descriptor
     * and JAR Manifest.
     */
    private void getPropertiesFromStorage() {
        RandomAccessStream myStorage;
        int size;
        byte[] buffer;
        InputStream is;
        DataInputStream dis;
        String jadEncoding = null;

        myStorage = new RandomAccessStream(classSecurityToken);

        // Get the JAD encoding, if the server provided one
        try {
            myStorage.connect(storageRoot +
                              Installer.JAD_ENCODING_FILENAME,
                              Connector.READ);
            try {
                // convert the JAD encoding to UTF8 and write it to storage
                dis = myStorage.openDataInputStream();
                try {
                    jadEncoding = dis.readUTF();
                } finally {
                    dis.close();
                }
            } finally {
                myStorage.disconnect();
            }
        } catch (IOException e) {
            // servers can choose the default encoding by not providing one
        }

        // Load .jad file
        bufferedJadProps = new JadProperties();
        try {
            myStorage.connect(storageRoot + Installer.JAD_FILENAME,
                              Connector.READ);
            try {
                size = myStorage.getSizeOf();
                buffer = new byte[size];
                dis = myStorage.openDataInputStream();
                try {
                    dis.readFully(buffer);
                    is = new ByteArrayInputStream(buffer);

                    bufferedJadProps.load(is, jadEncoding);

                    buffer = null;
                    is = null;
                } finally {
                    dis.close();
                }
            } finally {
                myStorage.disconnect();
            }
        } catch (IOException e) {
            // Jar only install
        }

        try {
            // Get Manifest file so we can buffer it
            myStorage.connect(storageRoot + Installer.MANIFEST_FILENAME,
                              Connector.READ);
            try {
                size = myStorage.getSizeOf();
                buffer = new byte[size];
                dis = myStorage.openDataInputStream();
                try {
                    dis.readFully(buffer);
                    is = new ByteArrayInputStream(buffer);

                    bufferedJarProps = new ManifestProperties();
                    bufferedJarProps.load(is);

                    buffer = null;
                    is = null;
                } finally {
                    dis.close();
                }
            } finally {
                myStorage.disconnect();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Gets push setting for interrupting other MIDlets.
     * Reuses the Permissions.
     *
     * @return push setting for interrupting MIDlets the value
     *        will be permission level from {@link Permissions}
     */
    public int getPushInterruptSetting() {
        return pushInterruptSetting;
    }

    /**
     * Gets list of permissions for this suite.
     *
     * @return array of permissions from {@link Permissions}
     */
    public byte[][] getPermissions() {
        return copyPermissions(permissions);
    }

    /**
     * Makes a copy of a list of permissions.
     *
     * @param permissions source copy
     * @return array of permissions from {@link Permissions}
     */
    protected byte[][] copyPermissions(byte[][] permissions) {
        if (permissions == null) {
            return null;
        }

        byte[][] copy = new byte[2][];
        for (int i = 0; i < 2; i++) {
            copy[i] = new byte[permissions[i].length];
            System.arraycopy(permissions[i], 0, copy[i], 0,
                             permissions[i].length);
        }

        return copy;
    }

    /**
     * Saves any the settings (security or others) that the user may have
     * changed. Normally called by the scheduler after
     * the last running MIDlet in the suite is destoryed.
     * However it could be called during a suspend of the VM so
     * that persisent settings of the suite can be perserved or
     * by the graphical manager application settings MIDlet.
     */
    public void saveSettings() {
        try {
            Installer.saveSuiteSettings(classSecurityToken, storageRoot,
                (byte)pushInterruptSetting, permissions, trusted);
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Reads the suite settings from storage.
     */
    private void readSettings() {
        byte[] maximums = Permissions.getEmptySet();
        byte[] currentLevels = Permissions.getEmptySet();
        RandomAccessStream storage =
            new RandomAccessStream(classSecurityToken);
        DataInputStream storageStream;
        int version;
        int count;

        permissions = new byte[2][];
        permissions[Permissions.MAX_LEVELS] = maximums;
        permissions[Permissions.CUR_LEVELS] = currentLevels;

        try {
            storage.connect(getStorageRoot() + Installer.SETTINGS_FILENAME,
                            Connector.READ);
            try {
                storageStream = storage.openDataInputStream();

                version = storageStream.readByte();
                /*
                 * only version 1 are handled by the method
                 * 0 means that this is a beta version that are not handled
                 * by the method. Note that version number only has to
                 * increase if data has been removed, not if new data has been
                 * added to the end of the file.
                 */
                if (version != 1) {
                    System.out.println("Corrupt application settings file.");
                    return;
                }

                trusted = storageStream.readBoolean();

                pushInterruptSetting = storageStream.readByte();

                count = storageStream.readByte();
                storageStream.readFully(currentLevels, 0, count);

                count = storageStream.readByte();
                storageStream.readFully(maximums, 0, count);
            } finally {
                storage.disconnect();
            }
        } catch (IOException e) {
            // ignore, old settings files are shorter
        }
    }

    /**
     * Asks the user want to interrupt the current MIDlet with
     * a new MIDlet that has received network data. 
     *
     * @param connection connection to place in the permission question or
     *        null for alarm
     *
     * @return true if the use wants interrupt the current MIDlet, else false
     */
    public boolean permissionToInterrupt(String connection) {
        String name;
        MIDletSuite current;
        String question;
        String currentName;

        if (pushInterruptSetting == Permissions.USER_DENIED ||
                pushInterruptSetting == Permissions.NEVER) {
            return false;
        }

        // treat SESSION level the same as ONE_SHOT

        switch (pushInterruptSetting) {
        case Permissions.ALLOW:
        case Permissions.BLANKET_GRANTED:
            return true;
        }

        name = getSuiteNameForInterrupt();

        // The currently running suite controls what question to ask.
        current = Scheduler.getScheduler().getMIDletSuite();
        if (current instanceof MIDletSuiteImpl) {
            MIDletSuiteImpl temp = (MIDletSuiteImpl)current;
            if (connection == null) {
                question = temp.getAlarmInterruptQuestion();
            } else {
                question = temp.getPushInterruptQuestion();
            }

            currentName = temp.getSuiteNameForInterrupt();
        } else {
            // use the questions of this suite
            if (connection == null) {
                question = getAlarmInterruptQuestion();
            } else {
                question = getPushInterruptQuestion();
            }

            currentName = Resource.getString("The current application");
        }            

        try {
            switch (SecurityToken.askUserForPermission(classSecurityToken,
                    "Can %1 Interrupt?", question, name, currentName, null,
                    Permissions.BLANKET, pushInterruptSetting)) {
            case Permissions.BLANKET:
                pushInterruptSetting = Permissions.BLANKET_GRANTED;
                return true;

            case Permissions.SESSION:
            case Permissions.ONE_SHOT:
                // treat one shot as session
                pushInterruptSetting = Permissions.SESSION;
                return true;

            case Permissions.DENY:
                pushInterruptSetting = Permissions.USER_DENIED;
                return false;
            }
        } catch (InterruptedException ie) {
            return false;
        }

        // default, is cancel, ask again next time
        pushInterruptSetting = Permissions.DENY_SESSION;
        return false;
    }

    /**
     * Indicates if this suite is trusted.
     * (not to be confused with a domain named "trusted",
     * this is used to determine if a trusted symbol should be displayed
     * to the user and not used for permissions)
     *
     * @return true if the suite is trusted false if not
     */
    public boolean isTrusted() {
        return trusted;
    }

    /**
     * Indicates if the named MIDlet is registered in the suite
     * with MIDlet-&lt;n&gt; record in the manifest or
     * application descriptor.
     * @param midletName class name of the MIDlet to be checked
     *
     * @return true if the MIDlet is registered
     */
    public boolean isRegistered(String midletName) {
        String midlet;
        MIDletInfo midletInfo;

        for (int i = 1; ; i++) {
            midlet = getProperty("MIDlet-" + i);
            if (midlet == null) {
                return false; // We went past the last MIDlet
            }

	    /* Check if the names match. */
            midletInfo = new MIDletInfo(midlet);
            if (midletInfo.classname.equals(midletName)) {
                return true;
            }
        }
    }

    /**
     * Gets the Push interrupt question the should be used when
     * interrupting this suite.
     * <p>
     * The question will have %2 where this suite name should be and
     * a %1 where the current suite name should be.
     *
     * @return push interrupt question
     */
    protected String getPushInterruptQuestion() {
        return PUSH_INTERRUPT_QUESTION;
    }

    /**
     * Gets the Alarm interrupt question the should be used when
     * interrupting this suite.
     * <p>
     * The question will have %2 where this suite name should be and
     * a %1 where the current suite name should be.
     *
     * @return alarm interrupt question
     */
    protected String getAlarmInterruptQuestion() {
        return ALARM_INTERRUPT_QUESTION;
    }

    /**
     * Gets the suite name for interruption purposes.
     *
     * @return name for interrupt question
     */
    protected String getSuiteNameForInterrupt() {
        return getProperty(Installer.SUITE_NAME_PROP);
    }
    
    /**
     * Gets the JAD URL of the suite. This is only for the installer.
     *
     * @return URL of the JAD can be null
     */
    public String getJadUrl() {
        RandomAccessStream storage =
            new RandomAccessStream(classSecurityToken);
        DataInputStream storageStream;

        try {
            storage.connect(getStorageRoot() + Installer.JAD_URL_FILENAME,
                            Connector.READ);

            // convert the JAD URL to UTF8 and write it to storage
            storageStream = storage.openDataInputStream();
            return storageStream.readUTF();
        } catch (Exception e) {
            // ignore, not all suite have JAD URLs
            return null;
        } finally {
            try {
                storage.disconnect();
            } catch (IOException e) {
                // ignore
            }
        }

    }

    /**
     * Gets the JAR URL of the suite. This is only for the installer.
     *
     * @return URL of the JAR, never null, even in development environments
     */
    public String getJarUrl() {
        RandomAccessStream storage =
            new RandomAccessStream(classSecurityToken);
        DataInputStream storageStream;

        try {
            storage.connect(getStorageRoot() + Installer.JAR_URL_FILENAME,
                            Connector.READ);

            // convert the JAR URL to UTF8 and write it to storage
            storageStream = storage.openDataInputStream();
            return storageStream.readUTF();
        } catch (Exception e) {
            // old installations did not have JAR URL's
            return "unknown";
        } finally {
            try {
                storage.disconnect();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
