/*
 * @(#)BufferedConnectionAdapter.java	1.6 02/09/11 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io;

import java.io.*;

/**
 * This class adds read buffering to the
 * <code>ConnectionBaseAdapter</code>.
 * <p>
 * Implements {@link InputStream#available()}, however a subclass must
 * specify a buffer size greater than 0 and override
 * {@link #readBytesNonBlocking(byte[], int, int)} in order to for available
 * to work properly.
 */
public abstract class BufferedConnectionAdapter extends ConnectionBaseAdapter {

    /**
     * The end of file flag.
     */
    protected boolean eof;

    /**
     * The internal buffer array where the data is stored.
     * When necessary, it may be replaced by another array
     * of a different size.
     */
    protected byte buf[];

    /**
     * The index one greater than the index of the last valid
     * byte in the buffer.
     * This value is always in the range
     * <code>0</code> through <code>buf.length</code>;
     * elements <code>buf[0]</code> through <code>buf[count-1]
     * </code>contain buffered input data obtained
     * from the underlying input stream.
     */
    protected int count;

    /**
     * The current position in the buffer. This is the index
     * of the next character to be read from the
     * <code>buf</code> array.
     * <p>
     * This value is always in the range <code>0</code>
     * through <code>count</code>. If it is less
     * than <code>count</code>, then <code>buf[pos]</code>
     * is the next byte to be supplied as input;
     * if it is equal to <code>count</code>, then
     * the  next <code>read</code> or <code>skip</code>
     * operation will require more bytes to be
     * read from the contained input stream.
     */
    protected int pos;

    /**
     * Initializes the connection.
     *
     * @param sizeOfBuffer size of the internal buffer or 0 for no buffer
     */
    protected BufferedConnectionAdapter(int sizeOfBuffer) {
        if (sizeOfBuffer > 0) {
            buf = new byte[sizeOfBuffer];
        }
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, blocks until at least one byte is available.
     * <p>
     * Do not use this method if <code>openInputStream</code> has been called
     * since the input stream may be buffering data.
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
    public int readBytes(byte b[], int off, int len) throws IOException {
        int bytesRead;

        if (count == 0) {
            if (eof) {
                return -1;
            }

            if (buf == null || len >= buf.length) {
                return nonBufferedRead(b, off, len);
            } else {
                int res = nonBufferedRead(buf, 0, buf.length);

                pos = 0;

                if (res <= 0) {
                    return res;
                } else {
                    count = res;
                }
            }
        }

        if (len > count) {
            len = count;
        }

        System.arraycopy(buf, pos, b, off, len);
        count -= len;
        pos   += len;
        return len;
    };

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, but does not block if no bytes available. A subclass
     * should implement this to so the available method on the InputStream
     * will be useful.
     * Sets the <code>eof</code> field of the connection when the native read
     * returns -1.
     * <p>
     * The <code>readBytesNonBlocking</code> method of
     * <code>ConnectionBaseAdapter</code> does nothing and returns 0.
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
    protected int readBytesNonBlocking(byte b[], int off, int len)
        throws IOException {
        return 0;
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.  The next caller might be the same thread or
     * another thread.
     *
     * <p>The <code>available</code> method always returns <code>0</code> if
     * {@link #readBytesNonBlocking(byte[], int, int)} is
     * not overridden by the subclass or there is not buffer.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {
        int bytesRead;

        if (buf == null) {
            return 0;
        }

        if (count > 0) {
            return count;
        }

        bytesRead = readBytesNonBlocking(buf, 0, buf.length);

        if (bytesRead == -1) {
            return 0;
        }
	
	/*
	 * Reset the current buffer position and count of bytes
	 * available. These variables must be reset to match
	 * the processing in readBytes.
	 */
	pos = 0;
        count = bytesRead;

        return count;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, blocks until at least one byte is available.
     * Sets the <code>eof</code> field of the connection when there is
     * no more data in the stream to read.
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
    protected abstract int nonBufferedRead(byte b[], int off, int len)
        throws IOException;
}
