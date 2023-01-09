/*
 * @(#)DataConverter.java	1.7 02/07/24 @(#)
 *
 * Portiona Copyright (c) 2000-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Copyright 2000 Motorola, Inc. All Rights Reserved.
 * This notice does not imply publication.
 */

package javax.microedition.rms;

import java.lang.String;

/** 
 * A convenience class with useful functions for converting various
 * data types into and out of byte arrays for use in the database.
 * <p>
 * <b>(c) Copyright 2000 Motorola, Inc. All Rights Reserved.</b>
 * <p>
 * <b> Copyright (c) 2000-2001 Sun Microsystems, Inc. All Rights Reserved.</b>
 * This notice does not imply publication
 * <hr>
 *
 * @version 13-January-2000
 * @author Jim Van Peursem
 * @since MIDP 2.0
 */
class DataConverter
{
    /**
     * Nobody should ever need to instantiate this purely static class.
     */
    private DataConverter()
    {
    }

    /**
     * A convenience method for converting a byte array into
     * an int.  (Assumes big-endian byte ordering.)
     *
     * @param data the byte array returned from the record store.
     * @param offset the index into the data buffer of the first 
     *        relevant byte for this conversion.
     *
     * @return an int corresponding to the first four bytes 
     *         of the data buffer, starting at <code>offset</code>
     */
    public static int getInt(byte[] data, int offset)
    {
	// NYI.
	return 0;
    }

    /**
     * A convenience method for converting an integer into
     * a byte array with big-endian byte ordering.
     *
     * @param i the integer to turn into a byte array.
     * @param data the data buffer to write to.
     * @param offset the index into the data buffer of the 
     *        first byte to insert bytes at.
     *
     * @return the number of bytes written to the data buffer.
     */
    public static int putInt(int i, byte[] data, int offset)
    {
	// NYI.
	return 0;
    }

    /**
     * A convenience method for converting a byte array into
     * a long (assumes big-endian byte ordering).
     *
     * @param data the byte array returned from the record store.
     * @param offset the index into the data buffer of the first 
     *        relevant byte for this conversion.
     *
     * @return a long corresponding to the first eight bytes 
     *         of the data buffer, starting at <code>offset</code>.
     */
    public static long getLong(byte[] data, int offset)
    {
	// NYI.
	return 0;
    }

    /**
     * A convenience method for converting a long into
     * a byte array with big-endian byte ordering.
     *
     * @param l the long to turn into a byte array.
     * @param data the data buffer to write to.
     * @param offset the index into the data buffer of the
     *        first byte to insert bytes at.
     *
     * @return The number of bytes written to the data buffer.
     */
    public static int putLong(long l, byte[] data, int offset)
    {
	// NYI.
	return 0;
    }

    /**
     * A convenience method for converting a byte array into
     * a char (assumes big-endian byte ordering).  With a cast
     * this method can also convert a byte array into a short:
     *
     *   // Using getChar() to retrieve a short
     *   short s = (short) getChar(data, 0)
     *
     * @param data the byte array returned from the record store.
     * @param offset the index into the data buffer of the first 
     *        relevant byte for this conversion.
     *
     * @return a char corresponding to the first two bytes 
     *         of the data buffer, starting at <code>offset</code>.
     */
    public static char getChar(byte[] data, int offset)
    {
	// NYI.
	return '0';
    }

    /**
     * A convenience method for converting a char into
     * a byte array.  With a cast this method can convert a 
     * short into a byte array:
     *
     *   // Using putChar() to store a short.
     *   short s;
     *   int rv = putChar((char) s, data, 0); 
     *
     * @param c the char to turn into a byte array.
     * @param data the data buffer to write to.
     * @param offset the index into the data buffer of the 
     *        first byte to insert bytes at.
     *
     * @return The number of bytes written to the data buffer.
     */
    public static int putChar(char c, byte[] data, int offset)
    {
	// NYI.
	return 0;
    }

    /**
     * A convenience method for converting a byte array into
     * a String.
     *
     * @param data the byte array returned from the record store.
     * @param offset the index into the data buffer of the first
     *  relevant byte for this conversion.
     * @param numBytes the number of bytes of the data buffer to 
     *        use for this conversion.
     *
     * @return a String corresponding to the bytes of the data
     *         buffer passed in, starting at <code>offset</code>.
     */
    public static String getString(byte[] data, int offset, int numBytes)
    {
	// NYI.
	return null;
    }

    /**
     * A convenience method for converting a String into
     * a byte array.
     *
     * @param s the String to turn into a byte array.
     * @param data the data buffer to write to.
     * @param offset the index into the data buffer of the 
     *        first byte to insert bytes at.
     *
     * @return The number of bytes written to the data buffer.
     */
    public static int putString(String s, byte[] data, int offset)
    {
	// NYI.
	return 0;
    }
}
