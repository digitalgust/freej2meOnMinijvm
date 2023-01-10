/*
 * @(#)DatagramObject.java	1.29 02/09/11 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io.j2me.datagram;

import java.io.*;

import javax.microedition.io.*;

import com.sun.cldc.io.GeneralBase;

import com.sun.midp.io.HttpUrl;
import com.sun.midp.io.Util;

/**
 * Implements a UDP datagram for the UDP datagram connection.
 */
public class DatagramObject extends GeneralBase implements Datagram {

    /** Length of the hostname buffer. */
    private static final int MAX_HOST_LENGTH = 256;

    /** Buffer to be used. */
    private byte[] buffer;

    /** Where to start sending or receiving data. */
    private int offset;

    /** Number of bytes to send or receive. */
    private int length;

    /** Datagram address as a URL. */
    String address;

    /** Raw IPv4 address. */
    int ipNumber;

    /** UDP port */
    int port;

    /** Current read/write position in buffer. */
    private int readWritePosition;

    /**
     * Create a Datagram Object.
     *
     * @param  buf             The buffer to be used in the datagram
     * @param  len             The length of the buffer to be allocated
     *                         for the datagram
     */
    public DatagramObject(byte[] buf, int len) {
        setData(buf, 0, len);
    }

    /**
     * Get the address in the datagram.
     *
     * @return the address in string form, or null if no address was set
     *
     * @see #setAddress
     */
    public String getAddress() {
        return address;
    }

    /**
     * Get the buffer.
     *
     * @return the data buffer
     *
     * @see #setData
     */
    public byte[] getData() {
        return buffer;
    }

    /**
     * Get the a number that is either the number of bytes to send or the
     * number of bytes received.
     *
     * @return the length of the data
     *
     * @see #setLength
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the offset.
     *
     * @return the offset into the data buffer
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Set datagram address. Must a "datagram://" URI.
     * <p>
     * @param addr the new target address as a URL
     * @exception IllegalArgumentException if the address is not valid
     *
     * @see #getAddress
     */
    public void setAddress(String addr) {
        HttpUrl url;
        int temp;

	if (addr == null)  {
            throw new IllegalArgumentException("Invalid address");
        }

        url = new HttpUrl(addr);

        if (url.scheme == null || !url.scheme.equals("datagram")) {
            throw new IllegalArgumentException("Invalid scheme");
        }

        /*
         * Since we reused the HttpUrl parser, we must make sure that
         * there was nothing past the authority in the URL.
         */
        if (url.path != null || url.query != null || url.fragment != null) {
            throw new IllegalArgumentException("Malformed address");
        }

        port = url.port;

        if (url.host == null) {
            throw new IllegalArgumentException("Missing host");
        }
            
        if (port == -1) {
            throw new IllegalArgumentException("Missing port");
        }

        temp = Protocol.getIpNumber(Util.toCString(url.host));
        if (temp == -1) {
            throw new IllegalArgumentException("Invalid host");
        }

        ipNumber = temp;
        address = addr;
    }

    /**
     * Set datagram address, copying the address from another datagram.
     *
     * @param reference the datagram who's address will be copied as
     * the new target address for this datagram.
     * @exception IllegalArgumentException if the address is not valid
     *
     * @see #getAddress
     */
    public void setAddress(Datagram reference) {
        setAddress(reference.getAddress());
    }

    /**
     * Set the length. Which can represent either the number of bytes to send
     * or the maxium number of bytes to receive.
     *
     * @param len the new length of the data
     * @exception IllegalArgumentException if the length is negative
     * or larger than the buffer
     *
     * @see #getLength
     */
    public void setLength(int len) {
        setData(buffer, offset, len);
    }

    /**
     * Set the buffer, offset and length.
     *
     * @param buf the data buffer
     * @param off the offset into the data buffer
     * @param len the length of the data in the buffer
     * @exception IllegalArgumentException if the length or offset
     *                                     fall outside the buffer
     *
     * @see #getData
     */
    public void setData(byte[] buf, int off, int len) {
	/*
	 * Check that the offset and length are valid.
	 *   - must be positive
	 *   - must not exceed buffer length
	 *   - must have valid buffer
	 */
        if (len < 0 || off < 0 ||
	    (buf == null) ||
	    (off > 0 && off == buf.length) ||
            ((len + off) > buf.length)||
            ((len + off) < 0)) {
            throw new IllegalArgumentException("Illegal length or offset");
        }

        buffer = buf;
        offset = off;
        length = len;
    }

    /**
     * Zero the read/write pointer as well as the offset and
     * length parameters.
     */
    public void reset() {
        readWritePosition = 0;
        offset = 0;
        length = 0;
    }

    /**
     * A more effient <code>skip</code> than the one in
     * <code>GeneralBase</code>.
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream. The <code>skip</code> method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly <code>0</code>.
     * This may result from any of a number of conditions; reaching end of file
     * before <code>n</code> bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned.  If <code>n</code> is
     * negative, no bytes are skipped.
     *
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
        if (n < 0) {
            return 0;
        }

        if (readWritePosition >= length) {
            return 0;
        }

        int min = Math.min((int)n, length - readWritePosition);
        readWritePosition += min;
        return (min);
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     */
    public int read() {
        if (readWritePosition >= length) {
            return -1;
        }

        return buffer[offset + readWritePosition++] & 0xFF;
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>ch</code> are ignored.
     *
     * @param      ch   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             buffer is full.
     */
    public void write(int ch) throws IOException {
        if (offset + readWritePosition >= buffer.length) {
            throw new IOException("Buffer full");
        }

        buffer[offset + readWritePosition++] = (byte)ch;
        length = readWritePosition;
    }
}


