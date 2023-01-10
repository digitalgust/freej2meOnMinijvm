/*
 * @(#)InputStorage.java	1.6 02/08/07 @(#)
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.publickeystore;

import java.io.*;

/**
 * Retieves stored fields from an InputStream.
 * Since Java Microedition has no serialization, this is a simple substitute.
 */
class InputStorage extends Storage {
    /** stream to read from */
    private DataInputStream in;

    /**
     * Constructs an InputStorage for an InputStream.
     * @param input the input storage input stream.
     * @exception IOException if the storage version cannot be read
     */
    InputStorage(InputStream input) throws IOException {
        in = new DataInputStream(input);

        // skip past the version number.
        in.readByte();
    }

    /**
     * Reads a field that was stored as tag, type, value set.
     * @param tag byte array of one byte to hold the tag of the field that
     *            was read
     * @return value of field that was stored, or null if there are no more
     *         fields
     * @exception IOException if the input storage was corrupted
     */
    Object readValue(byte[] tag) throws IOException {
        byte type;

        try {
            try {
                in.readFully(tag, 0, 1);
            } catch (EOFException eofe) {
                // this just means there are no more fields in storage
                return null;
            }

            type = in.readByte();
            if (type == BINARY_TYPE) {
                int len;
                byte[] value;

                /*
                 * must read the length first, because DataOutputStream does
                 * not handle handle byte arrays.
                 */
                len = in.readUnsignedShort();
                if (len < 0) {
                    throw new IOException();
                }

                value = new byte[len];
                in.readFully(value);
                return value;
            }

            if (type == STRING_TYPE) {
                return in.readUTF();
            }

            if (type == LONG_TYPE) {
                return new Long(in.readLong());
            }

            throw new IOException();
        } catch (IOException e) {
            throw new IOException("input storage corrupted");
        }
    }
}
