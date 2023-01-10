/*
 * @(#)TextPolicy.java	1.13 02/09/17 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextField;

/**
 * Class to handle conforming text to specified constraints
 */
public class TextPolicy {

    /**
     * Check is this is a valid decimal
     *
     * @param array string to check
     * @return true if this is a valid string
     */
    private static boolean checkDecimal(char[] array) {

        int len = array.length;

        /*
         * If the whole content is "-", ".", or "-.",
         * this is invalid.
         */
        if ((len == 1 && array[0] == '-') ||
            (len == 1 && array[0] == '.') ||
            (len == 2 && array[0] == '-' && array[1] == '.')) {
            return false;
        }

        /*
         * For decimal constraint, it is probably easier to re-validate the
         * whole content, than to try to validate the inserted data in
         * relation to the existing data around it.
         */
        boolean hasSeparator = false;
        for (int i = 0; i < len; i++) {
            char    c = array[i];

            /*
             * valid characters are
             *   [0-9],
             *   '-' at the first pos,
             *   '.' as the decimal separator.
             */
            if (c == '.') {
                if (!hasSeparator) {
                    hasSeparator = true;
                } else {
                    return false;
                }
            } else if (((c < '0') || (c > '9')) &&
                       (c != '-'  || i != 0)) {
                return false;
            }
        }
        return true;

    }

    /**
     * Check is this is a valid email
     *
     * @param array string to check
     * @return true if this is a valid string
     */
    private static boolean checkEmail(char[] array) {
        return true;
    }

    /**
     * Check is this is a valid numeric
     *
     * @param array string to check
     * @return true if this is a valid string
     */
    private static boolean checkNumeric(char[] array) {

        int len = array.length;

        /* If the whole content is just a minus sign, this is invalid. */
        if (len == 1 && array[0] == '-') {
            return false;
        }

        int offset = 0;

        //
        // if first character is a minus sign then don't let the loop
        // below see it.
        //
        if (array[0] == '-') {
            offset++;
        }

        /*
         * Now we can just validate the inserted data. If we see a minus
         * sign then it must be in the wrong place because of the check
         * above
         */
        for (; offset < len; offset++) {
            char c = array[offset];
            if (((c < '0') || (c > '9'))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check is this is a valid phone number
     *
     * @param array string to check
     * @return true if this is a valid string
     */
    private static boolean checkPhoneNumber(char[] array) {
        int len = array.length;
        for (int i = 0; i < len; i++) {
            char c = array[i];
            if (((c < '0') || (c > '9')) &&
                (!(c == '#' || c == '*' || c == '+'))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check is this is a valid url
     *
     * @param array string to check
     * @return true if this is a valid string
     */
    private static boolean checkURL(char[] array) {
        return true;
    }

    /**
     * Check is this is a valid string given the constraints
     *
     * @param dca string to check
     * @param constraints the constraints 
     * @return true if this is a valid string
     */
    public static boolean isValidString(DynamicCharacterArray dca, 
                                        int constraints) { 

        if (dca.length() == 0) {
            return true;
        }

        char[] array = dca.toCharArray();

        switch (constraints & TextField.CONSTRAINT_MASK) {
            case TextField.ANY:         return true;
            case TextField.DECIMAL:     return checkDecimal(array);
            case TextField.EMAILADDR:   return checkEmail(array);
            case TextField.NUMERIC:     return checkNumeric(array);
            case TextField.PHONENUMBER: return checkPhoneNumber(array);
            case TextField.URL:         return checkURL(array);
        }
        return false;
    } 
  
    /**
     * Returns the constraints size of the text
     *
     * @param size preferred size
     * @param buf text buffer
     * @return constrained size
     */
    public static int constrainedSize(int size, char buf[]) {
        return size;
    }

    /**
     * Returns the string that would be painted. This may not be the
     * same as the string that is actually displayed because of the
     * options argument that affects painting.
     *
     * @param dca the text to paint
     * @param opChar if opChar > 0 then an optional character to paint.
     * @param constraints text constraints
     * @param inputHandler inputHandler to query for symbols
     * @param cursor text cursor object to use to draw vertical bar
     * @return the string that will be sent to be drawn. this may not be what
     *		is actually drawn depending on the options.
     */
    public static String getDisplayString(DynamicCharacterArray dca, 
                                          char opChar, 
                                          int constraints,
                                          InputMethodHandler inputHandler,
                                          TextCursor cursor) {
        return getDisplayString(dca, opChar, constraints, inputHandler,
                                cursor, false);
    }

    /**
     * Returns the string that would be painted. This may not be the
     * same as the string that is actually displayed because of the
     * options argument that affects painting.
     *
     * @param dca the text to paint
     * @param opChar if opChar > 0 then an optional character to paint.
     * @param constraints text constraints
     * @param inputHandler inputHandler to query for symbols
     * @param cursor text cursor object to use to draw vertical bar
     * @param modifyCursor true if this method can modify the cursor
     *        object if necessary, false otherwise
     * @return the string that will be sent to be drawn. this may not be what
     *		is actually drawn depending on the options.
     */
    public static String getDisplayString(DynamicCharacterArray dca, 
                                          char opChar, 
                                          int constraints,
                                          InputMethodHandler inputHandler,
                                          TextCursor cursor,
                                          boolean modifyCursor) {

        int len = dca.length();
        DynamicCharacterArray out = null;

        if ((constraints & TextField.PASSWORD) == TextField.PASSWORD) {

            out = new DynamicCharacterArray(len + 1);

            // char newBuf[] = new char[len];

            //
            // Handle password: if the constraints are ANY or NUMERIC
            // we can just set all the characters to a *. otherwise,
            // we must only change those characters which are not
            // considered to be symbols
            //

            if ((constraints & TextField.CONSTRAINT_MASK) ==
                   TextField.ANY ||
                 (constraints & TextField.CONSTRAINT_MASK) ==
                   TextField.NUMERIC) {


                for (int i = 0; i < len; i++) {
                    // newBuf[i] = '*';
                    out.append('*');
                }

            } else {
                for (int i = 0; i < len; i++) {
                    
                    if (!inputHandler.isSymbol(dca.charAt(i))) {
                        out.append('*');
                        // newBuf[i] = '*';
                    } else {
                        out.append(dca.charAt(i));
                        // newBuf[i] = buf[i + off];
                    }
                }
            }
            // return new String(newBuf);
        } 
        else {

            // +3 is the most characters we will need to insert here
            out = new DynamicCharacterArray(len + 1 + 3);
            out.insert(dca.toCharArray(), 0, len, 0);

            switch (constraints & TextField.CONSTRAINT_MASK) {
                case TextField.PHONENUMBER:
 
                    if (!modifyCursor) {
                        cursor = new TextCursor(cursor);
                    }

                    switch (len) {
                        case 5:
                        case 6:
                        case 7:
                            out.insert(3, ' ');
                            if (cursor.index > 3) cursor.index++;
                            break;
                        case 11:
                            out.insert(1, ' ');
                            if (cursor.index > 1) cursor.index++;
                            out.insert(5, ' ');
                            if (cursor.index > 5) cursor.index++;
                            out.insert(9, ' ');
                            if (cursor.index > 9) cursor.index++;
                            break;
                        case 8:
                        case 9:
                        case 10:
                        default:
                            out.insert(3, ' ');
                            if (cursor.index > 3) cursor.index++;
                            out.insert(7, ' ');
                            if (cursor.index > 7) cursor.index++;
                            break;
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            break;
                    }

                case TextField.EMAILADDR:
                case TextField.URL:
                case TextField.ANY:
                case TextField.DECIMAL:
                case TextField.NUMERIC:
            }
        }

        if (opChar > 0) {
            if (out == null) {
                out = new DynamicCharacterArray(dca.length() + 1);
            }

            if (cursor != null) {
                out.insert(cursor.index, opChar);
            } else {
                out.append(opChar);
            }
        }

        if (out == null) {
            out = dca;
        }

        return out.toString();
    }

    /**
     * Paint the text, linewrapping when necessary
     *
     * @param dca the text to paint
     * @param opChar if opChar > 0 then an optional character to paint. 
     * @param constraints text constraints
     * @param inputHandler inputHandler to query for symbols
     * @param font the font to use to paint the text
     * @param g the Graphics to use to paint with. If g is null then
     *        only the first four arguments are used and nothing is
     *        painted. Use this to return just the displayed string
     * @param w the available width for the text
     * @param h the available height for the text
     * @param offset the first line pixel offset
     * @param options any of Text.[NORMAL | INVERT | HYPERLINK | TRUNCATE]
     * @param cursor text cursor object to use to draw vertical bar
     */
    public static void paint(DynamicCharacterArray dca, char opChar, 
                             int constraints,
                             InputMethodHandler inputHandler, Font font, 
                             Graphics g, int w, int h, int offset, 
                             int options, TextCursor cursor) {

        int cursorIndex = cursor.index;

        if ((constraints & TextField.CONSTRAINT_MASK)
            == TextField.PHONENUMBER) {
            cursorIndex = cursor.index;
            cursor.option = Text.PAINT_USE_CURSOR_INDEX;
        }

        //
        // it's important to call this before paint so that we use the
        // right cursor
        //
        String str = getDisplayString(dca, opChar, constraints, 
                                      inputHandler, cursor, true); 

        Text.paint(str, font, g, w, h, offset, options, cursor);

        if ((constraints & TextField.CONSTRAINT_MASK)
            == TextField.PHONENUMBER) {
            cursor.index = cursorIndex;
        }
    }
}

