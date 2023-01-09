/*
 * @(#)StringItem.java	1.110 02/10/09 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.Text;

/**
 * An item that can contain a string. A <code>StringItem</code> is
 * display-only; the user
 * cannot edit the contents. Both the label and the textual content of a
 * <code>StringItem</code> may be modified by the application. The
 * visual representation
 * of the label may differ from that of the textual contents.
 */
public class StringItem extends Item {

    /**
     * Creates a new <code>StringItem</code> object.  Calling this
     * constructor is equivalent to calling
     * 
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     StringItem(label, text, PLAIN);     </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * @param label the <code>Item</code> label
     * @param text the text contents
     * @see #StringItem(String, String, int)
     */
    public StringItem(String label, String text) {
        this(label, text, Item.PLAIN);
    }

    /**
     * Creates a new <code>StringItem</code> object with the given label,
     * textual content, and appearance mode.
     * Either label or text may be present or <code>null</code>.
     *
     * <p>The <code>appearanceMode</code> parameter
     * (see <a href="Item.html#appearance">Appearance Modes</a>)
     * is a hint to the platform of the application's intended use
     * for this <code>StringItem</code>.  To provide hyperlink- or
     * button-like behavior,
     * the application should associate a default <code>Command</code> with this
     * <code>StringItem</code> and add an
     * <code>ItemCommandListener</code> to this
     * <code>StringItem</code>.
     * 
     * <p>Here is an example showing the use of a
     * <code>StringItem</code> as a button: </p>
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     StringItem strItem = 
     *         new StringItem("Default: ", "Set",     
     *                        Item.BUTTON);    
     *     strItem.setDefaultCommand(
     *         new Command("Set", Command.ITEM, 1);    
     *     // icl is ItemCommandListener 
     *     strItem.setItemCommandListener(icl);     </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * @param label the <code>StringItem's</code> label, or <code>null</code>
     * if no label
     * @param text the <code>StringItem's</code> text contents, or
     * <code>null</code> if the contents are initially empty
     * @param appearanceMode the appearance mode of the <code>StringItem</code>,
     * one of {@link #PLAIN}, {@link #HYPERLINK}, or {@link #BUTTON}
     * @throws IllegalArgumentException if <code>appearanceMode</code> invalid
     * 
     * @since MIDP 2.0
     */
    public StringItem(java.lang.String label,
                      java.lang.String text,
                      int appearanceMode) {
        super(label);

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

            this.str = text;
            this.font = Screen.CONTENT_FONT;

            int labelFontHeight = LABEL_FONT.getHeight();
            minimumLineHeight = Screen.CONTENT_HEIGHT;

            if (minimumLineHeight < labelFontHeight) {
                minimumLineHeight = labelFontHeight;
            }

            checkTraverse();
        }
    }

    /**
     * Gets the text contents of the <code>StringItem</code>, or
     * <code>null</code> if the <code>StringItem</code> is
     * empty.
     * @return a string with the content of the item
     * @see #setText
     */
    public String getText() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return str;
    }

    /**
     * Sets the text contents of the <code>StringItem</code>. If text
     * is <code>null</code>,
     * the <code>StringItem</code>
     * is set to be empty.
     * @param text the new content
     * @see #getText
     */
    public void setText(String text) {
        synchronized (Display.LCDUILock) {
            this.str = text;
            checkTraverse();
            invalidate();
        }
    }

    /** 
     * Returns the appearance mode of the <code>StringItem</code>.
     * See <a href="Item.html#appearance">Appearance Modes</a>.
     *
     * @return the appearance mode value,
     * one of {@link #PLAIN}, {@link #HYPERLINK}, or {@link #BUTTON}
     * 
     * @since MIDP 2.0
     */
    public int getAppearanceMode() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return appearanceMode;
    }

    /**
     * Sets the application's preferred font for
     * rendering this <code>StringItem</code>.
     * The font is a hint, and the implementation may disregard
     * the application's preferred font.
     *
     * <p> The <code>font</code> parameter must be a valid <code>Font</code>
     * object or <code>null</code>. If the <code>font</code> parameter is
     * <code>null</code>, the implementation must use its default font
     * to render the <code>StringItem</code>.</p>
     *
     * @param font the preferred font to use to render this
     *             <code>StringItem</code>
     * @see #getFont
     * @since MIDP 2.0
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Gets the application's preferred font for
     * rendering this <code>StringItem</code>. The
     * value returned is the font that had been set by the application,
     * even if that value had been disregarded by the implementation.
     * If no font had been set by the application, or if the application
     * explicitly set the font to <code>null</code>, the value is the default
     * font chosen by the implementation.
     *
     * @return the preferred font to use to render this
     *         <code>StringItem</code>
     * @see #setFont
     * @since MIDP 2.0
     */
    public Font getFont() {
        // SYNC NOTE: return of atomic value, no locking necessary
            return font;
    }

    /**
     * Sets the preferred width and height for this <code>Item</code>.
     * Values for width and height less than <code>-1</code> are illegal.
     * If the width is between zero and the minimum width, inclusive,
     * the minimum width is used instead.
     * If the height is between zero and the minimum height, inclusive,
     * the minimum height is used instead.
     *
     * <p>Supplying a width or height value greater than the minimum width or
     * height <em>locks</em> that dimension to the supplied
     * value.  The implementation may silently enforce a maximum dimension for
     * an <code>Item</code> based on factors such as the screen size.
     * Supplying a value of
     * <code>-1</code> for the width or height unlocks that dimension.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.</p>
     *
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within  an <code>Alert</code>.</p>
     *
     * @param width the value to which the width should be locked, or
     * <code>-1</code> to unlock
     * @param height the value to which the height should be locked, or
     * <code>-1</code> to unlock
     * @throws IllegalArgumentException if width or height is less than
     * <code>-1</code>
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     * @see #getPreferredHeight
     * @see #getPreferredWidth
     * @since MIDP 2.0
     */
    public void setPreferredSize(int width, int height) {
        super.setPreferredSize(width, height);
    }

    // package private

    /**
     * Get the minimum width of this Item
     *
     * @return the minimum width
     */
    int callMinimumWidth() {
        int pW = callPreferredWidth(-1);

        // FIX ME: paint() does not know to paint in a width which is less
        // than preferred
        int w = font.charWidth('W') * 8 + 6 * font.charWidth('.');
        if (w > pW) {
            w = pW;
        }

        if (isButton()) {
            w += 2 * (BUTTON_BORDER + BUTTON_PAD);
        }
        return w;
    }

    /**
     * Get the preferred width of this Item
     *
     * @param h the tentative content height in pixels, or -1 if a
     * tentative height has not been computed
     * @return the preferred width
     */
    int callPreferredWidth(int h) {

        // FIX ME: we ignore the 'h' value and just return
        // a basic width based on our contents. That is, this
        // StringItem's preferred width is always based on
        // being one line high
        
        if (isButton()) {
            int buttonPad = 2 * (BUTTON_BORDER + BUTTON_PAD);
            int prefW     = Text.getTwoStringsWidth(label, str, LABEL_FONT, 
                                                    font, 
                                                    DEFAULT_WIDTH - buttonPad,
                                                    LABEL_PAD);
            return (prefW > 0 ? prefW + buttonPad : 0);
        }

        return Text.getTwoStringsWidth(label, str, LABEL_FONT, font, 
                                       DEFAULT_WIDTH, LABEL_PAD);
    }

    /**
     * Get the minimum height of this Item
     *
     * @return the minimum height
     */
    int callMinimumHeight() {
        // IF we decide to have minHeight as one line
        // we need to change the way painting is done 
        // (to divide allocated space between label and string)
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
        if (w == -1) {
            w = DEFAULT_WIDTH;
        }

        if (isButton()) {
            int buttonPad = 2 * (BUTTON_BORDER + BUTTON_PAD);
            int prefH     = Text.getTwoStringsHeight(label, str, 
                                                     LABEL_FONT, font,
                                                     w - buttonPad, 
                                                     LABEL_PAD);
            return (prefH > 0 ? prefH + buttonPad : 0);
        }

        return Text.getTwoStringsHeight(label, str, LABEL_FONT, font,
                                        w, LABEL_PAD);
    }

    /**
     * Determine if this Item should have a newline before it
     *
     * @return true if it should have a newline before
     */
    boolean equateNLB() {

        // If label starts with a\n,
        // put this StringItem on a newline no matter what
        if (label != null && label.length() > 0) {
            if (label.charAt(0) == '\n') {
                return true;
            }
        } else if (str != null && str.length() > 0) {
            // If there is no label and our content starts with a \n,
            // this StringItem starts on a newline
            if (str.charAt(0) == '\n') {
                return true;
            }
        } else {
            // empty StringItem
            return false;
        }

        if ((layout & Item.LAYOUT_2) == Item.LAYOUT_2) {
            return ((layout & Item.LAYOUT_NEWLINE_BEFORE)
                            == Item.LAYOUT_NEWLINE_BEFORE);
        }
            
        // in MIDP1.0 new any StringItem with a non-null label would
        // go on a new line
        return label != null && label.length() > 0;
    }

    /**
     * Determine if this Item should have a newline after it
     *
     * @return true if it should have a newline after
     */
    boolean equateNLA() {

        // If content ends with a \n,
        // there is a newline after this StringItem no matter what
        if (str != null && str.length() > 0) {
            if (str.charAt(str.length() - 1) == '\n') {
                return true;
            }
        } else if (label != null && label.length() > 0) {
            // If there is no content and our label ends with a \n, 
            // there is a newline after this StringItem
            if (label.charAt(label.length() - 1) == '\n') {
                return true;
            }
        } else {
            // empty StringItem
            return false;
        }

        if ((layout & Item.LAYOUT_2) == Item.LAYOUT_2) {
            return ((layout & Item.LAYOUT_NEWLINE_AFTER)
                            == Item.LAYOUT_NEWLINE_AFTER);
        }
        return false;
    }

    /**
     * Determine if Form should not traverse to this StringItem
     *
     * @return true if Form should not traverse to this StringItem
     */
    boolean shouldSkipTraverse() {
        if ((label == null || label.length() == 0) && 
            (str == null || str.length() == 0)) {
            return true;
        }

        return skipTraverse;
    }

    /**
     * Paint this StringItem
     *
     * @param g the Graphics object to paint to
     * @param width the width of this item
     * @param height the height of this item
     */
    void callPaint(Graphics g, int width, int height) {

        int w = width;
        int h = height;
        
        int translateY = 0;
        int translateX = 0;

        if (isButton()) {
            translateX = translateY = BUTTON_BORDER + BUTTON_PAD;
            w -= 2*translateX;
            h -= 2*translateY;
            g.translate(translateX, translateY);
        }

        Font lFont = (LABEL_BOLD_ON_TRAVERSE && !hasFocus) ?
                     Screen.CONTENT_FONT : LABEL_FONT;

        int labelHeight = getLabelHeight(w);

        int offset = Text.paint(label, lFont, 
                                g, w, labelHeight, 0, Text.NORMAL, null);
        if (offset > 0) {
            offset += LABEL_PAD;
        }

        int mode = Text.NORMAL;
        if (numCommands > 0 && appearanceMode == Item.HYPERLINK) { 
            mode = hasFocus ? (Text.INVERT | Text.HYPERLINK) :
                              (Text.NORMAL | Text.HYPERLINK);
        }

        int yOffset = labelHeight;
        if (labelHeight > 0) {
            yOffset -= (lFont.getHeight() < font.getHeight() ?
                        lFont.getHeight() : font.getHeight());
        }

        g.translate(0, yOffset);
        translateY += yOffset;

        Text.paint(str, font, g, w, h - yOffset, offset, mode, null);

        g.translate(-translateX, -translateY);

        if (isButton()) {
            drawButtonBorder(g, 0, 0, width, height, hasFocus);
        }
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

        // StringItem takes focus only if there are one or more Item Commands
        // attached to it
        if (!(getCommandCount() > 0) || commandListener == null) {
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
     *  Adds a context sensitive Command to the item.
     * @param cmd the command to be removed
     */
    void addCommandImpl(Command cmd) {
        synchronized (Display.LCDUILock) {
            // The super class will update the command set of the Form
            super.addCommandImpl(cmd);

            if ((numCommands >= 1) && (appearanceMode == Item.PLAIN)) {
                // restore the value of the original appearanceMode
                // if it is a button
                // otherwise simple change the appearanceMode
                // to hyperlink
                this.appearanceMode =
                    originalAppearanceMode == Item.BUTTON ?
                    Item.BUTTON : Item.HYPERLINK;
            }

            checkTraverse();
            invalidate();
        } // synchronized
    }

    /**
     *  Removes the context sensitive command from item.
     * @param cmd the command to be removed
     */
    void removeCommandImpl(Command cmd) {
        synchronized (Display.LCDUILock) {
            // The super class will update the command set of the Form
            super.removeCommandImpl(cmd);

            if ((numCommands < 1) && (appearanceMode != Item.PLAIN)) {
                // store value of the original appearanceMode
                originalAppearanceMode = this.appearanceMode;
                // change the appearanceMode to plain
                this.appearanceMode = Item.PLAIN;
            }

            checkTraverse();
            invalidate();
        } // synchronized
    }

    /**
     * Check that given the label, text, and commands, Form
     * should traverse this StringItem. Updates the internal
     * 'skipTraverse' variable.
     */
    private void checkTraverse() {
        if (str == null && label == null) {
            skipTraverse = true;
        } else if (str == null && label.trim().equals("")) {
            skipTraverse = true;
        } else if (label == null && str.trim().equals("")) {
            skipTraverse = true;
        } else {
            skipTraverse = false;
        }
    }

    /**
     * Determines if this StringItem should be rendered with the button border.
     * @return true if the StringItem should be rendered with 
     *         the button border, false - otherwise
     */
    private boolean isButton() {
        return (numCommands > 0 && appearanceMode == Item.BUTTON);
    }

    /** The text of this StringItem */
    private String str;

    /** The Font to render this StringItem's text in */
    private Font font;

    /** The appearance hint */
    private int appearanceMode;

    /** The original appearance hint before switching */
    private int originalAppearanceMode;

    /** Minimum height for one line */
    private int minimumLineHeight;

    /**
     * An internal flag. True if Form should not traverse
     * to this StringItem
     */
    private boolean skipTraverse;
  
}
