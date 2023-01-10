/*
 * @(#)Protocol.java	1.13 02/10/14 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io.j2me.ssl;

import java.io.*;

import javax.microedition.io.*;

import javax.microedition.pki.*;

import com.sun.cldc.io.*;

import com.sun.midp.ssl.*;

import com.sun.midp.io.*;

import com.sun.midp.midlet.*;

import com.sun.midp.publickeystore.*;

import com.sun.midp.security.*;

/**
 * This class implements the necessary functionality
 * for an SSL connection.
 */
public class Protocol implements SecureConnection, ConnectionBaseInterface {

    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken;
    
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

    /** Underlying TCP connection. */
    private com.sun.midp.io.j2me.socket.Protocol tcpConnection;

    /** Underlying SSL connection. */
    private SSLStreamConnection sslConnection;

    /**
     * Connect to the underlying secure socket transport.
     *
     * @param name       the target of the connection
     * @param mode       a flag that is true if the caller
     *                   intends to write to the connection, ignored
     * @param timeouts   a flag to indicate that the called
     *                   wants timeout exceptions, ignored
     *
     * @return SSL/TCP stream connection
     *
     * @exception IOException is thrown if the connection cannot be opened
     * @exception IllegalArgumentException if the name is bad
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
            throws IOException {
        Scheduler scheduler = Scheduler.getScheduler();
        MIDletSuite midletSuite = scheduler.getMIDletSuite();
        HttpUrl url;
        OutputStream tcpOutputStream;
        InputStream tcpInputStream;

        try {
            midletSuite.checkForPermission(Permissions.SSL, "ssl:" + name);
        } catch (InterruptedException ie) {
            throw new InterruptedIOException(
                "Interrupted while trying to ask the user permission");
        }

        if (name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException(
                      "Protocol must start with \"//\"");
        }

        url = new HttpUrl("ssl", name); // parse name into host and port

        /*
         * Since we reused the HttpUrl parser, we must make sure that
         * there was nothing past the authority in the URL.
         */
        if (url.path != null || url.query != null || url.fragment != null) {
            throw new IllegalArgumentException("Malformed address");
        }

        tcpConnection = new com.sun.midp.io.j2me.socket.Protocol();
        tcpConnection.openPrim(classSecurityToken, "//" + url.authority);
        try {
            tcpOutputStream = tcpConnection.openOutputStream();
            try {
                tcpInputStream = tcpConnection.openInputStream();

                /*
                 * Porting note: This would be the place to connect to a 
                 *               SOCKS proxy if desired.
                 */

                try {
                    /*
                     * To save memory for applications the do not use SSL,
                     * the public keys of the certificate authorities may not
                     * have been loaded yet.
                     */
                    WebPublicKeyStore.loadCertificateAuthorities();

                    // Get the SSLStreamConnection
                    sslConnection = new SSLStreamConnection(url.host, url.port,
                                        tcpInputStream, tcpOutputStream);
                } catch (IOException e) {
                    tcpInputStream.close();
                    throw e;
                }    
            } catch (IOException e) {
                tcpOutputStream.close();
                throw e;
            }    
        } catch (IOException e) {
            tcpConnection.close();
            throw e;
        }

        return this;
    }

    /**
     * Close the connection to the target.
     *
     * @exception IOException  If an I/O error occurs
     */
    public void close() throws IOException {
        try {
            sslConnection.close();
        } finally {
            tcpConnection.close();
        }
    }

    /**
     * Returns an input stream.
     *
     * @return     an input stream for writing bytes to this port.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    public InputStream openInputStream() throws IOException {
        return sslConnection.openInputStream();
    }

    /**
     * Open and return a data input stream for a connection.
     *
     * @return                 An input stream
     * @exception IOException  If an I/O error occurs
     */
    public DataInputStream openDataInputStream() throws IOException {
        return sslConnection.openDataInputStream();
    }

    /**
     * Returns an output stream.
     *
     * @return     an output stream for writing bytes to this port.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    public OutputStream openOutputStream() throws IOException {
        return sslConnection.openOutputStream();
    }

    /**
     * Open and return a data output stream for a connection.
     *
     * @return                 An input stream
     * @exception IOException  If an I/O error occurs
     */
    public DataOutputStream openDataOutputStream() throws IOException {
        return sslConnection.openDataOutputStream();
    }

    /**
     * Set a socket option for the connection.
     * <P>
     * Options inform the low level networking code about intended 
     * usage patterns that the application will use in dealing with
     * the socket connection. 
     * </P>
     *
     * @param option socket option identifier (KEEPALIVE, LINGER, 
     * SNDBUF, RCVBUF, or DELAY)
     * @param value numeric value for specified option (must be positive)
     * @exception  IllegalArgumentException if  the value is not 
     *              valid (e.g. negative value)
     * @exception  IOException  if the connection was closed
     *              
     * @see #getSocketOption
     */
    public void setSocketOption(byte option,  int value) 
	throws IllegalArgumentException, IOException {
	tcpConnection.setSocketOption(option, value);
    }
    
    /**
     * Get a socket option for the connection.
     *
     * @param option socket option identifier (KEEPALIVE, LINGER, 
     * SNDBUF, RCVBUF, or DELAY)
     * @return positive numeric value for specified option or -1 if the 
     *  value is not available.
     * @exception IllegalArgumentException if the option identifier is 
     *  not valid
     * @exception  IOException  if the connection was closed
     * @see #setSocketOption
     */
    public  int getSocketOption(byte option) 
	throws IllegalArgumentException, IOException {
	return tcpConnection.getSocketOption(option);
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
    public String getLocalAddress()  throws IOException {
	return tcpConnection.getLocalAddress();
    }

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected.
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public int getLocalPort()  throws IOException {
	return tcpConnection.getLocalPort(); 
    }

    /**
     * Gets the remote address to which the socket is bound.
     * The address can be either the remote host name or the IP
     * address(if available).
     *
     * @return the remote address to which the socket is bound.
     * @exception  IOException  if the connection was closed
     */
    public String getAddress()  throws IOException {
	return tcpConnection.getAddress();
    }

    /**
     * Returns the remote port to which this socket is bound.
     *
     * @return the remote port number to which this socket is connected.
     * @exception  IOException  if the connection was closed
     */
    public int getPort()  throws IOException {
	return tcpConnection.getPort(); 
    }

    /**
     * Return the security information associated with this connection.
     * If the connection is still in <CODE>Setup</CODE> state then
     * the connection is initiated to establish the secure connection
     * to the server.  The method returns when the connection is
     * established and the <CODE>Certificate</CODE> supplied by the
     * server has been validated.
     * The <CODE>SecurityInfo</CODE> is only returned if the
     * connection has been successfully made to the server.
     *
     * @return the security information associated with this open connection.
     *
     * @exception CertificateException if the <code>Certificate</code>
     * supplied by the server cannot be validated.
     * The <code>CertificateException</code> will contain
     * the information about the error and indicate the certificate in the
     * validation chain with the error.
     * @exception IOException if an arbitrary connection failure occurs
     */
    public SecurityInfo getSecurityInfo() throws IOException {
        return sslConnection.getSecurityInfo();
    }
}
