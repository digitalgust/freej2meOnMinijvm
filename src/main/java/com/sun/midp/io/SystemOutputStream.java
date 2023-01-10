/*
 * @(#)SystemOutputStream.java	1.8 02/07/24 @(#)
 *
 * Copyright (c) 1999-2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io;

import java.io.*;

/**
 *  Standard output stream for the System.out diagnostics messages.
 */
public class SystemOutputStream extends OutputStream {

    /**
     * Writes the specified byte to this output stream.
     *
     * @param      c   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs.
     */
    synchronized public void write(int c) throws IOException {
        putchar((char)c);
    }
    /**
     * native interface to output a single character to standard output.
     * @param c the char to output.
     */
    private static native void putchar(char c);

}
