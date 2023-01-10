/*
 * @(#)TextCursor.java	1.8 02/08/19 @(#)
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

/**
 * Class that represents the character index, and (x,y) position
 * of a text cursor in a TextField
 */
public class TextCursor {

    /** x, y coordinates */
    public int x, y;
   
    /** width, height */
    public int width, height;
   
    /** array index */
    public int index;

    /** drawing options: can be one of the PAINT_* variables in Text.java */
    public int option;

    /** whether or not this cursor is visible */
    public boolean visible;

    /** preferred x location when traversing vertically */
    public int preferredX;

    /**
     * Construct a new text cursor with the given array index
     * 
     * @param index index into the array that this cursor will be drawn
     */
    public TextCursor(int index) {
        this.index = index;
        option = Text.PAINT_USE_CURSOR_INDEX;
        visible = true;
    }

    /**
     * Copy a TextCursor object
     *
     * @param tc TextCursor object to copy
     */
    public TextCursor(TextCursor tc) {
        this(0);

        if (tc != null) {
            this.x       = tc.x;
            this.y       = tc.y;
            this.option  = tc.option;
            this.index   = tc.index;
            this.visible = tc.visible;
        }
    }

    /**
     * Paint this cursor in the given graphics context
     *
     * @param g the graphics context to paint in
     */
    public void paint(Graphics g) {

        int stroke = g.getStrokeStyle();
        g.setStrokeStyle(g.SOLID);
        g.drawLine(x - 1, y - height, (x - 1) + (width - 1), y);
        g.setStrokeStyle(stroke);
        
    }

}

