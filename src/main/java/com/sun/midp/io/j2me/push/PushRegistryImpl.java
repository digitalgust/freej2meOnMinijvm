/*
 * @(#)PushRegistryImpl.java	1.67 02/10/14 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io.j2me.push;

import java.lang.ClassNotFoundException;

import com.sun.midp.io.Util;
import com.sun.midp.io.HttpUrl;

import com.sun.midp.midletsuite.Installer;

import com.sun.midp.midlet.Scheduler;
import com.sun.midp.midlet.MIDletSuite;

import java.io.InterruptedIOException;
import java.io.IOException;

import javax.microedition.midlet.MIDlet;

import javax.microedition.io.Connector;
import javax.microedition.io.ConnectionNotFoundException;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;
import com.sun.midp.security.ImplicitlyTrustedClass;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Inbound push connection watcher.
 */
public class PushRegistryImpl 
    implements Runnable, ImplicitlyTrustedClass {

    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken;

    /** 
     * Flag to control when push launching is permitted. 
     * This flag is set to false by the AMS when installing or removing
     * MIDlets, when an interruption could compromise the integrity of 
     * the operation.
     */
    private static boolean pushEnabled = true;

    /**
     * Run the polling loop to check for inbound connections.
     */    
    public void run() {
	long fd = -1;
	int ret = 0;
	while (true) {
	    try {
		if (pushEnabled
		    && (fd = poll0(System.currentTimeMillis())) != -1) {
		    if (fd != -1) {
			byte[] registryEntry = new byte[512];
			if ((ret = getMIDlet0(fd, registryEntry, 512)) == 0) {
			    String name = Util.toJavaString(registryEntry);
			    launchEntry(name);
			} else {
			    // NYI - can't find entry after successful poll
			}
		    }
		}

		Thread.sleep(1000);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Parse the registration entry and launch the associtated 
     * <code>MIDlet</code>.
     * @param name registration string for connection and 
     * <code>MIDlet</code> to be launched
     */
    protected void launchEntry(String name) {
        String conn;
        String midlet;
        String filter;
        String storage;

	/* 	     
	 * Parse the comma separated values  - 	     
	 *  " connection, midlet,  filter, storagename"
	 *  "  midlet,  wakeup, storagename"
	 */
        int comma1 = name.indexOf(',', 0);
        int comma2 = name.indexOf(',', comma1 + 1);
        int comma3 = name.indexOf(',', comma2 + 1);
	    
        if (comma3 == -1) {
            /* Alarm was triggered */
            conn = null;
            midlet = name.substring(0, comma1).trim();
            storage = name.substring(comma2+1).trim();
        } else {
            conn = name.substring(0, comma1).trim();
            midlet = name.substring(comma1+1, comma2).trim();
            filter = name.substring(comma2+1, comma3).trim();
            storage = name.substring(comma3+1).trim();
        }

	try {
            boolean okToInterrupt = true;
	    
            /*
             * Check to see if the MIDlet is already scheduled.
             */
            Installer installer = Installer.getInstaller(classSecurityToken);
	    Scheduler scheduler = Scheduler.getScheduler();
	    MIDletSuite current = scheduler.getMIDletSuite();
	    String root = null;
	    if (current != null) {
		root = current.getStorageName();
	    }

	    if ((root != null) && root.equals(storage)) {
		/*
		 * The storage name matches the current running
		 * MIDlet do not start it.
                 */
		if (scheduler.isScheduled(midlet)) {
                    return;
                }

                if (!current.permissionToInterrupt(conn)) {
                    // user does not want the interruption
                    if (conn != null) {
                        checkInConnectionInternal(classSecurityToken, conn);
                    }

                    return;
                } 

                current.saveSettings();
            } else {
                MIDletSuite next = installer.getMIDletSuite(storage);
                if (next == null) {
                    return;
                }

		okToInterrupt = next.permissionToInterrupt(conn);
		next.saveSettings();

                if (!okToInterrupt) {
                    // user does not want the interruption
                    if (conn != null) {
                        checkInConnectionInternal(classSecurityToken, conn);
                    }

                    return;
                }
            }

	    /*
	     * Inform the Installer about the MIDlet to run next.
	     * Need to restart the VM to load the next suite.
	     */
	    installer.execute(storage, midlet);
	    
	    /*
	     * Shutdown all running applications.
	     * NYI, ask the user before doing a forced take down.
	     */
            Scheduler.getScheduler().shutdown();
	} catch (Exception e) {
	    // Could not launch requested push entry
            if (conn != null) {
                checkInConnectionInternal(classSecurityToken, conn);
            }

	    e.printStackTrace();
	}
    }

    /**
     * Register a dynamic connection with the
     * application management software. Once registered,
     * the dynamic connection acts just like a
     * connection preallocated from the descriptor file.
     * The internal implementation includes the storage name
     * that uniquely identifies the <code>MIDlet</code>.
     *
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *               when new external data is available
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
     * @exception  IllegalArgumentException if the connection string is not
     *               valid
     * @exception ConnectionNotFoundException if the runtime system does not
     *              support push delivery for the requested
     *              connection protocol
     * @exception IOException if the connection is already
     *              registered or if there are insufficient resources
     *              to handle the registration request
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *               name can not be found in the current
     *               <code>MIDlet</code> suite
     * @exception SecurityException if the <code>MIDlet</code> does not
     *              have permission to register a connection
     * @see #unregisterConnection
     */
    public static void registerConnection(String connection, String midlet,
                                          String filter)
	throws ClassNotFoundException,
	        IOException {

        Scheduler scheduler = Scheduler.getScheduler();
        MIDletSuite midletSuite = scheduler.getMIDletSuite();

        /* This method should only be called by scheduled MIDlets. */
        if (midletSuite == null) {
            throw new IllegalStateException("Not in a MIDlet context");
        }

	/* Verify the MIDlet is in the current classpath. */
	if (midlet == null || midlet.length() == 0) {
	    throw new ClassNotFoundException("MIDlet missing");
	}

	Class cl = Class.forName(midlet);
	Class m = Class.forName("javax.microedition.midlet.MIDlet");

	if (!m.isAssignableFrom(cl)) {
	    throw new ClassNotFoundException("Not a MIDlet");
	}

	/* Perform the rest of the checks in the internal registry. */
	registerConnectionInternal(classSecurityToken, midletSuite,
            connection, midlet, filter, false);
    }

    /**
     * Check the registration arguments.
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *               when new external data is available
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
     * @exception  IllegalArgumentException if the connection string is not
     *               valid
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *               name can not be found in the current
     *               <code>MIDlet</code> suite
     */
    static void checkRegistration(String connection, String midlet, 
				  String filter) 
	throws ClassNotFoundException {

	/* Verify the MIDlet is in the current classpath. */
	if (midlet == null || midlet.length() == 0) {
	    throw new ClassNotFoundException("MIDlet missing");
	}

	/* Verify that the connection requested is valid. */
	if (connection == null || connection.length() == 0) {
	    throw new IllegalArgumentException("Connection missing");
	}

	/* Verify that the filter requested is valid. */
	if (filter == null || filter.length() == 0) {
	    throw new IllegalArgumentException("Filter missing");
	}

	int len = filter.length();
	for (int i = 0; i < len; i++) {
	    char c = filter.charAt(i);
	    if (!(c == '?' || c == '*' || c == '.' ||
		  ('0' <= c && c <= '9'))) {
		throw new IllegalArgumentException("Filter invalid");
	    }
	}
    }

    /**
     * Register a dynamic connection with the
     * application management software. Once registered,
     * the dynamic connection acts just like a
     * connection preallocated from the descriptor file.
     * The internal implementation includes the storage name
     * that uniquely identifies the <code>MIDlet</code>.
     * This method bypasses the class loader specific checks
     * needed by the <code>Installer</code>.
     *
     * @param token security token of the calling class
     * @param midletSuite MIDlet suite for the suite registering,
     *                   the suite only has to implement isRegistered,
     *                   checkForPermission, and getStorageName.
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *               and <em>port number</em>
     *               (optional parameters may be included
     *               separated with semi-colons (;))
     * @param midlet  class name of the <code>MIDlet</code> to be launched,
     *               when new external data is available
     * @param filter a connection URL string indicating which senders
     *               are allowed to cause the MIDlet to be launched
     * @param bypassChecks if true, bypass the permission checks,
     *         used by the installer when redo old connections during an
     *         aborted update
     *
     * @exception  IllegalArgumentException if the connection string is not
     *               valid
     * @exception ConnectionNotFoundException if the runtime system does not
     *              support push delivery for the requested
     *              connection protocol
     * @exception IOException if the connection is already
     *              registered or if there are insufficient resources
     *              to handle the registration request
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *               name can not be found in the current
     *               <code>MIDlet</code> suite
     * @exception SecurityException if the <code>MIDlet</code> does not
     *              have permission to register a connection
     *
     * @see #unregisterConnection
     */
    public static void registerConnectionInternal(SecurityToken token,
                                                  MIDletSuite midletSuite,
                                                  String connection,
                                                  String midlet,
                                                  String filter,
                                                  boolean bypassChecks)
	throws ClassNotFoundException,
               IOException {

        HttpUrl url;
        String storageName;

	token.checkIfPermissionAllowed(Permissions.MIDP);

        if (!bypassChecks) {
            try {
                midletSuite.checkForPermission(Permissions.PUSH, null);
                /* Check the registration arguments. */
                checkRegistration(connection, midlet, filter);
                    
                /* Check if an appropriate MIDlet-<n> record exists. */
                if (!midletSuite.isRegistered(midlet)) {
                    throw new
                        ClassNotFoundException("No MIDLet-<n> registration");
                }

                /*
                 * Only socket and datagram connections are supported by
                 * the MIDP 2.0 reference implementation. 
                 */
                url = new HttpUrl(connection);

                // Server connections do not have a host
                if (url.host != null) {
                    throw new ConnectionNotFoundException(
                        "Connection not supported");
                }
            
                /*
                 * Attempt to open the connection to perform security check
                 * int the context of the current MIDlet suite.
                 */

                if (connection.startsWith("socket://")) {
                    if (url.port == -1) {
                        new IllegalArgumentException("Port missing");
                    }

                    midletSuite.checkForPermission(Permissions.TCP_SERVER,
                                                   connection);
                } else if (connection.startsWith("datagram://")) {
                    /*
                     * Check the suite permission for the connection 
                     * and close the connection immediately.
                     */
                    midletSuite.checkForPermission(Permissions.UDP_SERVER,
                                                   connection);
                } else {
                    throw new ConnectionNotFoundException(
                        "Connection not supported");
                }
            } catch (InterruptedException ie) {
                throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
            }
        }

	storageName = midletSuite.getStorageName();

        byte[] asciiRegistration = Util.toCString(connection 
						      + "," + midlet 
						      + "," + filter
						      + "," + storageName);

        if (add0(asciiRegistration) == -1) {
            throw new IOException("Connection already registered");
        }
    }

    /**
     * Remove a dynamic connection registration.
     *
     * @param connection generic connection <em>protocol</em>,
     *             <em>host</em> and <em>port number</em>
     * @exception SecurityException if the connection was
     *            not registered by the current <code>MIDlet</code>
     *            suite
     * @return <code>true</code> if the unregistration was successful,
     *         <code>false</code> the  connection was not registered.
     * @see #registerConnection
     */
    public static boolean unregisterConnection(String connection) {
	
	/* Verify the connection string before using it. */
	if (connection == null || connection.length() == 0) {
	    return false;
	}
	Scheduler scheduler = Scheduler.getScheduler();
	MIDletSuite current = scheduler.getMIDletSuite();
	String root = current.getStorageName();

        byte[] asciiRegistration = Util.toCString(connection);
        byte[] asciiStorage = Util.toCString(root);
	int ret =  del0(asciiRegistration, asciiStorage);
	if (ret == -2) {
	    throw new SecurityException("wrong suite");
	}
	return ret != -1;
    }
    /**
     * Check in a push connection into AMS so the owning MIDlet can get
     * launched next time data is pushed. This method is used when a MIDlet
     * will not be able to get the connection and close (check in) the
     * connection for some reason. (normally because the user denied a
     * permission)
     * <p>
     * For datagram connections this function will discard the cached message.
     * <p>
     * For server socket connections this function will close the
     * accepted connection.
     *
     * @param token security token of the calling class
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *              and <em>port number</em>
     *              (optional parameters may be included
     *              separated with semi-colons (;))
     * @exception IllegalArgumentException if the connection string is not
     *              valid
     * @exception SecurityException if the <code>MIDlet</code> does not
     *              have permission to clear a connection
     * @return <code>true</code> if the check in was successful,
     *         <code>false</code> the connection was not registered.
     * @see #unregisterConnection
     */
    public static boolean checkInConnectionInternal(SecurityToken token,
                                                    String connection) {
	int ret;

	token.checkIfPermissionAllowed(Permissions.MIDP);
	/* Verify that the connection requested is valid. */
	if (connection == null || connection.length() == 0) {
	    throw new IllegalArgumentException("Connection missing");
	}
	
	byte[] asciiRegistration = Util.toCString(connection);

	return checkInByName0(asciiRegistration) != -1;
    }

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public void initSecurityToken(SecurityToken token) {
        if (classSecurityToken == null) {
            classSecurityToken = token;
        }
    }

    /**
     * Return a list of registered connections for the current
     * <code>MIDlet</code> suite.
     *
     * @param available if <code>true</code>, only return the list of
     *      connections with input available
     * @return array of connection strings, where each connection is
     *       represented by the generic connection <em>protocol</em>,
     *       <em>host</em> and <em>port number</em> identification
     */
    public static String listConnections(boolean available) {
	MIDletSuite current = Scheduler.getScheduler().getMIDletSuite();

	if (current == null) {
            return null;
        }

        return listConnections(classSecurityToken, current.getStorageName(),
                               available);
    }

    /**
     * Return a list of registered connections for given
     * <code>MIDlet</code> suite.
     *
     * @param storageName identifies the specific <code>MIDlet</code>
     *               suite to be launched
     * @param available if <code>true</code>, only return the list of
     *      connections with input available
     *
     * @return array of connection strings, where each connection is
     *       represented by the generic connection <em>protocol</em>,
     *       <em>host</em> and <em>port number</em> identification
     */
    public static String listConnections(String storageName,
            boolean available) {

        return listConnections(null, storageName, available);
    }

    /**
     * Return a list of registered connections for given
     * <code>MIDlet</code> suite. Root permissions are required.
     *
     * @param token security token of the calling class, or <code>null</code>
     *        to check the suite
     * @param storageName identifies the specific <code>MIDlet</code>
     *               suite to be launched
     * @param available if <code>true</code>, only return the list of
     *      connections with input available
     *
     * @return array of connection strings, where each connection is
     *       represented by the generic connection <em>protocol</em>,
     *       <em>host</em> and <em>port number</em> identification
     */
    public static String listConnections(SecurityToken token,
            String storageName, boolean available) {
        byte[] nativeStorageName;
	String connections = null;
	byte[] connlist;

        if (token == null) {
            MIDletSuite current = Scheduler.getScheduler().getMIDletSuite();

            if (current != null) {
                current.checkIfPermissionAllowed(Permissions.MIDP);
            }
        } else {
            token.checkIfPermissionAllowed(Permissions.MIDP);
        }

        nativeStorageName = Util.toCString(storageName);
	connlist = new byte[512];

        if (list0(nativeStorageName, available, connlist, 512) == 0) {
            connections = Util.toJavaString(connlist);
        }

	return connections;
    }

    /**
     * Unregister all the connections for a <code>MIDlet</code> suite.
     *
     * @param token security token of the calling class
     * @param storageName identifies the specific <code>MIDlet</code>
     *               suite
     */
    public static void  unregisterConnections(SecurityToken token,
                                              String storageName) {
	token.checkIfPermissionAllowed(Permissions.MIDP);

	delAllForSuite0(Util.toCString(storageName));
    }


    /**
     * Retrieve the registered <code>MIDlet</code> for a requested connection.
     *
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *              and <em>port number</em>
     *              (optional parameters may be included
     *              separated with semi-colons (;))
     * @return  class name of the <code>MIDlet</code> to be launched,
     *              when new external data is available, or
     *              <code>null</code> if the connection was not
     *              registered
     * @see #registerConnection
     */
    public static String getMIDlet(String connection) {

	/* Verify that the connection requested is valid. */
	if (connection == null || connection.length() == 0) {
	    return null;
	}

	String midlet = null;
        byte[] asciiConn = Util.toCString(connection);
	byte[] registryEntry = new byte[512];

	if (getEntry0(asciiConn, registryEntry, 512) == 0) {
	    String name = Util.toJavaString(registryEntry);
	    try {
		int comma1 = name.indexOf(',', 0);
		int comma2 = name.indexOf(',', comma1 + 1);
		
		midlet = name.substring(comma1+1, comma2).trim();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return 	midlet;
    }

    /**
     * Retrieve the registered filter for a requested connection.
     *
     * @param connection generic connection <em>protocol</em>, <em>host</em>
     *              and <em>port number</em>
     *              (optional parameters may be included
     *              separated with semi-colons (;))
     * @return a filter string indicating which senders
     *              are allowed to cause the MIDlet to be launched or
     *              <code>null</code> if the connection was not
     *              registered
     * @see #registerConnection
     */
    public static String getFilter(String connection) {

	/* Verify that the connection requested is valid. */
	if (connection == null || connection.length() == 0) {
	    return null;
	}

	String filter = null;
        byte[] asciiConn = Util.toCString(connection);
	byte[] registryEntry = new byte[512];

	if (getEntry0(asciiConn, registryEntry, 512) == 0) {
	    String name = Util.toJavaString(registryEntry);
	    try {
		int comma1 = name.indexOf(',', 0);
		int comma2 = name.indexOf(',', comma1 + 1);
		int comma3 = name.indexOf(',', comma2 + 1);
		
		filter = name.substring(comma2+1, comma3).trim();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	return 	filter;
    }

    /** 
     * Register a time to launch the specified application. The 
     * <code>PushRegistry</code> supports one outstanding wake up
     * time per <code>MIDlet</code> in the current suite. An application
     * is expected to use a <code>TimerTask</code> for notification
     * of time based events while the application is running.
     * <P>If a wakeup time is already registered, the previous value will
     * be returned, otherwise a zero is returned the first time the 
     * alarm is registered. </P>
     *
     * @param midlet  class name of the <code>MIDlet</code> within the
     *                current running <code>MIDlet</code> suite
     *                to be launched,
     *                when the alarm time has been reached
     * @param time time at which the <code>MIDlet</code> is to be executed
     *        in the format returned by <code>Date.getTime()</code>
     * @return the time at which the most recent execution of this 
     *        <code>MIDlet</code> was scheduled to occur, 
     *        in the format returned by <code>Date.getTime()</code>
     * @exception ConnectionNotFoundException if the runtime system does not
     *              support alarm based application launch
     * @exception ClassNotFoundException if the <code>MIDlet</code> class
     *              name can not be found in the current
     *              <code>MIDlet</code> suite
     * @see Date#getTime()
     * @see Timer
     * @see TimerTask
     */
    public static long registerAlarm(String midlet, long time)
	 throws ClassNotFoundException, ConnectionNotFoundException {

        Scheduler scheduler = Scheduler.getScheduler();
        MIDletSuite midletSuite = scheduler.getMIDletSuite();

        /* There is no suite running when installing from the command line. */
        if (midletSuite != null) {
            try {
                midletSuite.checkForPermission(Permissions.PUSH, null);
            } catch (InterruptedException ie) {
                throw new RuntimeException(
                    "Interrupted while trying to ask the user permission");
            }
        }

	/* Verify the MIDlet is in the current classpath. */
	if (midlet == null || midlet.length() == 0) {
	    throw new ClassNotFoundException("MIDlet missing");
	}

	/* Check if an appropriate MIDlet-<n> record exists. */
	if (!midletSuite.isRegistered(midlet)) {
	    throw new ClassNotFoundException("No MIDLet-<n> registration");
	}

	Class c = Class.forName(midlet);
	Class m = Class.forName("javax.microedition.midlet.MIDlet");

	if (!m.isAssignableFrom(c)) {
	    throw new ClassNotFoundException("Not a MIDlet");
	}

	/* 
	 * Add the alarm for the specified MIDlet int the current
	 * MIDlet suite.
	 */
	MIDletSuite current = Scheduler.getScheduler().getMIDletSuite();
	if (current != null) {
	    String root = current.getStorageName();

	    byte[] asciiName = Util.toCString(midlet + "," 
					      + time + "," 
					      + root); 
	    return addAlarm0(asciiName, time);
	}

	return 0;
    }

    /**
     * Sets the flag which enables push launches to take place.
     *
     * @param token security token of the calling class
     * @param enable set to <code>true</code> to enable launching
     *  of MIDlets based on alarms and connection notification 
     *  events, otherwise set to <code>false</code> to disable
     *  launches
     */
    public static void enablePushLaunch(SecurityToken token,
					boolean enable) {
	token.checkIfPermissionAllowed(Permissions.MIDP);

	pushEnabled = enable;
    }

    /**
     * Native connection registry add connection function.
     * @param connection string to register
     * @return 0 if successful, -1 if failed
     */
    private static native int add0(byte[] connection);

    /**
     * Native function to test registered inbound connections 
     * for new connection notification.
     * @param time current time to use for alarm checks
     * @return handle for the connection with inbound connection
     *         pending.
     */
    private native long poll0(long time);
    
    /**
     * Native connection registry lookup for MIDlet name from file 
     * descriptor.
     * @param handle file descriptor of registered active connection
     * @param regentry registered entry
     * @param entrysz maximum string that will be accepted 
     * @return 0 if successful, -1 if failed
     */
    private static native int getMIDlet0(long handle, byte[] regentry, 
					 int entrysz);
    
    /**
     * Native connection registry lookup registry entry from a 
     * specific connection.
     * @param connection registered connection string
     * @param regentry registered entry
     * @param entrysz maximum string that will be accepted 
     * @return 0 if successful, -1 if failed
     */
    private static native int getEntry0(byte[]connection, byte[] regentry, 
					 int entrysz);
 
    /**
     * Native connection registry add alarm function.
     * @param midlet string to register
     * @param time
     * @return 0 if unregistered, otherwise the time of the previous
     *         registered alarm
     */
    private static native long addAlarm0(byte[] midlet, long time);
 
    /**
     * Native connection registry del connection function.
     * @param connection string to register
     * @param storage current suite storage name
     * @return 0 if successful, -1 if failed
     */
    private static native int del0(byte[] connection, byte[] storage);
 
    /**
     * Native connection registry check in connection function.
     * @param connection string to register
     * @return 0 if successful, -1 if failed
     */
    private static native int checkInByName0(byte[] connection);
 
    /**
     * Native connection registry list connection function.
     * @param midlet string to register
     * @param available if <code>true</code>, only return the list of
     *      connections with input available
     * @param connectionlist comma separated string of connections
     * @param listsz maximum string that will be accepted in connectionlist
     * @return 0 if successful, -1 if failed
     */
    private static native int list0(byte[] midlet, boolean available, 
				    byte[] connectionlist, int listsz);
 
    /**
     * Native connection registry delete a suite's connections function.
     * @param storageName native ASCII representation of a suite's storage name
     */
    private static native void delAllForSuite0(byte[] storageName);


}
