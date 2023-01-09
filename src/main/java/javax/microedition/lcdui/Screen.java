/*
 * @(#)Screen.java	1.157 02/09/11 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

import java.util.TimerTask;
import java.util.Timer;

/**
 * The common superclass of all high-level user interface classes. The 
 * contents displayed and their interaction with the user are defined by 
 * subclasses.
 *
 * <P>Using subclass-defined methods, the application may change the contents
 * of a <code>Screen</code> object while it is shown to the user.  If
 * this occurs, and the
 * <code>Screen</code> object is visible, the display will be updated
 * automatically.  That
 * is, the implementation will refresh the display in a timely fashion without
 * waiting for any further action by the application.  For example, suppose a
 * <code>List</code> object is currently displayed, and every element
 * of the <code>List</code> is
 * visible.  If the application inserts a new element at the beginning of the
 * <code>List</code>, it is displayed immediately, and the other
 * elements will be
 * rearranged appropriately.  There is no need for the application to call
 * another method to refresh the display.</P>
 *
 * <P>It is recommended that applications change the contents of a
 * <code>Screen</code> only
 * while it is not visible (that is, while another
 * <code>Displayable</code> is current).
 * Changing the contents of a <code>Screen</code> while it is visible
 * may result in
 * performance problems on some devices, and it may also be confusing if the
 * <code>Screen's</code> contents changes while the user is
 * interacting with it.</P>
 *
 * <P>In MIDP 2.0 the four <code>Screen</code> methods that defined
 * read/write ticker and
 * title properties were moved to <code>Displayable</code>,
 * <code>Screen's</code> superclass.  The
 * semantics of these methods have not changed.</P>
 * 
 * @since MIDP 1.0
 */

public abstract class Screen extends Displayable  {

// ************************************************************
//  public member variables
// ************************************************************

// ************************************************************
//  protected member variables
// ************************************************************

// ************************************************************
//  package private member variables
// ************************************************************

    /** Special content font, shared amongst the LCDUI package */
    final static Font CONTENT_FONT  = Font.getDefaultFont();

    /** Special content height, shared amongst the LCDUI package */
    final static int CONTENT_HEIGHT = CONTENT_FONT.getHeight();

    /**
     * When paintBorder is not BORDER_NONE, there is a pixel border 
     * painted around content and viewPort is reduced by 4 pixels. 
     * This is used by subclasses such as TextBox which need a pixel 
     * border around them.
     */
    int paintBorder;             // = BORDER_NONE;
 
    /** this is for no border */
    final static int BORDER_NONE  = 0;
    /** this is for a solid border */
    final static int BORDER_SOLID = 1;
    /** this is for a dotted light grey border */
    final static int BORDER_GRAY  = 2;


// MARK                        

    /**
     * A boolean declaring whether the viewport is capable of
     * scrolling horizontally. FALSE by default
     */
    final static boolean SCROLLS_HORIZONTAL = false;

    /**
     * A boolean declaring whether the viewport is capable of
     * scrolling vertically. TRUE by default
     */
    final static boolean SCROLLS_VERTICAL = true;

    /**
     * An array which holds the scroll location and 
     * the overall dimensions of the view being
     * shown in the parent Displayable's viewport
     * Note that the following is always true.
     * 0 <= view[X] <= view[WIDTH] - viewport[WIDTH]
     * 0 <= view[Y] <= view[HEIGHT] - viewport[HEIGHT]
     */
    int view[];

    /**
     * Screens should automatically reset to the top of the when
     * they are shown, except in cases where it is interrupted by
     * a system menu or an off-screen editor - in which case it
     * should be reshown exactly as it was.
     */
    boolean resetToTop = true;

// ************************************************************
//  private member variables
// ************************************************************

// ************************************************************
//  Static initializer, constructor
// ************************************************************

    /**
     * Creates a new Screen object with no title and no ticker.
     */
    Screen() {
        this(null);
    }

    /**
     * Creates a new Screen object with the given title and with no ticker.
     *
     * @param title the Screen's title, or null for no title
     */
    Screen(String title) {
        view = new int[4];
        view[X] = 0;
        view[Y] = 0;
        view[WIDTH] = 0;
        view[HEIGHT] = 0;

        super.setTitleImpl(title);
    }

// ************************************************************
//  public methods
// ************************************************************

// ************************************************************
//  protected methods
// ************************************************************

// ************************************************************
//  package private methods
// ************************************************************

    /**
     * Layout the contents of this screen. Calls super.layout()
     * and then translateViewport();
     */
    void layout() {
        super.layout();
        translateViewport();
    }

    /**
     * Paint an Item contained in this Screen. The Item requests a paint
     * in its own coordinate space. Screen translates those coordinates
     * into the overall coordinate space and schedules the repaint
     *
     * @param item the Item requesting the repaint
     * @param x the x-coordinate of the origin of the dirty region
     * @param y the y-coordinate of the origin of the dirty region
     * @param w the width of the dirty region
     * @param h the height of the dirty region
     */
    void repaintItem(Item item, int x, int y, int w, int h) {

        callRepaint(item.bounds[X] + viewport[X] - view[X] + x,
                    item.bounds[Y] + viewport[Y] - view[Y] + y,
                    w,
                    h,
                    item);
    }

    /**
     * Translate the viewport[] array so that subclasses only render
     * within the viewport
     */
    private void translateViewport() {
        if (paintBorder != BORDER_NONE) {
            // translate the viewport for subclasses layout()
            // to utilize
            viewport[X] += 4;
            viewport[Y] += 4;
            viewport[WIDTH] -= 8;
            viewport[HEIGHT] -= 8;
        }
    }

    /**
     * Paint the contents of this Screen
     *
     * @param g the Graphics to paint to
     * @param target the target Object of this repaint
     */
    void callPaint(Graphics g, Object target) {
        super.callPaint(g, target);

        /*
        System.err.println("Screen:Clip: " +
            g.getClipX() + "," + g.getClipY() + "," +
            g.getClipWidth() + "," + g.getClipHeight());
        */

        synchronized (Display.LCDUILock) {
            if ((paintBorder != BORDER_NONE) && (g.getClipX() < viewport[X])) {
                // erase the border and its surrounding rectangles
                g.setColor(Display.ERASE_COLOR);
                g.drawRect(viewport[X] - 4, viewport[Y] - 4,
                           viewport[WIDTH] + 7, viewport[HEIGHT] + 7);
                g.drawRect(viewport[X] - 3, viewport[Y] - 3,
                           viewport[WIDTH] + 5, viewport[HEIGHT] + 5);
                g.drawRect(viewport[X] - 2, viewport[Y] - 2,
                           viewport[WIDTH] + 3, viewport[HEIGHT] + 3);
                g.drawRect(viewport[X] - 1, viewport[Y] - 1,
                           viewport[WIDTH] + 1, viewport[HEIGHT] + 1);

                // Draw the border
                if (paintBorder == BORDER_SOLID) {
                    // solid border
                    g.setColor(Display.FG_COLOR);
                } else {
                    // light gray dotted border
                    g.setColor(Display.BORDER_COLOR);
                    g.setStrokeStyle(Graphics.DOTTED);
                }
                g.drawRect(viewport[X] - 3, viewport[Y] - 3,
                           viewport[WIDTH] + 5, viewport[HEIGHT] + 5);

                // restore the line style
                g.setStrokeStyle(Graphics.SOLID);
            }

            // Clip off the ticker/title area to keep subclasses from
            // painting on it
            g.clipRect(viewport[X], viewport[Y],
                    viewport[WIDTH],
                    viewport[HEIGHT]);

            if (target == null || (
                    target instanceof Item && ((Item)target).owner == this)) {

                g.setColor(Display.ERASE_COLOR);
                g.fillRect(g.getClipX(), g.getClipY(),
                           g.getClipWidth(), g.getClipHeight());
                g.setColor(Display.FG_COLOR);
            }
        } // synchronized
    }

    /**
     * Set the vertical scroll indicators for this Screen
     */
    void setVerticalScroll() {

        // Previously, Screen was always resetting the left/right
        // scroll indicators to 0 when it set the up/down indicators,
        // now it sets the up/down indicators as appropriate, but
        // maintains the left/right indicators as they were, and it
        // is the component's responsibility (ie Gauge) to update
        // the right/left scroll indicators if necessary
        if (view[HEIGHT] <= viewport[HEIGHT]) {
            super.setVerticalScroll(0, 100);
        } else {
            super.setVerticalScroll(
                (view[Y] * 100 / (view[HEIGHT] - viewport[HEIGHT])),
                (viewport[HEIGHT] * 100 / view[HEIGHT]));
        }
    }

}


