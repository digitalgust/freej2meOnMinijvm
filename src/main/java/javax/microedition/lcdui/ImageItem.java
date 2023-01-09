/*
 * @(#)ImageItem.java	1.95 02/10/07 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.Text;

/**
 * An item that can contain an image.
 *
 * <P> Each <code>ImageItem</code> object contains a reference to an
 * {@link Image} object.
 * This <code>Image</code> may be mutable or immutable.  If the
 * <code>Image</code> is mutable, the
 * effect is as if snapshot of its contents is taken at the time the
 * <code>ImageItem</code>
 * is constructed with this <code>Image</code> and when
 * <code>setImage</code> is called with an <code>Image</code>.
 * The snapshot is used whenever the contents of the
 * <code>ImageItem</code> are to be
 * displayed.  Even if the application subsequently draws into the
 * <code>Image</code>, the
 * snapshot is not modified until the next call to
 * <code>setImage</code>.  The snapshot is
 * <em>not</em> updated when the container of the
 * <code>ImageItem</code> becomes current or
 * becomes visible on the display.  (This is because the application does not
 * have control over exactly when <code>Displayables</code> and Items
 * appear and disappear
 * from the display.)</P>
 * 
 * <P>The value <code>null</code> may be specified for the image
 * contents of an <code>ImageItem</code>.
 * If
 * this occurs (and if the label is also <code>null</code>) the
 * <code>ImageItem</code> will occupy no
 * space on the screen. </p>
 *
 * <p><code>ImageItem</code> contains layout directives that were
 * originally defined in
 * MIDP 1.0.  These layout directives have been moved to the
 * {@link Item} class and now apply to all items.  The declarations are left 
 * in <code>ImageItem</code> for source compatibility purposes.</p>
 * 
 * <P>The <code>altText</code> parameter specifies a string to be
 * displayed in place of the
 * image if the image exceeds the capacity of the display. The
 * <code>altText</code>
 * parameter may be <code>null</code>.</P>
 *
 * @since MIDP 1.0
 */

public class ImageItem extends Item {

    /**
     * See {@link Item#LAYOUT_DEFAULT}.
     * 
     * <P>Value <code>0</code> is assigned to <code>LAYOUT_DEFAULT</code>.</P>
     */
    public final static int LAYOUT_DEFAULT = 0;

    /**
     * See {@link Item#LAYOUT_LEFT}.
     * 
     * <P>Value <code>1</code> is assigned to <code>LAYOUT_LEFT</code>.</P>
     */
    public final static int LAYOUT_LEFT = 1;
  
    /**
     * See {@link Item#LAYOUT_RIGHT}.
     * 
     * <P>Value <code>2</code> is assigned to <code>LAYOUT_RIGHT</code>.</P>
     */
    public final static int LAYOUT_RIGHT = 2;
  
    /**
     * See {@link Item#LAYOUT_CENTER}.
     * 
     * <P>Value <code>3</code> is assigned to <code>LAYOUT_CENTER</code>.</P>
     */
    public final static int LAYOUT_CENTER = 3;
  
    /**
     * See {@link Item#LAYOUT_NEWLINE_BEFORE}.
     * 
     * <P>Value <code>0x100</code> is assigned to
     * <code>LAYOUT_NEWLINE_BEFORE</code>.</P>
     */
    public final static int LAYOUT_NEWLINE_BEFORE = 0x100;
  
    /**
     * See {@link Item#LAYOUT_NEWLINE_AFTER}.
     * 
     * <P>Value <code>0x200</code> is assigned to
     * <code>LAYOUT_NEWLINE_AFTER</code>.</P>
     */
    public final static int LAYOUT_NEWLINE_AFTER = 0x200;
  
    /**
     * Creates a new <code>ImageItem</code> with the given label, image, layout
     * directive, and alternate text string.  Calling this constructor is 
     * equivalent to calling 
     *
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *    ImageItem(label, image, layout, altText, PLAIN);     </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * @param label the label string
     * @param img the image, can be mutable or immutable
     * @param layout a combination of layout directives
     * @param altText the text that may be used in place of the image
     * @throws IllegalArgumentException if the <code>layout</code> value is not
     * a legal combination of directives
     * @see #ImageItem(String, Image, int, String, int)
     */
    public ImageItem(String label, Image img, int layout, String altText) {
        super(label);

        synchronized (Display.LCDUILock) {
            setImageImpl(img);
            setLayoutImpl(layout);
            this.altText = altText;
        }
    }

    /**
     * Creates a new <code>ImageItem</code> object with the given label, image,
     * layout directive, alternate text string, and appearance mode.
     * Either label or alternative text may be present or <code>null</code>.
     *
     * <p>The <code>appearanceMode</code> parameter
     * (see <a href="Item.html#appearance">Appearance Modes</a>)
     * is a hint to the platform of the application's intended use
     * for this <code>ImageItem</code>. To provide hyperlink- or
     * button-like behavior,
     * the application should associate a default <code>Command</code> with this
     * <code>ImageItem</code> and add an
     * <code>ItemCommandListener</code> to this
     * <code>ImageItem</code>.
     * 
     * <p>Here is an example showing the use of an
     * <code>ImageItem</code> as a button: <p>
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     ImageItem imgItem = 
     *         new ImageItem("Default: ", img,     
     *                       Item.LAYOUT_CENTER, null,    
     *                       Item.BUTTON);    
     *     imgItem.setDefaultCommand(
     *         new Command("Set", Command.ITEM, 1); 
     *     // icl is ItemCommandListener   
     *     imgItem.setItemCommandListener(icl);      </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     *
     * @param label the label string
     * @param image the image, can be mutable or immutable
     * @param layout a combination of layout directives
     * @param altText the text that may be used in place of the image
     * @throws IllegalArgumentException if the <code>layout</code> value is not
     * a legal combination of directives
     * @param appearanceMode the appearance mode of the <code>ImageItem</code>,
     * one of {@link #PLAIN}, {@link #HYPERLINK}, or {@link #BUTTON}
     * @throws IllegalArgumentException if <code>appearanceMode</code> invalid
     *
     * @since MIDP 2.0
     */
    public ImageItem(String label, Image image, int layout, String altText,
                     int appearanceMode) {

        this(label, image, layout, altText);

        synchronized (Display.LCDUILock) {
            switch (appearanceMode) {
            case Item.PLAIN:
            case Item.HYPERLINK:
            case Item.BUTTON:
                this.appearanceMode = appearanceMode;
                break;
            default:
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Gets the image contained within the <code>ImageItem</code>, or
     * <code>null</code> if there is no
     * contained image.
     * @return image used by the <code>ImageItem</code>
     * @see #setImage
     */
    public Image getImage() {
        synchronized (Display.LCDUILock) {
            return mutImg == null ? img : mutImg;
        }
    }

    /**
     * Sets the <code>Image</code> object contained within the
     * <code>ImageItem</code>.  The image may be
     * mutable or immutable.  If <code>img</code> is
     * <code>null</code>, the <code>ImageItem</code> is set to be
     * empty.  If <code>img</code> is mutable, the effect is as if a
     * snapshot is taken of
     * <code>img's</code> contents immediately prior to the call to
     * <code>setImage</code>.  This
     * snapshot is used whenever the contents of the
     * <code>ImageItem</code> are to be
     * displayed.  If <code>img</code> is already the
     * <code>Image</code> of this <code>ImageItem</code>, the effect
     * is as if a new snapshot of img's contents is taken.  Thus, after 
     * painting into a mutable image contained by an
     * <code>ImageItem</code>, the
     * application can call 
     * 
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *    imageItem.setImage(imageItem.getImage());       </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     *
     * <p>to refresh the <code>ImageItem's</code> snapshot of its Image.</p>
     *
     * <p>If the <code>ImageItem</code> is visible on the display when
     * the snapshot is
     * updated through a call to <code>setImage</code>, the display is
     * updated with the new
     * snapshot as soon as it is feasible for the implementation to so do.</p>
     *
     * @param img the <code>Image</code> for this
     * <code>ImageItem</code>, or <code>null</code> if none
     * @see #getImage
     */
    public void setImage(Image img) {
        synchronized (Display.LCDUILock) {
            setImageImpl(img);
            invalidate();
        }
    }

    /**
     * Gets the text string to be used if the image exceeds the device's
     * capacity to display it.
     *
     * @return the alternate text value, or <code>null</code> if none
     * @see #setAltText
     */
    public String getAltText() { 
        // SYNC NOTE: return of atomic value, no locking necessary
        return altText;
    }

    /**
     * Sets the alternate text of the <code>ImageItem</code>, or
     * <code>null</code> if no alternate text is provided.
     * @param text the new alternate text
     * @see #getAltText
     */
    public void setAltText(String text) {
        // SYNC NOTE: atomic, no locking necessary
        this.altText = text;
    }

    /**
     * Gets the layout directives used for placing the image.
     * @return a combination of layout directive values
     * @see #setLayout
     */
    public int getLayout() {
        // NOTE: looks odd, but this method is required for 1.0 compatiblitiy
        return super.getLayout();
    }

    /**
     * Sets the layout directives.
     * @param layout a combination of layout directive values
     * @throws IllegalArgumentException if the value of <code>layout</code>
     * is not a valid
     * combination of layout directives
     * @see #getLayout
     */
    public void setLayout(int layout) {
        // NOTE: looks odd, but this method is required for 1.0 compatiblitiy
        super.setLayout(layout);
    }

    /** 
     * Returns the appearance mode of the <code>ImageItem</code>.
     * See <a href="Item.html#appearance">Appearance Modes</a>.
     *
     * @return the appearance mode value,
     * one of {@link #PLAIN}, {@link #HYPERLINK}, or {@link #BUTTON}
     * 
     * @since MIDP 2.0
     */
    public int getAppearanceMode() {
        return appearanceMode;
    }

    // package private implementation

    /**
     * Get the minimum width of this Item
     *
     * @return the minimum width
     */
    int callMinimumWidth() {
        return callPreferredWidth(-1);
    }

    /**
     * Get the preferred width of this Item
     *
     * @param h the tentative content height in pixels, or -1 if a
     * tentative height has not been computed
     * @return the preferred width
     */
    int callPreferredWidth(int h) {
        if (img == null) {
            return getLabelWidth();
        }

        if (numCommands >= 1) {
            if (this.appearanceMode == Item.BUTTON) {
                return img.getWidth() + (BUTTON_BORDER + BUTTON_PAD) * 2;
            } else if (this.appearanceMode == Item.HYPERLINK) {
                return img.getWidth() + 
                    (VERTICAL_HYPERLINK_IMG.getWidth() + HYPERLINK_PAD) * 2;
            }
        }

        int labelWidth = getLabelWidth();
        int imageW = img.getWidth();
        return (labelWidth > imageW ? labelWidth : imageW);
    }

    /**
     * Get the minimum height of this Item
     *
     * @return the minimum height
     */
    int callMinimumHeight() {
        return callPreferredHeight(-1);
    }

    /**
     * Get the preferred height of this Item
     *
     * @param w the tentative content width in pixels, or -1 if a
     * tentative width has not been computed
     * @return the preferred height
     */
    int callPreferredHeight(int w) {
        if (img == null) {
            return getLabelHeight(w);
        }

        if (numCommands >= 1) {
            if (this.appearanceMode == Item.BUTTON) {
                return img.getHeight() + getLabelHeight(w) +
                       (BUTTON_BORDER + BUTTON_PAD) * 2;
            } else if (this.appearanceMode == Item.HYPERLINK) {
                return img.getHeight() + getLabelHeight(w) +
                       (HYPERLINK_IMG.getHeight() + HYPERLINK_PAD) * 2;
            }
        }

        return img.getHeight() + getLabelHeight(w);
    }

    /**
     * Paint this ImageItem
     *
     * @param g the Graphics context to paint to
     * @param width the width of this ImageItem
     * @param height the height of this ImageItem
     */
    void callPaint(Graphics g, int width, int height) {

        int labelHeight = super.paintLabel(g, width);

        if (img == null) {
            return;
        }

        int x = 0;
        int y = labelHeight;
        int l = getLayout() & ImageItem.LAYOUT_CENTER;

        if (l == ImageItem.LAYOUT_CENTER) {
            x = width / 2;
            if ((numCommands >= 1) &&
               (this.appearanceMode == Item.BUTTON)) {
                x += BUTTON_BORDER + BUTTON_PAD;
                y += BUTTON_BORDER + BUTTON_PAD;
            } else if ((numCommands >= 1) &&
                (this.appearanceMode == Item.HYPERLINK)) {
                x += VERTICAL_HYPERLINK_IMG.getWidth() + HYPERLINK_PAD;
                y += HYPERLINK_IMG.getHeight() + HYPERLINK_PAD;
            }

            g.drawImage(img, x, y,
                        Graphics.TOP | Graphics.HCENTER);

        } else if (l == ImageItem.LAYOUT_RIGHT) {
            x = width;
            if ((numCommands >= 1) &&
               (this.appearanceMode == Item.BUTTON)) {
                x -= (BUTTON_BORDER + BUTTON_PAD);
                y += BUTTON_BORDER + BUTTON_PAD;
            } else if ((numCommands >= 1) &&
                (this.appearanceMode == Item.HYPERLINK)) {
                x -= VERTICAL_HYPERLINK_IMG.getWidth() + HYPERLINK_PAD;
                y += HYPERLINK_IMG.getHeight() + HYPERLINK_PAD;
            }

            g.drawImage(img, x, y,
                        Graphics.TOP | Graphics.RIGHT);

        } else {
            // use x = 0;
            if ((numCommands >= 1) &&
               (this.appearanceMode == Item.BUTTON)) {
                x += BUTTON_BORDER + BUTTON_PAD;
                y += BUTTON_BORDER + BUTTON_PAD;
            } else if ((numCommands >= 1) &&
                (this.appearanceMode == Item.HYPERLINK)) {
                x += VERTICAL_HYPERLINK_IMG.getWidth() + HYPERLINK_PAD;
                y += HYPERLINK_IMG.getHeight() + HYPERLINK_PAD;
            }

            g.drawImage(img, x, y,
                        Graphics.TOP | Graphics.LEFT);
        }

        // draw the button border or hyperlink image

        if ((numCommands >= 1) &&
            (this.appearanceMode == Item.BUTTON)) {
            int w = img.getWidth();
            int h = img.getHeight();
            // y = labelHeight + vertPad/2;
            y = labelHeight;

            if (l == ImageItem.LAYOUT_CENTER) {
                x = (width / 2) - (w / 2);
            } else if (l == ImageItem.LAYOUT_RIGHT) {
                x = width - w - 2 * (BUTTON_BORDER + BUTTON_PAD);
            } else {
                x = 0;
            }

            w += 2 * (BUTTON_BORDER + BUTTON_PAD);
            h += 2 * (BUTTON_BORDER + BUTTON_PAD);
            drawButtonBorder(g, x, y, w, h, hasFocus);

        } else if ((numCommands >= 1) &&
            (this.appearanceMode == Item.HYPERLINK)) {
            // NOTE: We test to see if the width of the Image
            // in the ImageItem is
            // wider than the width of the hyperlink image,
            // if so, we re-draw
            // the image offset to the right as many times as necessary
            // to fill the width of the Image in the ImageItem.

            int imageItemWidth = img.getWidth();
            int imageItemHeight = img.getHeight();
            y = labelHeight;

            if (l == ImageItem.LAYOUT_CENTER) {
                drawTop_BottomBorder(g,
                    width / 2 - imageItemWidth / 2
                    - HYPERLINK_PAD - VERTICAL_HYPERLINK_IMG.getWidth(),
                    width / 2 + imageItemWidth / 2 + 2 * HYPERLINK_PAD,
                    y, y + HYPERLINK_IMG.getHeight()
                    + imageItemHeight + 2 * HYPERLINK_PAD);
                drawLeft_RightBorder(g, y,
                    y + imageItemHeight + (2 * HYPERLINK_PAD),
                    width / 2 - imageItemWidth / 2
                    - VERTICAL_HYPERLINK_IMG.getHeight(),
                    width / 2 + imageItemWidth / 2 + 2 * HYPERLINK_PAD);
            } else if (l == ImageItem.LAYOUT_RIGHT) {
                drawTop_BottomBorder(g,
                    width - imageItemWidth
                    - 2 * HYPERLINK_PAD - VERTICAL_HYPERLINK_IMG.getWidth(),
                    width - VERTICAL_HYPERLINK_IMG.getWidth(),
                    y, y + HYPERLINK_IMG.getHeight()
                    + imageItemHeight + 2 * HYPERLINK_PAD);
                drawLeft_RightBorder(g, y,
                    y + imageItemHeight + 2 * HYPERLINK_PAD,
                    width - imageItemWidth
                    - HYPERLINK_PAD - VERTICAL_HYPERLINK_IMG.getHeight(),
                    width - VERTICAL_HYPERLINK_IMG.getWidth());
            } else {
                drawTop_BottomBorder(g, 0,
                    imageItemWidth + 2 * HYPERLINK_PAD,
                    y, y + HYPERLINK_IMG.getHeight()
                    + imageItemHeight + 2 * HYPERLINK_PAD);
                drawLeft_RightBorder(g, y,
                    y + imageItemHeight + 2 * HYPERLINK_PAD,
                    0,  HYPERLINK_IMG.getHeight()
                    + imageItemWidth + 2 * HYPERLINK_PAD);
            }
        }// end if HYPERLINK
    }

    /**
     * Called by the system to signal a key press
     *
     * @param keyCode the key code of the key that has been pressed
     * @see #getInteractionModes
     */
    void callKeyPressed(int keyCode) {
        if (keyCode != Display.KEYCODE_SELECT) {
            return;
        }

        if (getCommandCount() == 0 || commandListener == null) {
            return;
        }

        ItemCommandListener cl = null;
        Command defaultCmd = null;

        synchronized (Display.LCDUILock) {
            cl = commandListener;
            defaultCmd = defaultCommand;
        } // synchronized


        // SYNC NOTE: The call to the listener must occur outside
        // of the lock
        if (cl != null) {
            try {
                // SYNC NOTE: We lock on calloutLock around any calls
                // into application code
                synchronized (Display.calloutLock) {
                    if (defaultCmd != null) {
                        cl.commandAction(defaultCmd, this);
                    } else {
                        // REMINDER : Needs HI decision
                        // either call the first command
                        // from  the command list or
                        // invoke the menu
                    }
                }
            } catch (Throwable thr) {
                Display.handleThrowable(thr);
            }
        }
    }

    /**
     *  Adds a context sensitive Command to the image item.
     * @param cmd the command to be removed
     */

    void addCommandImpl(Command cmd) {
        synchronized (Display.LCDUILock) {
            super.addCommandImpl(cmd);

            if ((numCommands >= 1) && (appearanceMode == Item.PLAIN)) {
                // restore the value of the original appearanceMode
                // if it is a button
                // otherwise simple change the appearanceMode
                // to a hyperlink
                this.appearanceMode =
                    originalAppearanceMode == Item.BUTTON ?
                    Item.BUTTON : Item.HYPERLINK;

                invalidate();
            }
        } // synchronized
    }

    /**
     *  Removes the context sensitive command from item.
     * @param cmd the command to be removed
     */
    void removeCommandImpl(Command cmd) {
        synchronized (Display.LCDUILock) {
            super.removeCommandImpl(cmd);

            if ((numCommands < 1) && (appearanceMode != Item.PLAIN)) {
                // store value of the original appearanceMode
                originalAppearanceMode = this.appearanceMode;
                // change the appearanceMode to plain
                this.appearanceMode = Item.PLAIN;

                // size or appearance changed
                invalidate();
            } 
        } // synchronized
    }

    /**
     * Determine if Form should not traverse to this ImageItem
     *
     * @return true if Form should not traverse to this ImageItem
     */
    boolean shouldSkipTraverse() {
        if ((label == null || label.length() == 0) && (img == null)) {
            return true;
        }

        return false;
    }

    // private

    /**
     * Set the Image for this ImageItem
     *
     * @param img The image to use for this ImageItem
     */
    private void setImageImpl(Image img) {
        if (img != null && img.isMutable()) {
            this.mutImg = img;
            this.img = Image.createImage(img); // use immutable copy of img
        } else { 
            this.mutImg = null;
            this.img = img;
        }
    }

    /**
     * Set the alternate text for this ImageItem
     *
     * @param text The alternate text for this ImageItem
     */
    private void setAltTextImpl(String text) {
        this.altText = text;
    }

    /**
     * Draw Top and Bottom Border for showing Hyperlink ImageItem
     *
     * @param g The Graphics context to paint to
     * @param start start x co-ordinate of ImageItem
     * @param end end x co-ordinate of ImageItem
     * @param y1  y co-ordinate of Top ImageItem
     * @param y2  y co-ordinate of Bottom ImageItem
     */
    private void drawTop_BottomBorder(Graphics g, int start, int end,
                                      int y1, int y2) {

        for (int x = start; x < end; x += HYPERLINK_IMG.getWidth()) {
            // draw the top  border
            g.drawImage(HYPERLINK_IMG, x, y1,
                        Graphics.TOP | Graphics.LEFT);
            // draw the bottom border
            g.drawImage(HYPERLINK_IMG, x, y2,
                        Graphics.TOP | Graphics.LEFT);
        }
    }

    /**
     * Draw Left and Right Border for showing Hyperlink ImageItem
     *
     * @param g The Graphics context to paint to
     * @param start start y co-ordinate of ImageItem
     * @param end end y co-ordinate of ImageItem
     * @param x1  x co-ordinate of Left ImageItem
     * @param x2  x co-ordinate of Right ImageItem
     */
    private void drawLeft_RightBorder(Graphics g, int start, int end, 
                                      int x1, int x2) {

        for (int y = start; y < end; y += VERTICAL_HYPERLINK_IMG.getHeight()) {
            // draw the left border
            g.drawImage(VERTICAL_HYPERLINK_IMG, x1,        y,
                        Graphics.TOP | Graphics.LEFT);
            // draw the right border
            g.drawImage(VERTICAL_HYPERLINK_IMG, x2, y,
                        Graphics.TOP | Graphics.LEFT);
        }
    }

    /**
     * The snapshot of the Image of this ImageItem;
     * If the Image of this ImageItem was set to a mutable Image
     * this variable is updated with a new snapshot each time setImage() is
     * called. 
     */
    private Image img;

    /**
     * If the ImageItem was created with a mutable image or its Image
     * was set to a mutable image, that mutable image is stored in 
     * the mutImg variable so that ImageItem.getImage() could return it.
     */
    private Image mutImg;

    /**
     * The alternate text of this ImageItem
     */
    private String altText;

    /**
     * Vertical padding used for this ImageItem so that it is padded to 
     * occupy whole number of lines.
     */
    private int vertPad;

    /**
     * The appearance hint
     */
    private int appearanceMode;

    /** The original appearance hint before switching */
    private int originalAppearanceMode;

    /** The hyperlink pad used between Hyperlink Border and ImageItem */
    private final static int       HYPERLINK_PAD = 3;

    /** The hyperlink Image to display with the Hyperlink */
    private final static Image     HYPERLINK_IMG;

    /** The Vertical hyperlink Image to display with the Hyperlink */
    private final static Image     VERTICAL_HYPERLINK_IMG;

    static {
        /*
         * Initialize the icons necessary for hyper links.
         */
        VERTICAL_HYPERLINK_IMG = ImmutableImage.createIcon("link_vertical.png");
        HYPERLINK_IMG = ImmutableImage.createIcon("link_horizontal.png");
        com.sun.midp.lcdui.Text.HYPERLINK_IMG = HYPERLINK_IMG;
    }
}
