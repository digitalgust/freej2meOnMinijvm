/*
 * @(#)Protocol.java	1.52 02/10/14 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io.j2me.datagram;

import java.io.*;

import javax.microedition.io.*;

import com.sun.cldc.io.ConnectionBaseInterface;
import com.sun.cldc.io.GeneralBase;

import com.sun.midp.io.*;

import com.sun.midp.io.j2me.push.*;

import com.sun.midp.midlet.*;

import com.sun.midp.security.*;

/**
 * This is the default "datagram://" protocol for J2ME that maps onto UDP.
 *
 * @author  Nik Shaylor
 * @version 1.1 11/19/99
 */
public class Protocol implements UDPDatagramConnection,
    ConnectionBaseInterface {

    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken;
    
    /** Initialize the native network. */
    static {
        NetworkConnectionBase.initializeNativeNetwork();
    }

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public static void initSecurityToken(SecurityToken token) {
	if (classSecurityToken != null) {
	    return;
	}
	
	classSecurityToken = token;
    }

    /** Used by native code to hold the file descriptor handle. */
    private int handle;

    /** Machine name from the URL connection string. */
    private String host;

    /** Port number from the URL connection string. */
    private int port;

    /** Open flag to indicate if the connection is currently open. */
    private boolean open;

    /**
     * Open a connection to a target.
     * <p>
     * The name string for this protocol should be:
     * "//[address:][port]"
     *
     * @param name       the target of the connection
     * @param mode       a flag that is <code>true</code> if the caller
     *                   intends to write to the connection, ignored
     * @param timeouts   a flag to indicate that the called
     *                   wants timeout exceptions, ignored
     * @return this connection
     * @exception IllegalArgumentException if a parameter is invalid
     * @throws IOException if an I/O operation failed
     * @exception SecurityException if a caller is not authorized for UDP
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
            throws IOException {
        int incommingPort = 0;
        Scheduler scheduler = Scheduler.getScheduler();
        MIDletSuite midletSuite = scheduler.getMIDletSuite();

        // parse name into host and port
        HttpUrl url = new HttpUrl("datagram", name);

        /*
         * Since we reused the <code>HttpUrl</code> parser, we must
	 * make sure that there was nothing past the authority in the
	 * URL.
         */
        if (url.path != null || url.query != null || url.fragment != null) {
            throw new IllegalArgumentException("Malformed address");
        }

        host = url.host;
        port = url.port;

        if (name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException(
                      "Protocol must start with slash slash");
        }

	/*
         * If 'host' == null then we are a server endpoint at
         * port 'port'.
         *
         * If 'host' != null we are a client endpoint at a port
         * decided by the system and the default address for
         * datagrams to be send is 'host':'port'
	 *
	 * If 'host' and 'port' are omitted in 
	 * the name, then 
	 * the application is requesting a system assigned port
	 * number.
         */
        if (host == null) {
            try {
                // When asking permission use Internet protocol name.
                midletSuite.checkForPermission(Permissions.UDP_SERVER,
                    "UDP:" + name);
            } catch (SecurityException e) {
                // Give back the connection to AMS
                PushRegistryImpl.checkInConnectionInternal(
                    classSecurityToken, "datagram:" + name);
                    
                throw e;
            } catch (InterruptedException ie) {
                throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
            }

            if (port > 0) {
                incommingPort = port;
            }
        } else {
            if (port < 0) {
                throw new IllegalArgumentException("Missing port number");
            }

            try {
                // When asking permission use Internet protocol name.
                midletSuite.checkForPermission(Permissions.UDP,
                                               "UDP:" + name);
            } catch (InterruptedException ie) {
                throw new InterruptedIOException(
                    "Interrupted while trying to ask the user permission");
            }
        }

	/* Check the mode parameter. (See NetworkConnectionAdapter). */
        switch (mode) {
        case Connector.READ:
        case Connector.WRITE:
        case Connector.READ_WRITE:
            break;

        default:
            throw new IllegalArgumentException("Illegal mode");
        }

	String root = midletSuite.getStorageName();

        byte[] asciiStorage = Util.toCString(root);

        open0(incommingPort, asciiStorage);
        registerCleanup();
        open = true;
        return this;
    }

    /**
     * Ensure that the connection is open.
     * @exception  IOException  if the connection was closed
     */
    void ensureOpen() throws IOException {
        if (!open) {
            throw new IOException("Connection closed");
        }
    }

    /**
     * Get the maximum length a datagram can be.
     *
     * @return  the length
     * @exception  IOException  if the connection was closed
     */
    public int getMaximumLength() throws IOException {
        ensureOpen();
        return getMaximumLength0();
    }

    /**
     * Get the nominal length of a datagram.
     *
     * @return    address      the length
     * @exception  IOException  if the connection was closed
     */
    public int getNominalLength() throws IOException {
        ensureOpen();
        return getNominalLength0();
    }

    /**
     * Send a datagram.
     *
     * @param     dgram        a datagram
     * @exception IOException  if an I/O error occurs
     */
    public void send(Datagram dgram) throws IOException {
        synchronized (dgram) {
            int length;
            int ipNumber;
            int port;

            ensureOpen();

            length = dgram.getLength();

            // allow zero length datagrams to be sent
            if (length < 0) {
                throw new IOException("Bad datagram length");
            }

            if (dgram instanceof DatagramObject) {
                DatagramObject dh = (DatagramObject)dgram;

                ipNumber = dh.ipNumber;
                if (ipNumber == 0) {
                    throw new IOException(
                        "No address in datagram");
                }

                port = dh.port;
            } else {
                // address is a datagram url
                String addr;
                HttpUrl url;
                String host;

                addr = dgram.getAddress();
                if (addr == null) {
                    throw new IOException(
                         "No address in datagram");
                }

                url = new HttpUrl(addr);
                host = url.host;
                port = url.port;

                if (host == null) {
                    throw new IOException("Missing host");
                }
            
                if (port == -1) {
                    throw new IOException("Missing port");
                }

                ipNumber = getIpNumber(Util.toCString(host));

                if (ipNumber == -1) {
                    throw new IOException("Invalid host");
                }
            }

            while (true) {
                int res;

                try {
                    res = send0(ipNumber, port, dgram.getData(),
                                dgram.getOffset(), length);
                } finally {
                    if (!open) {
                        throw new InterruptedIOException("Socket closed");
                    }
                }

                if (res == dgram.getLength()) {
                    break;
                }

                if (res != 0) {
                    throw new IOException("Failed to send datagram");
                }

                /* Wait a while for I/O to become ready */
                GeneralBase.iowait(); 
            }
        }
    }

    /**
     * Receive a datagram.
     *
     * @param     dgram        a datagram
     * @exception IOException  if an I/O error occurs
     */
    public synchronized void receive(Datagram dgram)
        throws IOException {

        synchronized (dgram) {
            int length;
            long res;
            int count;
            int ipNumber;
            String host;
            int port;
            String addr;

            ensureOpen();

            length = dgram.getLength();

            if (length <= 0) {
                throw new IOException("Bad datagram length");
            }

            while (true) {
                try {
                    res = receive0(dgram.getData(), dgram.getOffset(),
                                   length);
                } finally {
                    if (!open) {
                        throw new InterruptedIOException("Socket closed");
                    }
                }

                // check res, not count so we can receive zero length datagrams
                if (res != 0) {
                    break;
                }

                /* Wait a while for I/O to become ready */
                GeneralBase.iowait(); 
            }

            count = ((int)res) & 0xffff;

            /*
             * There should be another field for bytes received so
             * the datagram can be reused without an extra effort, but
             * to be consistant with J2SE DatagramSocket we shrink the buffer
             * length.
             */
            dgram.setLength(count);

            ipNumber = (int)((res >> 32));
            host = getHostByAddr(ipNumber).trim();
            port = (int)((res >> 16)) & 0xffff;
            addr = "datagram://" + host + ":" + port;

            if (dgram instanceof DatagramObject) {
                // save this data for sending back a message
                DatagramObject dh = (DatagramObject)dgram;
                dh.address = addr;
                dh.ipNumber = ipNumber;
                dh.port = port;
            } else {
                dgram.setAddress("datagram://" + host + ":" + port);
            }
        }
    }

    /**
     * Close the connection to the target.
     *
     * @exception IOException  if an I/O error occurs
     */
    public void close() throws IOException {
        if (open) {
            open = false;
            close0();
        } 

    }

    /**
     * Get a new datagram object.
     *
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @return                 a new datagram
     * @exception IOException  if an I/O error occurs
     * @exception IllegalArgumentException if the length is negative
     *                                     or larger than the buffer
    */
    public Datagram newDatagram(int size) throws IOException {

        Datagram dgram;

        ensureOpen();

        if (size < 0) {
            throw new IllegalArgumentException("Size is negative");
        }

	byte[] buf = new byte[size];

        dgram = new DatagramObject(buf, size);

        if (host != null) {
	    try {
		dgram.setAddress("datagram://" + host + ":" + port);
	    } catch (IllegalArgumentException iae) {
		// Intercept a bad address, here.
		// It'll be caught on send if used.
	    }
        }

        return dgram;
    }

    /**
     * Get a new datagram object.
     *
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @param     addr         the address to which the datagram must go
     * @return                 a new datagram
     * @exception IOException  if an I/O error occurs
     * @exception IllegalArgumentException if the length is negative or
     *                         larger than the buffer, or if the address 
     *                         parameter is invalid
     */
    public Datagram newDatagram(int size, String addr) throws IOException {
        Datagram dgram = createDatagram(true, null, size);
        dgram.setAddress(addr); // override the address
        return dgram;
    }

    /**
     * Get a new datagram object.
     *
     * @param  buf             the buffer to be used in the datagram
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @return                 a new datagram
     * @exception IOException  if an I/O error occurs
     * @exception IllegalArgumentException if the length is negative or
     *                         larger than the buffer, or if the address
     *                         or buffer parameters is invalid
     */
    public Datagram newDatagram(byte[] buf, int size) throws IOException {
        return createDatagram(false, buf, size);
    }

    /**
     * Get a new datagram object.
     *
     * @param  buf             the buffer to be used in the datagram
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @param     addr         the address to which the datagram must go
     * @exception IOException  if an I/O error occurs
     * @return                 a new datagram
     */
    public Datagram newDatagram(byte[] buf, int size, String addr)
        throws IOException {

        Datagram dgram = createDatagram(false, buf, size);
        dgram.setAddress(addr); // override the address
        return dgram;
    }

    /**
     * Create a new datagram object with error checking.
     * If there is a <code>host</code> associated with the connection,
     * set the address of the datagram.
     *
     * @param  createBuffer    if true the buffer is created
     * @param  buf             the buffer to be used in the datagram
     * @param  size            the length of the buffer to be allocated
     *                         for the datagram
     * @return                 a new datagram
     * @exception IOException  if an I/O error occurs, or the connection 
     *                         was closed
     * @exception IllegalArgumentException if the length is negative or
     *                         larger than the buffer, or if the address
     *                         or buffer parameters is invalid
     */
    private Datagram createDatagram(boolean createBuffer, byte[] buf, int size)
        throws IOException {

        Datagram dgram;

        ensureOpen();

        if (size < 0) {
            throw new IllegalArgumentException("Size is negative");
        }

        if (createBuffer) {
            buf = new byte[size];
        } else if (buf == null) {
            throw new IllegalArgumentException("Buffer is invalid");
        } else if (size > buf.length) {
            throw new
                IllegalArgumentException("Size bigger than the buffer");
        }

        dgram = new DatagramObject(buf, size);

        if (host != null) {
            dgram.setAddress("datagram://" + host + ":" + port);
        }

        return dgram;
    }

    /**
     * Gets the local address to which the socket is bound.
     *
     * <P>The host address(IP number) that can be used to connect to this
     * end of the socket connection from an external system. 
     * Since IP addresses may be dynamically assigned a remote application
     * will need to be robust in the face of IP number reasssignment.</P>
     * <P> The local hostname (if available) can be accessed from 
     * <code>System.getProperty("microedition.hostname")</code>
     * </P>
     *
     * @return the local address to which the socket is bound.
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public  String getLocalAddress() throws IOException {
        ensureOpen();
	return getHost0();
    }

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public  int  getLocalPort() throws IOException {
        ensureOpen();
	return getPort0(); 
    }

    /**
     * Open a connection to a target, and fillin the handle field.
     *
     * @param port       port to listen on, or 0 to have one selected
     * @param storage    name of current suite storage
     *
     * @exception IOException  if some other kind of I/O error occurs
     *  or if reserved by another suite
     */
    private native void open0(int port, byte[] storage) throws IOException;

    /**
     * Send a datagram, handle field is accessed by the native code.
     *
     * @param ipNumber raw IPv4 address
     * @param port UDP port
     * @param buf the data buffer
     * @param off the offset into the data buffer
     * @param len the length of the data in the buffer
     * @return number of bytes to send
     * @exception IOException  if an I/O error occurs
     */
    private native int send0(int ipNumber, int port, byte[] buf, int off,
        int len) throws IOException;

    /**
     * Receive a datagram, handle field is accessed by the native code.
     *
     * @param buf the data buffer
     * @param off the offset into the data buffer
     * @param len the length of the data in the buffer
     * @return upper 32 bits: raw IPv4 address, middle 16 bits: port, bytes
     *         in datagram
     * @exception IOException  if an I/O error occurs
     */
    private native long receive0(byte[] buf, int off, int len)
        throws IOException;

    /**
     * Close the native socket, handle field is accessed by the native code.
     *
     * @exception IOException  if an I/O error occurs
     */
    private native void close0()
        throws IOException;

    /** Register this object's native cleanup function. */
    private native void registerCleanup();

    /**
     * Get a hostname for a raw IPv4 address.
     *
     * @param ipn raw IPv4 address
     * @return hostname or the dot notation if not found
     */
    static native String getHostByAddr(int ipn);

    /**
     * Get a raw IPv4 address for hostname.
     *
     * @param szHost hostname as ASCII chars with zero terminator
     * @return raw IPv4 address or -1 if there was an error
     */
    static native int getIpNumber(byte[] szHost);

    /**
     * Get the maximum length of a datagram.
     *
     * @return maximum length of a datagram
     *
     * @exception IOException  if an I/O error occurs
     */
    native int getMaximumLength0()
        throws IOException;

    /**
     * Get the nominal length of a datagram.
     *
     * @return nomimal length of a datagram
     *
     * @exception IOException  if an I/O error occurs
     */
    native int getNominalLength0()
        throws IOException;

    /**
     * Native finalizer.
     */
    private native void finalize();

    /**
     * Get the requested IP number.
     *
     * @return     the IP address as a String
     */
    private native String getHost0();

    /**
     * Get the requested port number.
     *
     * @return     the port number of the requested end point
     */
    private native int getPort0();
}
