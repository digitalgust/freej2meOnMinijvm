/*
 * @(#)Text.java	1.30 02/10/09 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Graphics;

/**
 * Static method class use to draw and size text
 */

public class Text {

    static {
        //
        // this questionable piece of code is used to force the
        // loading of the HYPERLINK_IMG. we need a way
        // to load icons from this package.
        //
        ImageItem ii = new ImageItem(null, null, 
                                     ImageItem.LAYOUT_DEFAULT, null);
        ii = null;
    }

    /** String to represent an ellipsis (...) */
    private static final char[] ellipsis = {'.', '.', '.'};
 
    // the following are used in calling the getNextLine method

    /** line start */
    private static final int GNL_LINE_START = 0;
    /** line end */
    private static final int GNL_LINE_END = 1;
    /** new line start */
    private static final int GNL_NEW_LINE_START = 2;
    /** screen width available */
    private static final int GNL_WIDTH = 3;
    /** screen height available */
    private static final int GNL_HEIGHT = 4;
    /** font height */
    private static final int GNL_FONT_HEIGHT = 5;
    /** line number */
    private static final int GNL_NUM_LINES = 6;
    /** text options (NORMAL, INVERT...) see below. */
    private static final int GNL_OPTIONS = 7;
    /** text pixel offset */
    private static final int GNL_OFFSET = 8;
    /** width of the ellipsis in the current font */
    private static final int GNL_ELLIP_WIDTH = 9;
    /** line width in pixels */
    private static final int GNL_LINE_WIDTH = 10;
    
    /** number of GNL_ parameter constants */
    private static final int GNL_NUM_PARAMS = 11;
    

    // Display will set these colors when it initializes

    /** The default foreground color */
    public static int FG_COLOR;

    /** The default foreground hilight color */
    public static int FG_H_COLOR;

    // The hyperlink Image to display with the Hyperlink
    // Image will set this img when it initializes

    /** The image to draw for hyperlinks */
    public static Image HYPERLINK_IMG;


    // constants to affect how text drawing is handled
    // These values can be OR'd together. no error checking is performed

    /** NORMAL text */
    public static final int NORMAL    = 0x0;
    /** INVERTED text color */
    public static final int INVERT    = 0x1;
    /** Draw a hyperlink for the text */
    public static final int HYPERLINK = 0x2;
    /** truncate the text and put a "..." if the text doesn't fit the bounds */
    public static final int TRUNCATE  = 0x4;



    // these values are stored used as setting in a TextCursor object
 
    /** 
     * when a paint occurs use the cursor index to know when to
     * paint the cursor
     */
    public static final int PAINT_USE_CURSOR_INDEX = 0;
 
    /** 
     * when a paint occurs try to find the best value for the cursor
     * index based on the x,y coordinates of the cursor
     */
    public static final int PAINT_GET_CURSOR_INDEX = 1;

    /** don't draw a cursor */
    public static final int PAINT_HIDE_CURSOR = 2;


    /**
     * Get the height in pixels to render the given string
     *
     * @param str the string to render
     * @param font the font to use to render the string
     * @param w the available width for the string
     * @param offset the pixel offset for the first line
     * @return the height in pixels required to render this string completely
     */
    public static int getHeightForWidth(String str, Font font, 
                                        int w, int offset) {
        // Case 0: null or empty string, no height
        if (str == null || str.length() == 0 || w <= 0) {
            return 0;
        }

        // Case 1: text requires line wrapping
        return (font.getHeight() *
                linesOfText(str.toCharArray(), offset, w, font));
    }

    /**
     * Paint the text, linewrapping when necessary
     *
     * @param str the text to paint
     * @param font the font to use to paint the text
     * @param g the Graphics to use to paint with
     * @param w the available width for the text
     * @param h the available height for the text
     * @param offset the first line pixel offset
     * @param options any of NORMAL | INVERT | HYPERLINK | TRUNCATE
     * @param cursor text cursor object to use to draw vertical bar
     * @return the width of the last line painted
     */
    public static int paint(String str, Font font, Graphics g,
                      int w, int h, int offset, int options,
                      TextCursor cursor) {

        if (w <= 0 || 
            (cursor == null && (str == null || str.length() == 0))) {
            return 0;
        }

        if (str == null) {
            str = "";
        }

        Font oldFont = g.getFont();
        if (oldFont != font) {
            g.setFont(font);
        }

        char[] text = str.toCharArray();
        int fontHeight = font.getHeight();

        if (cursor != null && cursor.visible == false) {
            cursor = null;
        }

        int[] inout               = new int[GNL_NUM_PARAMS];
        inout[GNL_FONT_HEIGHT]    = fontHeight;
        inout[GNL_WIDTH]          = w;
        inout[GNL_HEIGHT]         = h;
        inout[GNL_OPTIONS]        = options;
        inout[GNL_ELLIP_WIDTH]    = font.charsWidth(ellipsis, 0, 3);

        inout[GNL_LINE_START]     = 0;
        inout[GNL_LINE_END]       = 0;
        inout[GNL_NEW_LINE_START] = 0;
        inout[GNL_OFFSET]         = offset;

        int numLines = 0;
        int height   = 0;

        do {

            numLines++;
            height += fontHeight;

            if (height > h) {
                break;
            }

            inout[GNL_NUM_LINES] = numLines;

            boolean truncate = getNextLine(text, font, inout);

            int lineStart    = inout[GNL_LINE_START];
            int lineEnd      = inout[GNL_LINE_END];
            int newLineStart = inout[GNL_NEW_LINE_START];

            //
            // now we can get around to actually draw the text
            // lineStart is the array index of the first character to
            // start drawing, while lineEnd is the index just after
            // the last character to draw.
            //
            if (lineEnd > lineStart) {

                if ((options & INVERT) == INVERT) {
                    g.fillRect(offset, height - fontHeight,
                               inout[GNL_LINE_WIDTH], fontHeight);
                    g.setColor(FG_H_COLOR);
                }
                if ((options & HYPERLINK) == HYPERLINK) {
                    drawHyperLink(g, offset, height, inout[GNL_LINE_WIDTH]);
                }

                //
                // we are given x,y coordinates and we must calculate
                // the best array index to put the cursor
                //
                if (cursor != null && 
                    cursor.option == PAINT_GET_CURSOR_INDEX && 
                    cursor.x >= 0 && 
                    cursor.y == height) {
 
                    int bestIndex = lineStart;
                    int bestX = offset;
                    int curX = offset;
                    int curY = height;

                    //
                    // draw one character at a time and check its position
                    // against the supplied coordinates in cursor
                    //
                    for (int i = lineStart; i < lineEnd; i++) {

                        char ch = text[i];

                        g.drawChar(ch, curX, curY, 
                                    Graphics.BOTTOM | Graphics.LEFT);


                        if (Math.abs(curX - cursor.preferredX) <
                            Math.abs(bestX - cursor.preferredX)) {
                            bestIndex = i;
                            bestX = curX;
                        }

                        curX += font.charWidth(ch);
                    }

                    if (Math.abs(curX - cursor.preferredX) <
                        Math.abs(bestX - cursor.preferredX)) {
                        bestIndex = lineEnd;
                        bestX = curX;
                    }

                    cursor.index = bestIndex;
                    cursor.x = bestX;
                    cursor.y = height;
                    cursor.option = PAINT_USE_CURSOR_INDEX;

                } else {

                    g.drawChars(text, lineStart, lineEnd - lineStart,
                                offset, height,
                                Graphics.BOTTOM | Graphics.LEFT);
                }

                //
                // draw the ellipsis
                //
                if (truncate) {
 
                    int curX = inout[GNL_LINE_WIDTH];

                    g.drawChars(ellipsis, 0, 3,
                                curX + offset, height,
                                Graphics.BOTTOM | Graphics.LEFT);
                }

            }

            //
            // try to draw a vertical cursor indicator
            //
            if (cursor != null &&
                cursor.option == PAINT_USE_CURSOR_INDEX && 
                cursor.index >= lineStart && cursor.index <= lineEnd) {
    
                int off = offset;
                if (cursor.index > lineStart) {
                    off += font.charsWidth(text, lineStart, 
                                            cursor.index - lineStart);
                }
    
                cursor.x      = off;
                cursor.y      = height;
                cursor.width  = 1;
                cursor.height = fontHeight;

                cursor.paint(g);
                cursor = null;
            }
    
            if ((options & INVERT) == INVERT) {
                g.setColor(FG_COLOR);
            }

            inout[GNL_LINE_START] = newLineStart;
            inout[GNL_OFFSET] = 0;
            offset = 0;

        } while (inout[GNL_LINE_END] < text.length);

        g.setFont(oldFont);

        return inout[GNL_LINE_WIDTH];
    }

    /**
     * Draw a hyperlink image
     *
     * @param g the graphics to use to draw the image
     * @param x the x location of the image
     * @param y the y location of the image
     * @param w the width of the hyperlink image
     */
    public static void drawHyperLink(Graphics g, int x, int y, int w) {

        int linkHeight = HYPERLINK_IMG.getHeight();
        int linkWidth = HYPERLINK_IMG.getWidth();

        int oldClipX = g.getClipX();
        int oldClipW = g.getClipWidth();
        int oldClipY = g.getClipY();
        int oldClipH = g.getClipHeight();

        g.clipRect(x, oldClipY, w, oldClipH);

        // Then, loop from the end of the string to the beginning,
        // drawing the image as we go
        for (int j = x + w - linkWidth, first = x - linkWidth; 
             j > first; j -= linkWidth) {
            g.drawImage(HYPERLINK_IMG, j, y,
                        Graphics.BOTTOM | Graphics.LEFT);
        }

        g.setClip(oldClipX, oldClipY, oldClipW, oldClipH);
    }

    /**
     * Utility method to return the number of lines it would take
     * to render the given text with the given first-line offset,
     * available width, and Font
     *
     * @param text the text to render
     * @param offset a pixel offset for the first line
     * @param width the available width for the text
     * @param font the font to render the text in
     * @return the number of lines required to fit the text
     */
    public static int linesOfText(char[] text, int offset,
                                  int width, Font font) {

        int numLines = 0;

        if (text == null || text.length == 0) {
            return numLines;
        }

        int[] inout               = new int[GNL_NUM_PARAMS];
        inout[GNL_FONT_HEIGHT]    = font.getHeight();
        inout[GNL_WIDTH]          = width;
        inout[GNL_OPTIONS]        = Text.NORMAL;
        inout[GNL_ELLIP_WIDTH]    = font.charsWidth(ellipsis, 0, 3);
        inout[GNL_LINE_START]     = 0;
        inout[GNL_LINE_END]       = 0;
        inout[GNL_NEW_LINE_START] = 0;
        inout[GNL_OFFSET]         = offset;
        inout[GNL_LINE_WIDTH]     = 0;

        do {

            numLines++;

            inout[GNL_NUM_LINES]      = numLines;
            getNextLine(text, font, inout);

            inout[GNL_LINE_START] = inout[GNL_NEW_LINE_START];
            inout[GNL_OFFSET] = 0;

        } while (inout[GNL_LINE_END] < text.length);

        return numLines;
    }


    /**
     * Utility method to retrieve the length of the longest line of the 
     * text given the width. this may not necessarily be the entire 
     * string if there are line breaks or word wraps.
     *
     * @param text the text to use.
     * @param offset a pixel offset for the first line
     * @param width the available width for the text
     * @param font the font to render the text in
     * @return the length of the longest line given the width
     */
    public static int getWidestLineWidth(char[] text, int offset,
                                         int width, Font font) {

        int numLines = 0;

        if (text == null || text.length == 0) {
            return 0;
        }

        int[] inout               = new int[GNL_NUM_PARAMS];
        inout[GNL_FONT_HEIGHT]    = font.getHeight();
        inout[GNL_WIDTH]          = width;
        inout[GNL_OPTIONS]        = Text.NORMAL;
        inout[GNL_ELLIP_WIDTH]    = font.charsWidth(ellipsis, 0, 3);
        inout[GNL_LINE_START]     = 0;
        inout[GNL_LINE_END]       = 0;
        inout[GNL_NEW_LINE_START] = 0;
        inout[GNL_OFFSET]         = offset;
        inout[GNL_LINE_WIDTH]     = 0;

        int widest = 0;

        do {

            numLines++;

            inout[GNL_NUM_LINES] = numLines;

            getNextLine(text, font, inout);

            if (inout[GNL_LINE_WIDTH] > width && offset == 0) {
                return width;
            }

            if (inout[GNL_LINE_WIDTH] > widest) {
                widest = inout[GNL_LINE_WIDTH];
            }

            inout[GNL_LINE_START] = inout[GNL_NEW_LINE_START];
            inout[GNL_OFFSET] = 0;

        } while (inout[GNL_LINE_END] < text.length);

        return widest;
    }

    /**
     * Calculate the starting and ending points for a new line of
     * text given the font and input parameters. Beware of the
     * multiple returns statements within the body.
     *
     * @param text text to process. this must not be null
     * @param font font to use for width information
     * @param inout an array of input parameters corresponing to the
     *              GNL_ constants
     * @return true if the text had to be truncated, false otherwise
     */
    private static boolean getNextLine(char[] text, Font font, int[] inout) {

        //
        // this inner loop will set lineEnd and newLineStart to 
        // the proper values so that a line is broken correctly
        //
        int curLoc         = inout[GNL_LINE_START];
        boolean foundBreak = false;
        int leftWidth      = 0;

        inout[GNL_LINE_WIDTH] = 0;

        while (curLoc < text.length) {

            //
            // a newLine forces a break and immediately terminates
            // the loop
            //
            // a space will be remembered as a possible place to break
            //
            if (text[curLoc] == '\n') {
                inout[GNL_LINE_END] = curLoc;
                inout[GNL_NEW_LINE_START] = curLoc + 1;

                break;

            } else if (text[curLoc] == ' ') {
                inout[GNL_LINE_END] = curLoc;
                inout[GNL_NEW_LINE_START] = curLoc + 1;
                foundBreak = true;
            }

            //
            // if the text is longer than one line then we
            // cut the word at a word boundary if possible, 
            // otherwise the word is broken. 
            //

            inout[GNL_LINE_WIDTH] += font.charWidth(text[curLoc]);

            if (((inout[GNL_OPTIONS] & TRUNCATE) == TRUNCATE)
                && ((inout[GNL_NUM_LINES] + 1) * inout[GNL_FONT_HEIGHT] 
                    > inout[GNL_HEIGHT])
                && (inout[GNL_LINE_WIDTH] + inout[GNL_OFFSET] + 
                    inout[GNL_ELLIP_WIDTH] > inout[GNL_WIDTH])) {

                leftWidth =  font.charsWidth(text, curLoc + 1, 
                                             text.length - curLoc - 1);
                //
                // we are on the last line and at the point where
                // we will need to put an ellipsis if we can't fit
                // the rest of the line
                //
                // if the rest of the line will fit, then don't
                // put an ellipsis
                //
                if (inout[GNL_OFFSET] + inout[GNL_LINE_WIDTH] + leftWidth 
                    > inout[GNL_WIDTH]) {
                    
                    inout[GNL_LINE_WIDTH] += inout[GNL_ELLIP_WIDTH];
                    
                    /*
                    if (!foundBreak) {
                        inout[GNL_LINE_END] = curLoc;
                        inout[GNL_NEW_LINE_START] = curLoc;
                    }
                    */
                    inout[GNL_LINE_END] = curLoc;
                    inout[GNL_NEW_LINE_START] = curLoc;

                    return true;

                } else {

                    inout[GNL_LINE_WIDTH] += leftWidth;

                    inout[GNL_LINE_END] = text.length;
                    inout[GNL_NEW_LINE_START] = text.length;

                    return false;
                }

            } else if (inout[GNL_OFFSET] + inout[GNL_LINE_WIDTH] 
                       > inout[GNL_WIDTH]) {
              
                if (!foundBreak) {
                    if (inout[GNL_OFFSET] > 0) {
                        // move to the next line which will have 0 offset
                        inout[GNL_LINE_END] = inout[GNL_LINE_START];
                        inout[GNL_NEW_LINE_START] = inout[GNL_LINE_START];
                    } else {
                        // the line is too long and we need to break it
                        inout[GNL_LINE_END] = curLoc;
                        inout[GNL_NEW_LINE_START] = curLoc;
                    }
                }
                return false; 
            }

            curLoc++;
        }

        inout[GNL_LINE_END] = curLoc;

        return false;
    }

    /**
     * Utility method to calculate the width in which 2 strings can fit 
     * given the strings, fonts and maximum width in which those strings 
     * should fit. Returned value is either the passed in width or
     * a smaller.
     * The offset in pixels for the first string is 0, second string is 
     * laid out right after the first one with padding in between 
     * equal to the passed in value.
     *
     * @param firstStr the first string to use.
     * @param secondStr the first string to use.
     * @param width the available width for the text
     * @param firstFont the font to render the first string in
     * @param secondFont the font to render the second string in
     * @param pad the padding that should be used between strings
     * @return the width in which both strings would fit 
     *         given the maximum width
     */
    public static int getTwoStringsWidth(String firstStr, String secondStr,
                                         Font firstFont, Font secondFont,
                                         int width, int pad) {

        if (((firstStr == null || firstStr.length() == 0) &&
             (secondStr == null || secondStr.length() == 0)) ||
               (width <= 0)) {
            return 0;
        }


        int[] inout = new int[GNL_NUM_PARAMS];

        char[] text; 

        int offset = 0;
        int widest = 0;
        int numLines = 0;

        if (firstStr != null && firstStr.length() > 0) {

            text = firstStr.toCharArray();

            inout[GNL_FONT_HEIGHT]    = firstFont.getHeight();
            inout[GNL_WIDTH]          = width;
            inout[GNL_OPTIONS]        = Text.NORMAL;
            inout[GNL_ELLIP_WIDTH]    = firstFont.charsWidth(ellipsis, 0, 3);
            inout[GNL_LINE_START]     = 0;
            inout[GNL_LINE_END]       = 0;
            inout[GNL_NEW_LINE_START] = 0;
            inout[GNL_OFFSET]         = offset;
            inout[GNL_LINE_WIDTH]     = 0;


            do {
                
                numLines++;
                
                inout[GNL_NUM_LINES] = numLines;
                
                getNextLine(text, firstFont, inout);
                
                if (inout[GNL_LINE_WIDTH] > widest) {
                    widest = inout[GNL_LINE_WIDTH];
                }
                
                inout[GNL_LINE_START] = inout[GNL_NEW_LINE_START];
                inout[GNL_OFFSET] = 0;
                
            } while (inout[GNL_LINE_END] < firstStr.length());

            offset = inout[GNL_LINE_WIDTH];
        }

        if (secondStr != null && secondStr.length() > 0) {

            if (offset > 0) {
                offset += pad;
            }

            text = secondStr.toCharArray();

            if (numLines > 0) {
                numLines--;
            }

            inout[GNL_FONT_HEIGHT]    = secondFont.getHeight();
            inout[GNL_WIDTH]          = width;
            inout[GNL_OPTIONS]        = Text.NORMAL;
            inout[GNL_ELLIP_WIDTH]    = secondFont.charsWidth(ellipsis, 0, 3);
            inout[GNL_LINE_START]     = 0;
            inout[GNL_LINE_END]       = 0;
            inout[GNL_NEW_LINE_START] = 0;
            inout[GNL_OFFSET]         = offset;
            inout[GNL_LINE_WIDTH]     = 0;

            do {

                numLines++;
                
                inout[GNL_NUM_LINES] = numLines;
                
                getNextLine(text, secondFont, inout);
                
                if (inout[GNL_OFFSET] + inout[GNL_LINE_WIDTH] > widest) {
                    widest = inout[GNL_OFFSET] + inout[GNL_LINE_WIDTH];
                }
                
                inout[GNL_LINE_START] = inout[GNL_NEW_LINE_START];
                inout[GNL_OFFSET] = 0;
                
            } while (inout[GNL_LINE_END] < secondStr.length());
        }

        return widest;
    }

    /**
     * Utility method to calculate the heightin which two strings can fit 
     * given the strings, fonts and available width.
     * The offset in pixels for the first string is 0, second string is 
     * laid out right after the first one with padding in between 
     * equal to the passed in value.
     *
     * @param firstStr the first string to use (can be null or empty)
     * @param secondStr the first string to use (can be null or empty)
     * @param width the available width for the text
     * @param firstFont the font to render the first string in (non-null)
     * @param secondFont the font to render the second string in (non-null)
     * @param pad the padding that should be used between strings
     * @return the height in which both strings would fit 
     *         given the passed in width
     */
    public static int getTwoStringsHeight(String firstStr, String secondStr,
                                          Font firstFont, Font secondFont,
                                          int width, int pad) {
        if (((firstStr == null || firstStr.length() == 0) &&
             (secondStr == null || secondStr.length() == 0)) ||
               (width <= 0)) {
            return 0;
        }


        int[] inout = new int[GNL_NUM_PARAMS];

        char[] text; 

        int offset = 0;
        int widest = 0;
        int numLines = 0;
        int height = 0;
        int fontHeight = 0;

        if (firstStr != null && firstStr.length() > 0) {

            text = firstStr.toCharArray();

            fontHeight = firstFont.getHeight();

            inout[GNL_FONT_HEIGHT]    = fontHeight;
            inout[GNL_WIDTH]          = width;
            inout[GNL_OPTIONS]        = Text.NORMAL;
            inout[GNL_ELLIP_WIDTH]    = firstFont.charsWidth(ellipsis, 0, 3);
            inout[GNL_LINE_START]     = 0;
            inout[GNL_LINE_END]       = 0;
            inout[GNL_NEW_LINE_START] = 0;
            inout[GNL_OFFSET]         = 0;
            inout[GNL_LINE_WIDTH]     = 0;


            do {
                
                numLines++;
                height += fontHeight;

                inout[GNL_NUM_LINES] = numLines;
                
                getNextLine(text, firstFont, inout);
                
                inout[GNL_LINE_START] = inout[GNL_NEW_LINE_START];
                
            } while (inout[GNL_LINE_END] < firstStr.length());

            offset = inout[GNL_LINE_WIDTH];

            if (secondStr == null || secondStr.length() == 0) {
                // last \n in the two strings should be ignored
                if (firstStr.charAt(firstStr.length() - 1) == '\n') {
                    height -= fontHeight;
                }
                return height;
            }
        }

        // Second string is not null and it is not empty
        if (secondStr != null && secondStr.length() > 0) {
            if (offset > 0) {
                offset += pad;
            }

            text = secondStr.toCharArray();

            fontHeight = secondFont.getHeight();

            // Line that has the end of the first string and the beginning
            // of the second one is a special one;
            // We have to make sure that it is not counted twice and that
            // the right font height is beeing added (the max of the two)
            if (numLines > 0) {
                numLines--;
                if (inout[GNL_FONT_HEIGHT] > fontHeight) {
                    height -= fontHeight;
                } else {
                    height -= inout[GNL_FONT_HEIGHT];
                }
            }

            inout[GNL_FONT_HEIGHT]    = fontHeight;
            inout[GNL_WIDTH]          = width;
            inout[GNL_OPTIONS]        = Text.NORMAL;
            inout[GNL_ELLIP_WIDTH]    = secondFont.charsWidth(ellipsis, 0, 3);
            inout[GNL_LINE_START]     = 0;
            inout[GNL_LINE_END]       = 0;
            inout[GNL_NEW_LINE_START] = 0;
            inout[GNL_OFFSET]         = offset;
            inout[GNL_LINE_WIDTH]     = 0;

            do {

                numLines++;
                height += fontHeight;

                inout[GNL_NUM_LINES] = numLines;
                
                getNextLine(text, secondFont, inout);
                                
                inout[GNL_LINE_START] = inout[GNL_NEW_LINE_START];
                inout[GNL_OFFSET] = 0;
                
            } while (inout[GNL_LINE_END] < secondStr.length());

            // last \n should be ignored
            if (secondStr.charAt(secondStr.length() - 1) == '\n') {
                height -= fontHeight;
            }
        }

        return height;
    }
}

