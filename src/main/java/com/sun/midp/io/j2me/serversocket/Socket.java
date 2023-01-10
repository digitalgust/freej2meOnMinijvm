/*
 * @(#)Socket.java	1.15 02/10/14 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io.j2me.serversocket;

import java.io.*;

import javax.microedition.io.*;

import com.sun.cldc.io.ConnectionBaseInterface;
import com.sun.cldc.io.GeneralBase;

import com.sun.midp.io.*;

import com.sun.midp.midlet.*;

import com.sun.midp.security.*;

/*
 * Note: Since this class references the TCP socket protocol class that
 * extends the NetworkConnectionBaseClass. The native networking will be
 * initialized when this class loads if needed without extending
 * NetworkConnectionBase.
 */
/**
 * StreamConnectionNotifier to the TCP Server Socket API.
 *
 * @author  Nik Shaylor
 * @version 1.0 10/08/99
 */
public class Socket implements ServerSocketConnection {

    /** Socket object used by native code, for now must be the first field. */
    private int handle;

    /** Flag to indicate connection is currently open. */
    boolean connectionOpen = false;

    /**
     * Opens a port to listen on.
     *
     * @param port       TCP to listen on
     *
     * @exception IOException  if some other kind of I/O error occurs
     */
    public void open(int port) throws IOException {
        Scheduler scheduler = Scheduler.getScheduler();
        MIDletSuite midletSuite = scheduler.getMIDletSuite();
	String root = midletSuite.getStorageName();

        byte[] asciiStorage = Util.toCString(root);

        open0(port > 0 ? port : 0, asciiStorage);
	connectionOpen = true;

        port = getLocalPort();

        // check permission after the open so we can get the assigned port
        try {
            // When asking permission use Internet protocol name.
            midletSuite.checkForPermission(Permissions.TCP_SERVER,
                "TCP://:" + port);
        } catch (SecurityException e) {
            close();
            throw e;
        } catch (InterruptedException ie) {
            throw new InterruptedIOException(
                "Interrupted while trying to ask the user permission");
        }

        registerCleanup();
    }

    /**
     * Checks if the connection is open.
     *
     * @exception  IOException  is thrown, if the stream is not open
     */
    void ensureOpen() throws IOException {
        if (!connectionOpen) {
            throw new IOException("Connection closed");
        }
    }

    /**
     * Opens a native socket and put its handle in the handle field.
     * <p>
     * Called by socket Protocol class after it parses a given URL and finds
     * no host.
     *
     * @param port       TCP port to listen for connections on
     * @param storage    name of current suite storage
     *
     * @exception IOException  if some other kind of I/O error occurs
     *  or if reserved by another suite
     */
    public native void open0(int port, byte[] storage) throws IOException;

    /**
     * Returns a connection that represents a server side
     * socket connection.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @return     a socket to communicate with a client.
     *
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream
     */
    synchronized public StreamConnection acceptAndOpen()
        throws IOException {

        com.sun.midp.io.j2me.socket.Protocol con;

	ensureOpen();

        while (true) {
            int handle = accept();
            if (handle >= 0) {
                con = new com.sun.midp.io.j2me.socket.Protocol();
                con.open(handle);
                break;
            }

            /* Wait a while for I/O to become ready */
            GeneralBase.iowait(); 
        }

        return con;
    }

    /**
     * Gets the local address to which the socket is bound.
     *
     * <P>The host address(IP number) that can be used to connect to this
     * end of the socket connection from an external system. 
     * Since IP addresses may be dynamically assigned, a remote application
     * will need to be robust in the face of IP number reasssignment.</P>
     * <P> The local hostname (if available) can be accessed from 
     * <code> System.getProperty("microedition.hostname")</code>
     * </P>
     *
     * @return the local address to which the socket is bound
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public String getLocalAddress() throws IOException {
	ensureOpen();
	return getLocalAddress0();
    }

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public int getLocalPort() throws IOException {
	ensureOpen();
	return getLocalPort0();
    }

    /**
     * Closes the connection, accesses the handle field.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection
     */
    public void close() throws IOException {
	if (connectionOpen) {
	    close0();
	    connectionOpen = false;
	}
    }

    /**
     * Accepts a TCP connection socket handle to a client,
     * accesses the handle field.
     *
     * @return TCP connection socket handle
     *
     * @exception IOException  If some other kind of I/O error occurs.
     */
    private native int accept() throws IOException;

    /**
     * Closes the connection, accesses the handle field.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection
     */
    public native void close0() throws IOException;

    /**
     * Registers with the native cleanup code, accesses the handle field.
     */
    private native void registerCleanup();

    /**
     * Native finalizer
     */
    private native void finalize();

    /**
     * Gets the requested printable IP number.
     *
     * @return     the IP address as a String
     */
    private native String getLocalAddress0();

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected
     */
    private native int getLocalPort0();
}


