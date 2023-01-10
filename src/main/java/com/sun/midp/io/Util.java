/*
 * @(#)Util.java	1.7 02/07/24 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io;

import java.util.Vector;

/** Contains static utility methods for IO protocol classes to use. */
public abstract class Util {
    /**
     * Converts <code>string</code> into a null terminated
     * byte array.  Expects the characters in <code>string
     * </code> to be in th ASCII range (0-127 base 10).
     *
     * @param string the string to convert
     *
     * @return byte array with contents of <code>string</code>
     */
    public static byte[] toCString(String string) {
        int length = string.length();
        byte[] cString = new byte[length + 1];

        for (int i = 0; i < length; i++) {
            cString[i] = (byte)string.charAt(i);
        }

        return cString;
    }

    /**
     * Converts an ASCII null terminated byte array in to a
     * <code>String</code>.  Expects the characters in byte array
     * to be in th Ascii range (0-127 base 10).
     *
     * @param cString the byte array to convert
     *
     * @return string with contents of the byte array
     * @exception ArrayIndexOutOfBounds if the C string does not end with 0
     */
    public static String toJavaString(byte[] cString) {
        int i;
        String jString;

        // find the string length
        for (i = 0; cString[i] != 0; i++);

        try {
            return new String(cString, 0, i, "ISO8859_1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Create a vector of values from a string containing comma separated
     * values. The values cannot contain a comma. The output values will be
     * trimmed of whitespace. The vector may contain zero length strings
     * where there are 2 commas in a row or a comma at the end of the input
     * string.
     *
     * @param input input string of comma separated values
     *
     * @return vector of string values.
     */
    public static Vector getCommaSeparatedValues(String input) {
        Vector output = new Vector(5, 5);
        int len;
        int start;
        int end;
        
        len = input.length();
        if (len == 0) {
            return output;
        }

        for (start = 0; ; ) {
            end = input.indexOf(',', start);
            if (end == -1) {
                break;
            }

            output.addElement(input.substring(start, end).trim());
            start = end + 1;
        }

        end = len;
        output.addElement(input.substring(start, end).trim());

        return output;
    }
}
