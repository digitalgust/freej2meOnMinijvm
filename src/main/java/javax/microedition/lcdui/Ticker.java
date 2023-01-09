/*
 * @(#)Ticker.java	1.68 02/08/01 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

/**
 * Implements a &quot;ticker-tape&quot;, a piece of text that runs
 * continuously across the display. The direction and speed of scrolling are
 * determined by the implementation. While animating, the ticker string
 * scrolls continuously. That is, when the string finishes scrolling off the
 * display, the ticker starts over at the beginning of the string. 
 *
 * <p> There is no API provided for starting and stopping the ticker. The
 * application model is that the ticker is always scrolling continuously.
 * However, the implementation is allowed to pause the scrolling for power
 * consumption purposes, for example, if the user doesn't interact with the
 * device for a certain period of time. The implementation should resume
 * scrolling the ticker when the user interacts with the device again. </p>
 *
 * <p>The text of the ticker may contain
 * <A HREF="Form.html#linebreak">line breaks</A>.
 * The complete text MUST be displayed in the ticker;
 * line break characters should not be displayed but may be used 
 * as separators. </p>
 * 
 * <p> The same ticker may be shared by several <code>Displayable</code>
 * objects (&quot;screens&quot;). This can be accomplished by calling
 * {@link Displayable#setTicker setTicker()} on each of them.
 * Typical usage is for an application to place the same ticker on
 * all of its screens. When the application switches between two screens that
 * have the same ticker, a desirable effect is for the ticker to be displayed
 * at the same location on the display and to continue scrolling its contents
 * at the same position. This gives the illusion of the ticker being attached
 * to the display instead of to each screen. </p>
 *
 * <p> An alternative usage model is for the application to use different
 * tickers on different sets of screens or even a different one on each
 * screen. The ticker is an attribute of the <code>Displayable</code> class
 * so that
 * applications may implement this model without having to update the ticker
 * to be displayed as the user switches among screens. </p>
 * @since MIDP 1.0
 */

public class Ticker {

    /**
     * Constructs a new <code>Ticker</code> object, given its initial
     * contents string.
     * @param str string to be set for the <code>Ticker</code>
     * @throws NullPointerException if <code>str</code> is <code>null</code>
     */
    public Ticker(String str) {
        synchronized (Display.LCDUILock) {
            setupText(str);
        }
    }

    /**
     * Sets the string to be displayed by this ticker. If this ticker is active
     * and is on the display, it immediately begins showing the new string.
     * @param str string to be set for the <code>Ticker</code>
     * @throws NullPointerException if <code>str</code> is <code>null</code>
     * @see #getString
     */
    public void setString(String str) {
        synchronized (Display.LCDUILock) {
            setupText(str);
        }
    }

    /**
     * Gets the string currently being scrolled by the ticker.
     * @return string of the ticker
     * @see #setString
     */
    public String getString() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return message;
    }

    // package private, called by Screen
    /**
     * Paint the contents of this Ticker
     *
     * @param g The Graphics object to paint to
     */
    void paintContent(Graphics g) {
        // Optimization: The TickerPainter in Screen sets the clip to be
        // Screen.CONTENT_HEIGHT. If the clip height is greater than that,
        // then we go ahead and draw the images as well as the text
        if (g.getClipHeight() > Screen.CONTENT_HEIGHT) {

            // NOTE: We test to see if the width of the Display is
            // wider than the width of our image, if so, we re-draw
            // the image offset to the right as many times as necessary
            // to fill the width of the Display. Specific ports would
            // probably want to optimize this for the specific width
            // of their device.
            for (int imgLoc = 0; imgLoc < Display.WIDTH; imgLoc += 96) {
                // We draw the top border
                g.drawImage(TICKER_IMG, imgLoc, 0,
                            Graphics.TOP | Graphics.LEFT);
                // We draw the bottom border
                g.drawImage(TICKER_IMG, imgLoc, Screen.CONTENT_HEIGHT + 3,
                            Graphics.TOP | Graphics.LEFT);
            }
        }

        // We draw the text of the message regardless of the clip, because
        // its possible the paint call from TickerPainter has coalesced with
        // other paint calls and if we don't paint the text, we may miss our
        // chance to update the Ticker display. Note this has the side effect
        // of advancing the ticker on subsequent repaints even if there is not
        // a TickerPainter timer task firing repaints. This is ok because if
        // a Ticker is visible on the screen, it should always be "running".

        g.setColor(Display.ERASE_COLOR);
        g.fillRect(0, 2, Display.WIDTH, Screen.CONTENT_HEIGHT + 1);

        g.setColor(Display.FG_COLOR);
        messageLoc -= tickSpeed;
        g.drawString(displayedMessage, messageLoc, 2,
                     Graphics.TOP | Graphics.LEFT);

        // Once the message is completely off the left side of
        // the screen, we reset its location to be the right side
        // of the screen
        if (messageLoc <= -messageWidth) {
            messageLoc = Display.WIDTH;
        }
    }

    // package private, called by Screen to reset the message to
    // the right side of the screen
    /**
     * Reset this Ticker's message location to the right side of the screen
     */
    void reset() {
        messageLoc = Display.WIDTH;
    }

    // Called by both the constructor and the setString methods
    /**
     * Initialize this Ticker with the given text
     *
     * @param message The text this Ticker will display
     */
    private final void setupText(String message) {
        if (message == null) {
            throw new NullPointerException();
        }

        /*
         * Search the message for linebreak characters, and replace 
         * with spaces.
         */
        StringBuffer msg = new StringBuffer(message);
        int offset = 0;
        boolean modified = false;
        while ((offset = message.indexOf('\n', offset)) != -1) {
          msg.setCharAt(offset, ' ');
          offset++;
          modified = true;
        }

        /* 
         * Save the original unmodified message so that getString() 
         * returns that. If the message is modified because it 
         * contains linebreak characters, then set the display message
         * to the modified string.
         */
        this.message = message;
        this.displayedMessage = modified ? msg.toString() : message;
        messageWidth = Screen.CONTENT_FONT.stringWidth(this.displayedMessage);

        if (messageWidth < 5) { 
            tickSpeed = messageWidth;
        } else {
            tickSpeed = 5;
        }

	reset();
    }

    /** The message set in this Ticker */
    private String                  message;
    /** The message to display in this Ticker */
    private String                  displayedMessage;
    /** The current location on screen of the scrolling message */
    private int                     messageLoc;
    /** The pixel width of the message being displayed */
    private int                     messageWidth;
    /**
     * tickSpeed is the distance in pixels the message will
     * travel during one tick
     */
    private int                     tickSpeed;
    /** The Image to display with this Ticker */
    private static final Image      TICKER_IMG;

    // NOTE: the height of the TICKER_IMG is 1 pixel, the width
    //       of the TICKER_IMG is 94 pixels

    /** TICK_RATE is the number of milliseconds between ticks */
    static final long               TICK_RATE = 250;
    /**
     * The preferred height of a Ticker is the same for
     * all Tickers
     */
    static final int                PREFERRED_HEIGHT;

    /** The height of the border around this ticker on one side */
    static final int                DECORATION_HEIGHT = 2;

    static {
        TICKER_IMG = ImmutableImage.createIcon("ticker_border.png");

        // The preferred height is the height of 1 line of text plus
        // the height of the top and bottom images (1 pixel each)
        // plus a pixel buffer (1 pixel top & bottom) around the message
        PREFERRED_HEIGHT = Screen.CONTENT_HEIGHT + 4;
    }

}

