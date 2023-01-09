/*
 * @(#)Spacer.java	1.21 02/10/07 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;
import java.lang.IllegalArgumentException;

/**
 * A blank, non-interactive item that has a settable minimum size.  The
 * minimum width is useful for allocating flexible amounts of space between
 * <code>Items</code> within the same row of a
 * <code>Form</code>.  The minimum height is useful for
 * enforcing a particular minimum height of a row.  The application can set 
 * the minimum width or height to any non-negative value.  The implementation 
 * may enforce implementation-defined maximum values for the minimum width and 
 * height.
 *
 * <p>The unlocked preferred width of a <code>Spacer</code> is the same as its
 * current minimum width.  Its unlocked preferred height is the same as its
 * current minimum height.</p>
 * 
 * <p>Since a <code>Spacer's</code> primary purpose is to position
 * other items, it is
 * restricted to be non-interactive, and the application is not allowed to add
 * <code>Commands</code> to a <code>Spacer</code>.  Since the
 * presence of a label on an <code>Item</code> may affect
 * layout in device-specific ways, the label of a
 * <code>Spacer</code> is restricted to
 * always be <code>null</code>, and the application is not allowed
 * to change it.</p>
 * 
 * @since MIDP 2.0
 */

public class Spacer extends Item {

    /**
     * Creates a new <code>Spacer</code> with the given minimum
     * size.  The <code>Spacer's</code> label
     * is <code>null</code>.
     * The minimum size must be zero or greater.
     * If <code>minWidth</code> is greater than the
     * implementation-defined maximum width, the maximum
     * width will be used instead.
     * If <code>minHeight</code> is greater than the
     * implementation-defined maximum height, the maximum
     * height will be used instead.
     *
     * @param minWidth the minimum width in pixels
     * @param minHeight the minimum height in pixels
     * @throws IllegalArgumentException if either <code>minWidth</code>
     * or <code>minHeight</code> is less than zero
     */
    public Spacer(int minWidth, int minHeight) {
        super(null);
        updateSizes(minWidth, minHeight);
    }

    /**
     * Sets the minimum size for this spacer.  The
     * <code>Form</code> will not
     * be allowed to make the item smaller than this size.
     * The minimum size must be zero or greater.
     * If <code>minWidth</code> is greater than the
     * implementation-defined maximum width, the maximum
     * width will be used instead.
     * If <code>minHeight</code> is greater than the
     * implementation-defined maximum height, the maximum
     * height will be used instead.
     * 
     * @param minWidth the minimum width in pixels
     * @param minHeight the minimum height in pixels
     * @throws IllegalArgumentException if either <code>minWidth</code>
     * or <code>minHeight</code> is less than zero
     */
    public void setMinimumSize(int minWidth, int minHeight) {
        updateSizes(minWidth, minHeight);
    }

    /**
     * <code>Spacers</code> are restricted from having
     * <code>Commands</code>, so this method will always
     * throw <code>IllegalStateException</code> whenever it is called.
     *
     * @param cmd the <code>Command</code>
     *
     * @throws IllegalStateException always
     */
    public void addCommand(Command cmd) {
        throw new IllegalStateException();
    }

    /**
     * Spacers are restricted from having <code>Commands</code>,
     * so this method will always
     * throw <code>IllegalStateException</code> whenever it is called.
     *
     * @param cmd the <code>Command</code>
     *
     * @throws IllegalStateException always
     */
    public void setDefaultCommand(Command cmd) {
        throw new IllegalStateException();
    }

    /**
     * <code>Spacers</code> are restricted to having
     * <code>null</code> labels, so this method will
     * always throw 
     * <code>IllegalStateException</code> whenever it is called.
     * 
     * @param label the label string
     *
     * @throws IllegalStateException always
     */
    public void setLabel(String label) { 
        throw new IllegalStateException();
    }

    // package private implementation

    /**
     * Get the minimum width of this Item
     *
     * @return the minimum width
     */
    int callMinimumWidth() {
        return width;
    }

    /**
     * Get the preferred width of this Item
     *
     * @param h the tentative content height in pixels, or -1 if a
     * tentative height has not been computed
     * @return the preferred width
     */
    int callPreferredWidth(int h) {
        return width;
    }

    /**
     * Get the minimum height of this Item
     *
     * @return the minimum height
     */
    int callMinimumHeight() {
        return height;
    }

    /**
     * Get the preferred height of this Item
     *
     * @param w the tentative content width in pixels, or -1 if a
     * tentative width has not been computed
     * @return the preferred height
     */
    int callPreferredHeight(int w) {
        return height;
    }

    /**
     * Determine if this Item should not be traversed to
     *
     * @return true if this Item should not be traversed to
     */
    boolean shouldSkipTraverse() {
        return true;
    }

    /**
     * Paint the content of this Item
     *
     * @param g the Graphics object to be used for rendering the item
     * @param w current width of the item in pixels
     * @param h current height of the item in pixels
     */
    void callPaint(Graphics g, int w, int h) {
        /*
         * There's no reason to erase anything because Form will erase
         * any dirty region for us
         *
        g.setColor(Display.ERASE_COLOR);
	    g.fillRect(g.getClipX(), g.getClipY(),
                   g.getClipWidth(), g.getClipHeight());
        g.setColor(Display.FG_COLOR);
        */
    }

    /**
     * Update the width and height values of this spacer, guaranteeing
     * the new values are positive. This method will also invalidate the
     * containing Form.
     *
     * @param minW the new width
     * @param minH the new height
     */
    private void updateSizes(int minW, int minH) {
        if (minW < 0 || minH < 0) {
             throw new IllegalArgumentException();
        }

        synchronized (Display.LCDUILock) {
            width  = minW;
            height = minH;
            invalidate();
        }
    }

    /** The preferred (and minimum) width of this Spacer */
    private int width;

    /** The preferred (and minimum) height of this Spacer */
    private int height;

}
