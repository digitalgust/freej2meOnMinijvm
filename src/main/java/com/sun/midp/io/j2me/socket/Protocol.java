/*
 * @(#)Protocol.java	1.22 02/09/20 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io.j2me.socket;

import java.io.*;

import javax.microedition.io.*;

import com.sun.cldc.io.GeneralBase;

import com.sun.midp.main.Configuration;

import com.sun.midp.io.*;

import com.sun.midp.midlet.*;

import com.sun.midp.security.*;

/** Connection to the J2ME socket API. */
public class Protocol extends NetworkConnectionBase
        implements SocketConnection {

    /** Size of the read ahead buffer, default is no buffering. */
    protected static int bufferSize;

    /**
     * Class initializer
     */
    static {
        /* See if a read ahead / write behind buffer size has been specified */
        String size = Configuration.getProperty(
                          "com.sun.midp.io.j2me.socket.buffersize");
        if (size != null) {
            try {
                bufferSize = Integer.parseInt(size);
            } catch (NumberFormatException ex) {}
        }
    }

    /** Hostname */
    private String host;

    /** TCP port */
    private int port;

    /** Shutdown output flag, true if output has been shutdown. */
    private boolean outputShutdown;

    /** Creates a buffered TCP client connection. */
    public Protocol() {
        // use the default buffer size
        super(bufferSize);

        // When asking permission use Internet protocol name.
        protocol = "TCP";
        requiredPermission = Permissions.TCP;
    }

    /**
     * Open a client or server socket connection.
     * <p>
     * The name string for this protocol should be:
     * "socket://&lt;name or IP number&gt;:&lt;port number&gt;
     * <p>
     * We allow "socket://:nnnn" to mean an inbound server socket connection.
     *
     * @param name       the target for the connection
     * @param mode       I/O access mode
     * @param timeouts   a flag to indicate that the caller wants
     *                   timeout exceptions
     *
     * @return client or server TCP socket connection
     *
     * @exception  IOException  if an I/O error occurs.
     * @exception  ConnectionNotFoundException  if the host cannot be connected
     *              to
     * @exception  IllegalArgumentException  if the name is malformed
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
        throws IOException {

        HttpUrl url;
        if (name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException(
                      "Protocol must start with \"//\"");
        }

        url = new HttpUrl("socket", name); // parse name into host and port

        /*
         * Since we reused the HttpUrl parser, we must make sure that
         * there was nothing past the authority in the URL.
         */
        if (url.path != null || url.query != null || url.fragment != null) {
            throw new IllegalArgumentException("Malformed address");
        }

        host = url.host;
        port = url.port;
        
        /*
         * If 'host' == null then we are a server endpoint at
         * port 'port'.
         */

        if (host != null) {
            // this will call the connect method which uses the host and port
            return super.openPrim(name, mode, timeouts);
        }

        // We allow "socket://:nnnn" to mean an inbound TCP server socket.
        com.sun.midp.io.j2me.serversocket.Socket con;
        con = new com.sun.midp.io.j2me.serversocket.Socket();
        con.open(port);
        return con;
    }

    /**
     * Connect to a server.
     * @param name       the target for the connection
     * @param mode       I/O access mode
     * @param timeouts   a flag to indicate that the caller wants
     *                   timeout exceptions
     * <p>
     * The name string for this protocol should be:
     * "socket://&lt;name or IP number&gt;:&lt;port number&gt;
     * @exception  IOException  if an I/O error occurs.
     * @exception  ConnectionNotFoundException  if the host cannot be connected
     *              to
     * @exception  IllegalStateException  if there is no hostname
     * @exception  IllegalArgumentException  if the name is malformed
     */
    public void connect(String name, int mode, boolean timeouts)
        throws IOException {

        byte[] szHost;

        verifyPermissionCheck();

        /*
         * The host and port were set by overridding the openPrim method of
         * our super class.
         */

        if (port < 0) {
            throw new IllegalArgumentException("Missing port number");
        }

        szHost = Util.toCString(host);
        open0(szHost, port);
        registerCleanup();
    }

    /**
     * Create a Java connection object from an open TCP socket.
     * This method is only used by com.sun.midp.io.j2me.serversocket.Socket;
     *
     * @param handle an already formed socket handle
     */
    public void open(int handle) {
        this.handle = handle;

        try {
            connectionOpen = true;
            checkForPermission(getAddress());
        } catch (Exception e) {
            connectionOpen = false;

            if (e instanceof IOException) {
                e = new SecurityException("Unknown TCP client");
            }

            try {
                close0();
            } catch (IOException ioe) {
                // ignore
            }

            throw (RuntimeException)e;
        }
        
        registerCleanup();
    }

    /**
     * Disconnect from the server.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void disconnect() throws IOException {
        /*
         * Only shutdown or close of the sending side of a connection is
         * defined in the TCP spec.
         *
         * The receiver can only abort (reset) the entire connection to stop
         * the a sender from sending. InputStream close already causes
         * an reads to fail so no native action is needed.
         *
         * Shutdown the output gracefully closes the sending side of the 
         * TCP connection by sending all pending data and the FIN flag.
         */

        if (!outputShutdown) {
            shutdownOutput0();
        }

        close0();
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, blocks until at least one byte is available.
     * Sets the <code>eof</code> field of the connection when the native read
     * returns -1.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    protected int nonBufferedRead(byte b[], int off, int len)
        throws IOException {

        int bytesRead;

        for (;;) {
            try {
                bytesRead = read0(b, off, len);
            } finally {
                if (iStreams == 0) {
                    throw new InterruptedIOException("Stream closed");
                }
            }

            if (bytesRead == -1) {
                eof = true;
                return -1;
            }

            if (bytesRead != 0) {
                return bytesRead;
            }

            /* Wait a while for I/O to become ready */
            GeneralBase.iowait(); 
        }
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.  The next caller might be the same thread or
     * another thread.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {
        if (count > 0) {
            /*
             * The next read will only return the bytes in the buffer,
             * so only return the number of bytes in the buffer.
             * While available can return a number less than than the next
             * read will get, it should not return more.
             */
            return count;
        }

        // The buffer is empty, so the next read will go directly to native
        return available0();
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * <p>
     * Polling the will be done by our super class.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @return     number of bytes written
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    public int writeBytes(byte b[], int off, int len) throws IOException {
        return write0(b, off, len);
    }

    /**
     * Called once by the child output stream. The output side of the socket
     * will be shutdown and then the parent method will be called.
     *
     * @exception IOException if the subclass throws one
     */
    protected void closeOutputStream() throws IOException {
        /*
         * Shutdown the output gracefully closes the sending side of the 
         * TCP connection by sending all pending data and the FIN flag.
         */
        shutdownOutput0();
        outputShutdown = true;
        super.closeOutputStream();
    }

    /**
     * Check a socket option to make sure it's a valid option.
     *
     * @param option socket option identifier (KEEPALIVE, LINGER, 
     * SNDBUF, RCVBUF, or DELAY)
     * @exception  IllegalArgumentException if  the value is not 
     *              valid (e.g. negative value)
     *              
     * @see #getSocketOption
     * @see #setSocketOption
     */
    private void checkOption(byte option) 
	throws IllegalArgumentException {
	if (option == SocketConnection.KEEPALIVE 
	    || option == SocketConnection.LINGER 
	    || option == SocketConnection.SNDBUF 
	    || option == SocketConnection.RCVBUF 
	    || option == SocketConnection.DELAY) {
	    return;
	}
	throw new IllegalArgumentException("Unsupported Socket Option");
	    
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
	checkOption(option);
	if (value < 0) {
	    throw new IllegalArgumentException("Unsupported Socket Option");
	}
	ensureOpen();

	setSockOpt0(option, value);
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
	throws IllegalArgumentException, IOException  {
	checkOption(option);
	ensureOpen();
	return getSockOpt0(option);
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
	return getHost0(true);
    }

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected.
     * @exception  IOException  if the connection was closed
     * @see ServerSocketConnection
     */
    public  int  getLocalPort()  throws IOException {
	ensureOpen();
	return getPort0(true); 
    }

    /**
     * Gets the remote address to which the socket is bound.
     * The address can be either the remote host name or the IP
     * address(if available).
     *
     * @return the remote address to which the socket is bound.
     * @exception  IOException  if the connection was closed
     */
    public  String getAddress() throws IOException {
	ensureOpen();
	return getHost0(false);
    }
    /**
     * Returns the remote port to which this socket is bound.
     *
     * @return the remote port number to which this socket is connected.
     * @exception  IOException  if the connection was closed
     */
    public  int  getPort() throws IOException {
	ensureOpen();
	return getPort0(false); 
    }

    /**
     * Connect to a server and fillin the handle field.
     *
     * @param szHost     host as a zero terminated ASCII string
     * @param port       TCP port at host
     *
     * @exception  IOException  if an I/O error occurs.
     */
    private native void open0(byte[] szHost, int port) throws IOException;

    /*
     * A note about read0() and write0()
     *
     * These routines will return the number of bytes transferred. It this
     * value is zero then it means that the data could not be read or written
     * and the calling code should call GeneralBase.iowait() to let some other
     * thread run.
     */

    /**
     * Read from the socket, accesses the handle field.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    private native int read0(byte b[], int off, int len)
        throws IOException;

    /**
     * Write to the socket, accesses the handle field.
     *
     * @param      b     the buffer of the data to write
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the number of bytes to write.
     * @return     the total number of bytes written
     * @exception  IOException  if an I/O error occurs.
     */
    private native int write0(byte b[], int off, int len)
        throws IOException;

    /**
     * Get the number of bytes that can be read without blocking,
     * accesses the handle field.
     *
     * @return     number of bytes that can be read without blocking
     * @exception  IOException  if an I/O error occurs.
     */
    private native int available0() throws IOException;

    /**
     * Close the connection, accesses the handle field.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    private native void close0() throws IOException;

    /**
     * Register with the native cleanup code, accesses the handle field.
     */
    private native void registerCleanup();

    /**
     * Native finalizer
     */
    private native void finalize();

    /**
     * Get the requested IP number.
     *
     * @param      local   <code>true</code for the local host, and
     *                     <code>false</code>for the remote host
     * @return     the IP address as a String
     */
    private native String  getHost0(boolean local);

    /**
     * Get the requested port number.
     *
     * @param      local   <code>true</code for the local host, and
     *                     <code>false</code>for the remote host
     * @return     the port number of the requested end point
     */
    private native int  getPort0(boolean local);
    
    /**
     * Get the requested socket option.
     *
     * @param      option  socket option to retrieve
     * @return     value of the socket option
     */
    private native int  getSockOpt0(int option);

    /**
     * Set the requested socket option.
     *
     * @param      option  socket option to set
     * @param      value of the socket option
     */
    private native void  setSockOpt0(int option, int value);

    /**
     * Shutdown the output side of the connection.
     */
    private native void shutdownOutput0();
}
