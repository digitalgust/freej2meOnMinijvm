/*
 * @(#)Displayable.java	1.136 02/10/14 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

import java.util.TimerTask;
import java.util.Timer;

import com.sun.midp.lcdui.Text;

/**
 * An object that has the capability of being placed on the display.  A 
 * <code>Displayable</code> object may have a title, a ticker,
 * zero or more commands and a listener associated with it.  The
 * contents displayed and their interaction with the user are defined by 
 * subclasses.
 *
 * <p>The title string may contain
 * <A HREF="Form.html#linebreak">line breaks</a>.
 * The display of the title string must break accordingly.
 * For example, if only a single line is available for a
 * title and the string contains a line break then only the characters
 * up to the line break are displayed.</p>
 *
 * <p>Unless otherwise specified by a subclass, the default state of newly 
 * created <code>Displayable</code> objects is as follows:</p>
 *
 * <ul>
 * <li>it is not visible on the <code>Display</code>;</li>
 * <li>there is no <code>Ticker</code> associated with this
 * <code>Displayable</code>;</li>
 * <li>the title is <code>null</code>;</li>
 * <li>there are no <code>Commands</code> present; and</li>
 * <li>there is no <code>CommandListener</code> present.</li>
 * </ul>
 *
 * @since MIDP 1.0
 */

abstract public class Displayable {

// ************************************************************
//  public member variables
// ************************************************************

// ************************************************************
//  protected member variables
// ************************************************************

// ************************************************************
//  package private member variables
// ************************************************************

    /** The current Display object */
    Display currentDisplay;

    /** An array of Commands added to this Displayable */
    Command commands[];

    /** The number of Commands added to this Displayable */
    int numCommands;

    /** The CommandListener for Commands added to this Displayable */
    CommandListener listener;

    /** Used as an index into the viewport[], for the x origin */
    final static int X      = 0;

    /** Used as an index into the viewport[], for the y origin */
    final static int Y      = 1;

    /** Used as an index into the viewport[], for the width */
    final static int WIDTH  = 2;

    /** Used as an index into the viewport[], for the height */
    final static int HEIGHT = 3;

    /**
     * The viewport coordinates.
     * Index 0: x origin coordinate (in the Display's coordinate space)
     * Index 1: y origin coordinate (in the DIsplay's coordinate space)
     * Index 2: width
     * Index 3: height
     */
    int[] viewport;

    /** True, if this Displayable is in full screen mode */
    boolean fullScreenMode;

    /**
     * True, indicates that before being painted, this Displayable should
     * be notified that its size has changed via callSizeChanged()
     */
    boolean sizeChangeOccurred;

    /**
     * In some circumstances (such as List), we need to delegate the
     * paint ownership. For example: List uses an internal Form to
     * do its rendering. If the Form requests a repaint, it will get
     * denied because the Form is not actually current - the List is.
     * So, we introduce the delegate, so the List can set the delegate
     * to itself, and its internal Form can schedule repaints.
     */
    Displayable paintDelegate;

// ************************************************************
//  private member variables
// ************************************************************

    /** Special title font */
    private final static  Font TITLE_FONT =
        Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);

    /** Special title height */
    private final static int TITLE_HEIGHT = TITLE_FONT.getHeight() + 1;

    /** The title for this Displayable */
    private String title;

    /** The ticker that may be set for this Displayable */
    private Ticker ticker;

    /** A Timer which will handle firing repaints of the TickerPainter */
    private final static Timer tickerTimer;

    /** A TimerTask which will repaint the Ticker on a repeated basis */
    private TickerPainter tickerPainter;

    /** Convenience int to avoid garbage during repainting */
    private int tickerHeight;

    /** Convenience int to avoid garbage during repainting */
    private int totalHeight;

    /** The vertical scroll position */
    private int vScrollPosition     = 0;

    /** The vertical scroll proportion */
    private int vScrollProportion   = 100;


// ************************************************************
//  Static initializer, constructor
// ************************************************************

    static {
    	tickerTimer = new Timer();
    }

    /**
     * Create a new Displayable
     */
    Displayable() {
        setupViewport();
        translateViewport();
        paintDelegate = this;
    }

// ************************************************************
//  public methods
// ************************************************************

    /**
     * Gets the title of the <code>Displayable</code>. Returns
     * <code>null</code> if there is no title.
     * @return the title of the instance, or <code>null</code> if no title
     * @since MIDP 2.0
     * @see #setTitle
     */
    public String getTitle() {
        // SYNC NOTE: return of atomic value, no lock necessary
        return title;
    }

    /**
     * Sets the title of the <code>Displayable</code>. If
     * <code>null</code> is given,
     * removes the title. 
     *
     * <P>If the <code>Displayable</code> is actually visible on
     * the display,
     * the implementation should update 
     * the display as soon as it is feasible to do so.</P>
     * 
     * <P>The existence of a title  may affect the size
     * of the area available for <code>Displayable</code> content. 
     * Addition, removal, or the setting of the title text at runtime
     * may dynamically change the size of the content area.
     * This is most important to be aware of when using the
     * <code>Canvas</code> class.
     * If the available area does change, the application will be notified
     * via a call to {@link #sizeChanged(int, int) sizeChanged()}. </p>
     *
     * @param s the new title, or <code>null</code> for no title
     * @since MIDP 2.0
     * @see #getTitle
     */
    public void setTitle(String s) {
        synchronized (Display.LCDUILock) {
            setTitleImpl(s);
        }
    }

    /**
     * Gets the ticker used by this <code>Displayable</code>.
     * @return ticker object used, or <code>null</code> if no
     * ticker is present
     * @since MIDP 2.0
     * @see #setTicker
     */
    public Ticker getTicker() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return ticker;
    }

    /**
     * Sets a ticker for use with this <code>Displayable</code>,
     * replacing any
     * previous ticker.
     * If <code>null</code>, removes the ticker object
     * from this <code>Displayable</code>. The same ticker may be shared by 
     * several <code>Displayable</code>
     * objects within an application. This is done by calling
     * <code>setTicker()</code>
     * with the same <code>Ticker</code> object on several
     * different <code>Displayable</code> objects.
     * If the <code>Displayable</code> is actually visible on the display,
     * the implementation should update 
     * the display as soon as it is feasible to do so.
     * 
     * <p>The existence of a ticker may affect the size
     * of the area available for <code>Displayable's</code> contents. 
     * Addition, removal, or the setting of the ticker at runtime
     * may dynamically change the size of the content area.
     * This is most important to be aware of when using the
     * <code>Canvas</code> class.
     * If the available area does change, the application will be notified
     * via a call to {@link #sizeChanged(int, int) sizeChanged()}. </p>
     *
     * @param ticker the ticker object used on this screen
     * @since MIDP 2.0
     * @see #getTicker
     */
    public void setTicker(Ticker ticker) {
        synchronized (Display.LCDUILock) {
            setTickerImpl(ticker);
        }
    }

    /**
     * Checks if the <code>Displayable</code> is actually visible
     * on the display.  In order
     * for a <code>Displayable</code> to be visible, all of the
     * following must be true:
     * the <code>Display's</code> <code>MIDlet</code> must be
     * running in the foreground, the <code>Displayable</code>
     * must be the <code>Display's</code> current screen, and the
     * <code>Displayable</code> must not be
     * obscured by a <a href="Display.html#systemscreens">
     * system screen</a>.
     *
     * @return <code>true</code> if the
     * <code>Displayable</code> is currently visible
     */
    public boolean isShown() {
        synchronized (Display.LCDUILock) {
            return (currentDisplay == null) ?
                false : currentDisplay.isShown(this);
        }
    }

    /**
     * Adds a command to the <code>Displayable</code>. The
     * implementation may choose,
     * for example,
     * to add the command to any of the available soft buttons or place it 
     * in a menu.
     * If the added command is already in the screen (tested by comparing the
     * object references), the method has no effect.
     * If the <code>Displayable</code> is actually visible on the
     * display, and this call
     * affects the set of visible commands, the implementation should update 
     * the display as soon as it is feasible to do so.
     * 
     * @param cmd the command to be added
     *
     * @throws NullPointerException if <code>cmd</code> is
     * <code>null</code>
     */
    public void addCommand(Command cmd) {
        if (cmd == null) {
            throw new NullPointerException();
        }

        synchronized (Display.LCDUILock) {
            addCommandImpl(cmd);
        }
    }

    /**
     * Removes a command from the <code>Displayable</code>.
     * If the command is not in the <code>Displayable</code>
     * (tested by comparing the
     * object references), the method has no effect.
     * If the <code>Displayable</code> is actually visible on the
     * display, and this call
     * affects the set of visible commands, the implementation should update 
     * the display as soon as it is feasible to do so.
     * If <code>cmd</code> is <code>null</code>, this method
     * does nothing.
     * 
     * @param cmd the command to be removed
     */
    public void removeCommand(Command cmd) {
        synchronized (Display.LCDUILock) {
            removeCommandImpl(cmd);
        }
    }

    /**
     * Sets a listener for {@link Command Commands} to this
     * <code>Displayable</code>,
     * replacing any previous <code>CommandListener</code>. A
     * <code>null</code> reference is
     * allowed and has the effect of removing any existing listener.
     *
     * @param l the new listener, or <code>null</code>.
     */
    public void setCommandListener(CommandListener l) {
        synchronized (Display.LCDUILock) {
            listener = l;
        }
    }

    /**
     * Gets the width in pixels of the displayable area available to the 
     * application.  The value returned is appropriate for the particular 
     * <code>Displayable</code> subclass.  This value may depend
     * on how the device uses the
     * display and may be affected by the presence of a title, a ticker, or 
     * commands.
     * This method returns the proper result at all times, even if the
     * <code>Displayable</code> object has not yet been shown.
     * 
     * @return width of the area available to the application
     * @since MIDP 2.0
     */
    public int getWidth() {
        // SYNC NOTE: return of atomic value
        return viewport[WIDTH];
    }

    /**
     * Gets the height in pixels of the displayable area available to the 
     * application.  The value returned is appropriate for the particular 
     * <code>Displayable</code> subclass.  This value may depend
     * on how the device uses the
     * display and may be affected by the presence of a title, a ticker, or 
     * commands.
     * This method returns the proper result at all times, even if the
     * <code>Displayable</code> object has not yet been shown.
     * 
     * @return height of the area available to the application
     * @since MIDP 2.0
     */
    public int getHeight() {
        // SYNC NOTE: return of atomic value
        return viewport[HEIGHT];
    }

// ************************************************************
//  protected methods
// ************************************************************

    /**
     * The implementation calls this method when the available area of the
     * <code>Displayable</code> has been changed. 
     * The &quot;available area&quot; is the area of the display that
     * may be occupied by
     * the application's contents, such as <code>Items</code> in a
     * <code>Form</code> or graphics within
     * a <code>Canvas</code>.  It does not include space occupied
     * by a title, a ticker,
     * command labels, scroll bars, system status area, etc.  A size change
     * can occur as a result of the addition, removal, or changed contents of 
     * any of these display features.
     *
     * <p> This method is called at least once before the
     * <code>Displayable</code> is shown for the first time.
     * If the size of a <code>Displayable</code> changes while
     * it is visible,
     * <CODE>sizeChanged</CODE> will be called.  If the size of a
     * <code>Displayable</code>
     * changes while it is <em>not</em> visible, calls to
     * <CODE>sizeChanged</CODE> may be deferred.  If the size had changed
     * while the <code>Displayable</code> was not visible,
     * <CODE>sizeChanged</CODE> will be
     * called at least once at the time the
     * <code>Displayable</code> becomes visible once
     * again.</p>
     *
     * <p>The default implementation of this method in <code>Displayable</code>
     * and its
     * subclasses defined in this specification must be empty.
     * This method is intended solely for being overridden by the
     * application. This method is defined on <code>Displayable</code>
     * even though applications are prohibited from creating 
     * direct subclasses of <code>Displayable</code>.
     * It is defined here so that applications can override it in
     * subclasses of <code>Canvas</code> and <code>Form</code>.
     * This is useful for <code>Canvas</code> subclasses to tailor
     * their graphics and for <code>Forms</code> to modify
     * <code>Item</code> sizes and layout
     * directives in order to fit their contents within the the available
     * display area.</p>
     * 
     * @param w the new width in pixels of the available area
     * @param h the new height in pixels of the available area
     * @since MIDP 2.0
     */ 
    protected void sizeChanged(int w, int h) {
	// this method is intended to be overridden by the application
    }

// ************************************************************
//  package private methods
// ************************************************************

    /**
     * Called to commit any pending user interaction
     */
    void commitPendingInteraction() { }

    /**
     * Called to schedule an "invalidate" for this Displayable. Invalidation
     * is caused by things like size changes, content changes, or spontaneous
     * traversal within the Item
     *
     * @param src The Item who is causing the invalidation. If NULL,
     *            all contents should be considered invalid.
     */
    void invalidate(Item src) {
        Display d = currentDisplay;
        if (d != null) {
            d.invalidate(src);
        }
    }

    /**
     * Called by the event handler to perform an
     * invalidation of this Displayable
     *
     * @param src The Item who is causing the invalidation. If NULL,
     *            all contents should be considered invalid.
     */
    void callInvalidate(Item src) {
    }

    /**
     * Called to schedule a call to itemStateChanged() due to
     * a change in the given Item.
     *
     * @param src the Item which has changed
     */
    void itemStateChanged(Item src) {
        Display d = currentDisplay;
        if (d != null) {
            d.itemStateChanged(src);
        }
    }

    /**
     * Called by the event handler to notify any ItemStateListener
     * of a change in the given Item
     *
     * @param src The Item which has changed
     */
    void callItemStateChanged(Item src) {
    }

    /**
     * Set the ticker for this Displayable
     *
     * @param t the ticker to set
     */
    void setTickerImpl(Ticker t) {
        // Return early if there's nothing to do
        if (this.ticker == t) {
            return;
        }

        Ticker oldTicker = this.ticker;
        this.ticker = t;

        // CASES:
        // 1. Had an invisible non-null ticker, setting a null ticker
        //    - We need to set the new ticker. There's no need to re-layout
        //      or start the new ticker
        // 2. Had an invisible non-null ticker, setting a non-null ticker
        //    - We need to set the new ticker. There's no need to re-layout
        //      or start the new ticker
        // 3. Had a visible non-null ticker, setting a null ticker
        //    - We need to set the new ticker and re-layout. There's no
        //      need to start the new ticker.
        // 4. Had a null ticker, setting a visible non-null ticker
        //    - We need to set the new ticker, re-layout, and
        //      start up the new ticker
        // 5. Had a visible non-null ticker, setting a non-null ticker
        //    - We need to set the new ticker. There's no need to re-layout

        boolean sizeChange =
            ((oldTicker != null) && (ticker == null)) ||
            ((oldTicker == null) && (ticker != null));

        if (sizeChange) {
            if (ticker != null) {
                ticker.reset();
                startTicker();
            } else {
                stopTicker();
            }
            layout();
            callSizeChanged(viewport[WIDTH], viewport[HEIGHT]);
            callRepaint();
        } else {
            ticker.reset();
        }
    }

    /**
     * Package private unsynchronized version of setTitle(String)
     *
     * @param s Title to set on this Displayable.
     */
    void setTitleImpl(String s) {
        if (title == s || (title != null && title.equals(s))) {
            return;
        }

        String oldTitle = this.title;
        this.title = s;

        if (fullScreenMode) {
            return;
        }

        boolean sizeChange =
            ((oldTitle != null) && (title == null)) ||
            ((oldTitle == null) && (title != null));

        if (sizeChange) {
            layout();
            callSizeChanged(viewport[WIDTH], viewport[HEIGHT]);
            callRepaint();
        } else {
            repaintTitle();
        }
    }

    /**
     * Package private equivalent of sizeChanged()
     *
     * @param w the new width
     * @param h the new height
     *
     */
    void callSizeChanged(int w, int h) {
        // If there is no Display, or if this Displayable is not
        // currently visible, we simply record the fact that the
        // size has changed
        sizeChangeOccurred =
            (currentDisplay == null) || (!currentDisplay.isShown(this));
    }

    /**
     * Display calls this method on it's current Displayable.
     * Displayable uses this oppportunity to do necessary stuff
     * on the Graphics context, this includes,
     * paint Ticker, paint Title, translate as necessary.
     *
     * <p>The target Object of this repaint may be some Object
     * initially set by this Displayable when the repaint was
     * requested - allowing this Displayable to know exactly
     * which Object it needs to call to service this repaint,
     * rather than potentially querying all of its Objects to
     * determine the one(s) which need painting.
     *
     * SYNC NOTE: The caller of this method handles synchronization.
     *
     * @param g the graphics context to paint into.
     * @param target the target Object of this repaint
     */
    void callPaint(Graphics g, Object target) {
        /*
        System.err.println("Displayable:Clip: " +
            g.getClipX() + "," + g.getClipY() + "," +
            g.getClipWidth() + "," + g.getClipHeight());
        */

        synchronized (Display.LCDUILock) {

            if (!(fullScreenMode || (title == null && ticker == null))) {

                if (g.getClipY() < totalHeight) {
                    // We always paint "something" for the ticker, rather
                    // than make the title paint more complicated. If there
                    // is no ticker, we draw the darkgray/white saparator line
                    if (g.getClipY() < tickerHeight) {
                        paintTicker(g);
                    }

                    if (title != null) {
                        if (g.getClipY() + g.getClipHeight() >
                                totalHeight - tickerHeight + 1) {

                            g.translate(0, tickerHeight);
                            paintTitle(g);
                            g.translate(0, -tickerHeight);
                        }
                    }
                }
            } else {
                g.setColor(Item.DARK_GRAY_COLOR);
                g.drawLine(0, 0, Display.WIDTH, 0);
                g.setColor(Display.FG_COLOR);
            }


        } // synchronized
    }

    /**
     * Paint the ticker if it exists, on this graphics object.
     *
     * @param g The Graphics object to paint this ticker on.
     */
    void paintTicker(Graphics g) {
        // paint the ticker here.
        if (ticker != null) {
            ticker.paintContent(g);
        } else if (title != null) {
            g.setColor(Item.DARK_GRAY_COLOR);
            g.drawLine(0, 0, Display.WIDTH, 0);
            g.setColor(Display.ERASE_COLOR);
            g.drawLine(0, 1, Display.WIDTH, 1);
            g.setColor(Display.FG_COLOR);
        }
    }

    /**
     * Paints the title of this Displayable, including it's border.
     * The graphics context is then translated by the height occupied
     * by the title area.
     *
     * @param g The graphics object to paint this title on.
     *
     */
    void paintTitle(Graphics g) {

        g.setColor(Item.LIGHT_GRAY_COLOR);
        g.fillRect(0, 0, Display.WIDTH, TITLE_HEIGHT - 1);
        g.setColor(Display.FG_COLOR);

        Text.paint(title, TITLE_FONT, g, Display.WIDTH,
                   TITLE_HEIGHT, 1, Text.NORMAL, null);

        g.setColor(Item.DARK_GRAY_COLOR);
        g.drawLine(0, TITLE_HEIGHT - 1, Display.WIDTH, TITLE_HEIGHT - 1);
        g.setColor(Display.FG_COLOR);
    }

    /**
     * Perform any necessary layout, and update the viewport
     * as necessary
     */
    void layout() {
        setupViewport();
        translateViewport();
    }

    /**
     * Set the full screen mode of this Displayable. If true,
     * this Displayable will take up as much screen real estate
     * as possible
     *
     * @param onOff true if full screen mode should be turned on
     */
    void fullScreenMode(boolean onOff) {
        if (fullScreenMode == onOff) {
            return;
        }

        fullScreenMode = onOff;

        layout();
        updateCommandSet();
        callSizeChanged(viewport[WIDTH], viewport[HEIGHT]);

        callRepaint();

        if (fullScreenMode) {
            stopTicker();
        } else {
            startTicker();
        }
    }

    /**
     * By default, the viewport array is configured to be
     * at origin 0,0 with width Display.WIDTH and height
     * either Display.HEIGHT or Display.ADORNEDHEIGHT, depending
     * on full screen mode.
     */
    private void setupViewport() {
        // setup the default viewport, the size of the Display
        if (viewport == null) {
            viewport = new int[4];
        }

        viewport[X] = 
        viewport[Y] = 0;

        viewport[WIDTH]  = Display.WIDTH;
        viewport[HEIGHT] = (fullScreenMode) 
                         ? Display.HEIGHT 
                         : Display.ADORNEDHEIGHT;
    }

    /**
     * Translate the viewport for any decorations by this Displayable
     * such as a title or ticker
     */
    private void translateViewport() {

        if (!(fullScreenMode || (title == null && ticker == null))) {

            //
            // determine the right tickerHeight
            //
            if (ticker != null) {
                tickerHeight = Ticker.PREFERRED_HEIGHT;
            } else {
                if (title != null) {
                    tickerHeight = 2;
                } else {
                    tickerHeight = 0;
                }
            }
 
            //
            // add to any title height
            //
            totalHeight = (title != null)
                        ? TITLE_HEIGHT + tickerHeight
                        : tickerHeight;
        } else {
            //
            // in fullscreen, or with no title or ticker we have
            // a single dark line under the status bar
            //
            totalHeight = 1;
        }

        viewport[Y] += totalHeight;
        viewport[HEIGHT] -= totalHeight;
    }

    /**
     * Handle a key press
     *
     * @param keyCode The key that was pressed
     */
    void callKeyPressed(int keyCode) { }
    /**
     * Handle a repeated key press
     *
     * @param keyCode The key that was pressed
     */
    void callKeyRepeated(int keyCode) { }
    /**
     * Handle a key release
     *
     * @param keyCode The key that was released
     */
    void callKeyReleased(int keyCode) { }
    /**
     * Handle a key that was typed from the keyboard
     *
     * @param c The char that was typed
     */
    void callKeyTyped(char c) {}
    /**
     * Handle a pointer press event
     *
     * @param x The x coordinate of the press
     * @param y The y coordinate of the press
     */
    void callPointerPressed(int x, int y) { }
    /**
     * Handle a pointer drag event
     *
     * @param x The x coordinate of the drag
     * @param y The y coordinate of the drag
     */
    void callPointerDragged(int x, int y) { }
    /**
     * Handle a pointer release event
     *
     * @param x The x coordinate of the release
     * @param y The y coordinate of the release
     */
    void callPointerReleased(int x, int y) { }

    /**
     * Repaint this Displayable
     *
     * @param x The x coordinate of the region to repaint
     * @param y The y coordinate of the region to repaint
     * @param width The width of the region to repaint
     * @param height The height of the region to repaint
     * @param target an optional paint target to receive the paint request
     *               when it returns via callPaint()
     */
    final void callRepaint(int x, int y, int width, int height, Object target) {
        if (currentDisplay != null) {
            // Note: Display will not let anyone but the current
            // Displayable schedule repaints
            currentDisplay.repaintImpl(paintDelegate, x, y, width, height,
                                       target);
        }
    }

    /**
     * Repaints this Displayable. 
     * This is the same as calling 
     * callRepaint(0, 0, 
     *     viewport[X] + viewport[WIDTH], 
     *     viewport[Y] + viewport[HEIGHT], null)
     */
    final void callRepaint() {
        callRepaint(0, 0, 
                    viewport[X] + viewport[WIDTH],
                    viewport[Y] + viewport[HEIGHT], null);
    }

    /**
     * Repaint the viewport region of this Displayable
     */
    final void repaintContents() {
        callRepaint(viewport[X], viewport[Y],
            viewport[WIDTH], viewport[HEIGHT], null);
    }

    /**
     * Set the vertical scroll position and proportion
     *
     * @param scrollPosition The vertical scroll position to set on a
     *                       scale of 0-100
     * @param scrollProportion The vertical scroll proportion to set on
     *                         a scale of 0-100. For example, if the viewport
     *                         is 25 pixels high and the Displayable is 100
     *                         pixels high, then the scroll proportion would
     *                         be 25, since only 25% of the Displayable can
     *                         be viewed at any one time. This proportion
     *                         value can be used by implementations which
     *                         render scrollbars to indicate scrollability
     *                         to the user.
     */
    void setVerticalScroll(int scrollPosition, int scrollProportion) {
        synchronized (Display.LCDUILock) {
            this.vScrollPosition = scrollPosition;
            this.vScrollProportion = scrollProportion;

            if (currentDisplay != null) {
                currentDisplay.setVerticalScroll(scrollPosition,
                                                 scrollProportion);
            }
        }
    }

    /**
     * Get the current vertical scroll position
     *
     * @return int The vertical scroll position on a scale of 0-100
     */
    int getVerticalScrollPosition() {
        // SYNC NOTE: return of atomic value
        return vScrollPosition;
    }

    /**
     * Get the current vertical scroll proportion
     *
     * @return ing The vertical scroll proportion on a scale of 0-100
     */
    int getVerticalScrollProportion() {
        // SYNC NOTE: return of atomic value
        return vScrollProportion;
    }

    /**
     * Notify this Displayable it is being shown on the given Display
     *
     * @param d the Display showing this Displayable
     */
    void callShowNotify(Display d) {
        synchronized (Display.LCDUILock) {
            currentDisplay = d;

            // call grab full screen to let the native
            // layer know whether to be in 
            // fullscreen or normal mode.
            grabFullScreen(fullScreenMode);

            if (sizeChangeOccurred) {
                callSizeChanged(viewport[WIDTH], viewport[HEIGHT]);
            }
            // display the ticker if we have a visible one.
            startTicker();
        }
    }

    /**
     * Notify this Displayable it is being hidden on the given Display
     *
     * @param d the Display hiding this Displayable
     */
    void callHideNotify(Display d) {
        synchronized (Display.LCDUILock) {
            currentDisplay = null;
            stopTicker();
        }
    }

    /**
     * Get the set of Commands that have been added to this Displayable
     *
     * @return Command[] The array of Commands added to this Displayable
     */
    Command[] getCommands() {
        return commands;
    }

    /**
     * Get the number of commands that have been added to this Displayable
     *
     * @return int The number of commands that have been added to this
     *              Displayable
     */
    int getCommandCount() {
        return numCommands;
    }

    /**
     * Gets item currently in focus. This is will be only applicable to
     * Form. The rest of the subclasses will return null.
     * @return Item The item currently in focus in this Displayable;
     *          if there are no items in focus, null is returned
     */
    Item getCurrentItem() {
        return null;
    }

    /**
     * Get the CommandListener for this Displayable
     *
     * @return CommandListener The CommandListener listening to Commands on
     *                          this Displayable
     */
    CommandListener getCommandListener() {
        return listener;
    }

    /**
     * Add a Command to this Displayable
     *
     * @param cmd The Command to add to this Displayable
     */
    void addCommandImpl(Command cmd) {
        for (int i = 0; i < numCommands; ++i) {
            if (commands[i] == cmd) {
                return;
            }
        }

        if ((commands == null) || (numCommands == commands.length)) {
            Command[] newCommands = new Command[numCommands + 4];
            if (commands != null) {
                System.arraycopy(commands, 0, newCommands, 0, numCommands);
            }
            commands = newCommands;
        }

        commands[numCommands] = cmd;
        ++numCommands;
        updateCommandSet();
    }

    /**
     * Remove a Command from this Displayable
     *
     * @param cmd The Command to remove from this Displayable
     */
    void removeCommandImpl(Command cmd) {
        for (int i = 0; i < numCommands; ++i) {
            if (commands[i] == cmd) {
                commands[i] = commands[--numCommands];
                commands[numCommands] = null;
                updateCommandSet();
                break;
            }
        }
    }

    /**
     * Updates command set if this Displayable is visible
     */
    void updateCommandSet() {
        // SYNC NOTE: Display requires calls to updateCommandSet to
        // be synchronized
        synchronized (Display.LCDUILock) {
            if ((currentDisplay != null) && currentDisplay.isShown(this)) {
                currentDisplay.updateCommandSet();
            }
        }
    }

    /**
     * Decide if the given Command has been added to this
     * Displayable's set of abstract commands.
     *
     * @param command The Command to check. This value should
     *                never be null (no checks are made).
     * @return True if the Command has been previously added
     *         via the addCommand() method
     */
    boolean commandInSetImpl(Command command) {
        for (int i = 0; i < numCommands; i++) {
            if (commands[i] == command) {
                return true;
            }
        }
        return false;
    }

// ************************************************************
//  private methods
// ************************************************************

    /**
     * Repaint the title area.
     */
    private void repaintTitle() {
        if (currentDisplay != null) {
            currentDisplay.repaintImpl(paintDelegate, 0,
                (ticker != null) 
                    ? Ticker.PREFERRED_HEIGHT 
                    : 2,
                viewport[WIDTH], TITLE_HEIGHT, title);
	    }
    }

    /**
     * Starts the "ticking" of the ticker.
     *
     */
    private void startTicker() {
        if (ticker == null || fullScreenMode) {
            return;
        }

        stopTicker();
        tickerPainter = new TickerPainter();
        tickerTimer.schedule(tickerPainter, 0, Ticker.TICK_RATE);
    }

    /**
     * Stop the ticking of the ticker.
     *
     */
    private void stopTicker() {
        if (tickerPainter == null) {
            return;
        }
        tickerPainter.cancel();
        tickerPainter = null;
    }

    /**
     * Paints the ticker's text area.
     *
     */
    private void repaintTickerText() {

        if (currentDisplay != null &&
                currentDisplay.isShown(paintDelegate)) {

            currentDisplay.repaintImpl(paintDelegate, 0,
                                       Ticker.DECORATION_HEIGHT,
                                       viewport[WIDTH], Screen.CONTENT_HEIGHT,
                                       ticker);
        }
    }

    /**
     * This method is called from showNotify(Display )
     * It checks if a size change has occurred since the last time
     * a size change occurred or since the object was constructed.
     *
     * If a size change has occurred, it calls callSizeChanged()
     * for package private handling of size change events, and returns true,
     * else returns false.
     * 
     * @return true  if a size change has occurred since the last size change
     *               or since the construction of this Displayable.
     *         false otherwise
     *
     */
    private boolean sizeChangeImpl() {
        boolean flag = sizeChangeOccurred;
        sizeChangeOccurred = false;

        if (flag) {
            callSizeChanged(viewport[WIDTH], viewport[HEIGHT]);
        }

        return flag;
    }

    /**
     * Grabs the requested area of the display
     *
     * @param mode if true, grabs the entire display area for Canvas'
     *             display
     *             if false, occupies only part of the entire area
     *             leaving space for the status and the command labels.
     */
    native void grabFullScreen(boolean mode);
    

// ************************************************************
//  Inner Class, TickerPainter
// ************************************************************

    /**
     * A special helper class to repaint the Ticker
     * if one has been set
     */
    private class TickerPainter extends TimerTask {
        /**
         * Repaint the ticker area of this Screen
         */
        public final void run() {
            synchronized (Display.LCDUILock) {
                repaintTickerText();
            }
        }
    }

} // Displayable

