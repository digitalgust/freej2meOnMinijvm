/*
 * @(#)ResourceInputStream.java	1.19 02/09/04 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io;

import java.io.IOException;
import java.io.InputStream;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Input stream class for accessing resource files in classpath.
 */
public class ResourceInputStream extends InputStream {
    /**
     * native handle to underlying stream
     */
    private int handle = -1;

    /**
     * The index of the next character to read from the input stream buffer.
     * This value should always be nonnegative
     * and not larger than the value of <code>size</code>.
     */
    protected int pos;

    /**
     * The index one greater than the last valid character in the input 
     * stream buffer. 
     * This value should always be nonnegative.
     */
    protected int size;

    /**
     * Fixes the resource name to be conformant with the CLDC 1.0 
     * specification. We are not allowed to use "../" to get outside
     * of the .jar file.
     *
     * @param name the name of the resource in classpath to access.
     * @return     the fixed string.
     * @exception  IOException if the resource name points to a
     *              classfile, as determined by the resource name's
     *              extension.
     */
    private String fixResourceName(String name) throws IOException {
	Vector dirVector = new Vector();
	int    startIdx = 0;
	int    endIdx = 0;
	String curDir;

	while ((endIdx = name.indexOf('/', startIdx)) != -1) {
	    if (endIdx == startIdx) {
		// We have a leading '/' or two consecutive '/'s
		startIdx++;
		continue;
	    }

	    curDir = name.substring(startIdx, endIdx);
	    startIdx = endIdx + 1;

	    if (curDir.equals(".")) {
		// Ignore a single '.' directory
		continue;
	    }
	    if (curDir.equals("..")) {
		// Go up a level
		int size = dirVector.size();

		if (size > 0) {
		    dirVector.removeElementAt(size - 1);
		} else {
		    // Do not allow "/../resource"
		    throw new IOException();
		}
		continue;
	    }

	    dirVector.addElement(curDir);
	}

	// save directory structure
	int nameLength = name.length();
	StringBuffer dirName = new StringBuffer(nameLength);
	int numElements = dirVector.size();

	for (int i = 0; i < numElements; i++) {
	    dirName.append((String)dirVector.elementAt(i));
	    dirName.append("/");
	}

	// save filename
	if (startIdx < nameLength) {
	    String filename = name.substring(startIdx);

	    // Throw IOE if the resource ends with ".class", but, not
            //  if the entire name is ".class"
	    if ((filename.endsWith(".class")) && 
		(! ".class".equals(filename))) {
		throw new IOException();
	    }
	    dirName.append(name.substring(startIdx));
	}

	return dirName.toString();
    }

    /**
     * Construct a resource input stream for accessing objects in the jar file.
     *
     * @param name the name of the resource in classpath to access. The
     *              name must not have a leading '/'.
     * @exception  IOException  if an I/O error occurs.
     */
    public ResourceInputStream(String name) throws IOException {
	if (handle != -1) { 
	    throw new IOException();
	}  

	String fixedName = fixResourceName(name);

	handle = open(fixedName.getBytes());  // open() sets 'size' data member
	pos = 0;
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @return     the next byte of data, or <code>-1</code> if the end
     *             of the stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read() throws IOException { 
        if (pos < size) {
            pos++;
        } else {
            return -1;
        }
        return read(handle);
    }

    /**
     * Gets the number of bytes remaining to be read.
     *
     * @return     the number of bytes remaining in the resource.
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {
	return size - pos;
    }

    /**
     * Reads bytes into a byte array.
     *
     * @param b the buffer to read into.
     * @param off offset to start at in the buffer.
     * @param len number of bytes to read.
     * @return     the number of bytes read, or <code>-1</code> if the end
     *             of the stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (pos >= size) {
            return -1;
        }
        if (pos + len > size) {
            len = size - pos;
        }
        if (len <= 0) {
            return 0;
        }
        int result = readBytes(handle, b, off, pos, len);
	if (result > -1) {
	    pos += result;
	}
	return result;
    }

    /**
     * Resets the buffer position to zero.
     * The value of <code>pos</code> is set to 0.
     */
    public synchronized void reset() {
        if (handle == -1) {
            return;
	}
        pos = 0;
    }

    /**
     * closes the open resource stream.
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException { 
	close(handle);
	handle = -1;
    }

    /**
     * native interface to open resource as stream.
     * @param name name of the resource in CLASSPATH
     * @return the Object reference to the located resource.
     * @exception IOException if I/O error occurs.
     */
    private native int open(byte name[]) throws IOException;

    /**
     * native interface to read a byte from the opened resource.
     * @param handle for the opened resource
     * @return a character read from the opened resource.
     * @exception IOException if I/O error occurs.
     */
    private native int read(int handle) throws IOException;

    /**
     * native interface to read  bytes from the opened resource.
     * @param handle for the opened resource
     * @param b array for the data
     * @param offset Offset in byte array to start reading into
     * @param pos position in resource to start reading
     * @param len number of bytes to read
     * @return number of characters read into the buffer
     * @exception IOException if I/O error occurs.
     */
    private native int readBytes(int handle, byte[] b, int offset, 
				 int pos, int len) throws IOException;

    /**
     * native interface to close the opened resource.
     * @param handle for the opened resource
     * @exception IOException if I/O error occurs.
     */
    private native void close(int handle) throws IOException;

    /**
     * native finalizaion
     */
    private native void finalize();
}
