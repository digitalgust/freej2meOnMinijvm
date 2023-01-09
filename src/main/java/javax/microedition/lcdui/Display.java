/*
 * @(#)Display.java	1.192 02/10/15
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

import java.util.*;
import java.io.*;

/* This is used to implement the communication between MIDlet and Display */
import javax.microedition.midlet.MIDlet;

import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.io.Connector;

import com.sun.midp.midlet.MIDletState;
import com.sun.midp.midlet.MIDletStateMap;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.Scheduler;

import com.sun.midp.io.j2me.storage.File;
import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.main.Configuration;
import com.sun.midp.lcdui.*;

import com.sun.midp.security.*;

/**
 * <code>Display</code> represents the manager of the display and
 * input devices of the
 * system. It includes methods for retrieving properties of the device and
 * for requesting that objects be displayed on the device. Other methods that
 * deal with device attributes are primarily used with {@link Canvas Canvas}
 * objects and are thus defined there instead of here. <p>
 *
 * There is exactly one instance of Display per {@link
 * javax.microedition.midlet.MIDlet MIDlet} and the application can get a
 * reference to that instance by calling the {@link
 * #getDisplay(javax.microedition.midlet.MIDlet) getDisplay()} method. The
 * application may call the <code>getDisplay()</code> method at any time
 * during course of
 * its execution. The <code>Display</code> object
 * returned by all calls to <code>getDisplay()</code> will remain the
 * same during this
 * time. <p>
 *
 * A typical application will perform the following actions in response to
 * calls to its <code>MIDlet</code> methods:
 * <UL>
 * <LI><STRONG>startApp</STRONG> - the application is moving from the
 * paused state to the active state.
 * Initialization of objects needed while the application is active should be
 * done.  The application may call
 * {@link #setCurrent(Displayable) setCurrent()} for the first screen if that
 * has not already been done. Note that <code>startApp()</code> can be
 * called several
 * times if <code>pauseApp()</code> has been called in between. This
 * means that one-time
 * initialization
 * should not take place here but instead should occur within the
 * <code>MIDlet's</code>
 * constructor.
 * </LI>
 * <LI><STRONG>pauseApp</STRONG> - the application may pause its threads.
 * Also, if it is
 * desirable to start with another screen when the application is re-activated,
 * the new screen should be set with <code>setCurrent()</code>.</LI>
 * <LI><STRONG>destroyApp</STRONG> - the application should free resources,
 * terminate threads, etc.
 * The behavior of method calls on user interface objects after
 * <code>destroyApp()</code> has returned is undefined. </li>
 * </UL>
 * <p>
 *
 * <P>The user interface objects that are shown on the display device are
 * contained within a {@link Displayable Displayable} object. At any time the
 * application may have at most one <code>Displayable</code> object
 * that it intends to be
 * shown on the display device and through which user interaction occurs.  This
 * <code>Displayable</code> is referred to as the <em>current</em>
 * <code>Displayable</code>. </p>
 *
 * <P>The <code>Display</code> class has a {@link
 * #setCurrent(Displayable) setCurrent()}
 * method for setting the current <code>Displayable</code> and a
 * {@link #getCurrent()
 * getCurrent()} method for retrieving the current
 * <code>Displayable</code>.  The
 * application has control over its current <code>Displayable</code>
 * and may call
 * <code>setCurrent()</code> at any time.  Typically, the application
 * will change the
 * current <code>Displayable</code> in response to some user action.
 * This is not always the
 * case, however.  Another thread may change the current
 * <code>Displayable</code> in
 * response to some other stimulus.  The current
 * <code>Displayable</code> will also be
 * changed when the timer for an {@link Alert Alert} elapses. </P>
 *
 * <p> The application's current <code>Displayable</code> may not
 * physically be drawn on the
 * screen, nor will user events (such as keystrokes) that occur necessarily be
 * directed to the current <code>Displayable</code>.  This may occur
 * because of the presence
 * of other <code>MIDlet</code> applications running simultaneously on
 * the same device. </p>
 *
 * <P>An application is said to be in the <em>foreground</em> if its current
 * <code>Displayable</code> is actually visible on the display device
 * and if user input
 * device events will be delivered to it. If the application is not in the
 * foreground, it lacks access to both the display and input devices, and it is
 * said to be in the <em>background</em>. The policy for allocation of these
 * devices to different <code>MIDlet</code> applications is outside
 * the scope of this
 * specification and is under the control of an external agent referred to as
 * the <em>application management software</em>. </p>
 *
 * <P>As mentioned above, the application still has a notion of its current
 * <code>Displayable</code> even if it is in the background. The
 * current <code>Displayable</code> is
 * significant, even for background applications, because the current
 * <code>Displayable</code> is always the one that will be shown the
 * next time the
 * application is brought into the foreground.  The application can determine
 * whether a <code>Displayable</code> is actually visible on the
 * display by calling {@link
 * Displayable#isShown isShown()}. In the case of <code>Canvas</code>,
 * the {@link
 * Canvas#showNotify() showNotify()} and {@link Canvas#hideNotify()
 * hideNotify()} methods are called when the <code>Canvas</code> is
 * made visible and is
 * hidden, respectively.</P>
 *
 * <P> Each <code>MIDlet</code> application has its own current
 * <code>Displayable</code>.  This means
 * that the {@link #getCurrent() getCurrent()} method returns the
 * <code>MIDlet's</code>
 * current <code>Displayable</code>, regardless of the
 * <code>MIDlet's</code> foreground/background
 * state.  For example, suppose a <code>MIDlet</code> running in the
 * foreground has current
 * <code>Displayable</code> <em>F</em>, and a <code>MIDlet</code>
 * running in the background has current
 * <code>Displayable</code> <em>B</em>.  When the foreground
 * <code>MIDlet</code> calls <code>getCurrent()</code>, it
 * will return <em>F</em>, and when the background <code>MIDlet</code>
 * calls <code>getCurrent()</code>, it
 * will return <em>B</em>.  Furthermore, if either <code>MIDlet</code>
 * changes its current
 * <code>Displayable</code> by calling <code>setCurrent()</code>, this
 * will not affect the any other
 * <code>MIDlet's</code> current <code>Displayable</code>. </p>
 *
 * <P>It is possible for <code>getCurrent()</code> to return
 * <code>null</code>. This may occur at startup
 * time, before the <code>MIDlet</code> application has called
 * <code>setCurrent()</code> on its first
 * screen.  The <code>getCurrent(</code>) method will never return a
 * reference to a
 * <code>Displayable</code> object that was not passed in a prior call
 * to <code>setCurrent()</code> call
 * by this <code>MIDlet</code>. </p>
 *
 * <a name="systemscreens"></a>
 * <h3>System Screens</h3>
 *
 * <P> Typically, the
 * current screen of the foreground <code>MIDlet</code> will be
 * visible on the display.
 * However, under certain circumstances, the system may create a screen that
 * temporarily obscures the application's current screen.  These screens are
 * referred to as <em>system screens.</em> This may occur if the system needs
 * to show a menu of commands or if the system requires the user to edit text
 * on a separate screen instead of within a text field inside a
 * <code>Form</code>.  Even
 * though the system screen obscures the application's screen, the notion of
 * the current screen does not change.  In particular, while a system screen is
 * visible, a call to <code>getCurrent()</code> will return the
 * application's current
 * screen, not the system screen.  The value returned by
 * <code>isShown()</code> is <code>false</code>
 * while the current <code>Displayable</code> is obscured by a system
 * screen. </p>
 *
 * <p> If system screen obscures a canvas, its
 * <code>hideNotify()</code> method is called.
 * When the system screen is removed, restoring the canvas, its
 * <code>showNotify()</code>
 * method and then its <code>paint()</code> method are called.  If the
 * system screen was used
 * by the user to issue a command, the <code>commandAction()</code>
 * method is called after
 * <code>showNotify()</code> is called. </p>
 *
 * <p>This class contains methods to retrieve the prevailing foreground and
 * background colors of the high-level user interface.  These methods are
 * useful for creating <CODE>CustomItem</CODE> objects that match the user
 * interface of other items and for creating user interfaces within
 * <CODE>Canvas</CODE> that match the user interface of the rest of the
 * system.  Implementations are not restricted to using foreground and
 * background colors in their user interfaces (for example, they might use
 * highlight and shadow colors for a beveling effect) but the colors returned
 * are those that match reasonably well with the implementation's color
 * scheme.  An application implementing a custom item should use the
 * background color to clear its region and then paint text and geometric
 * graphics (lines, arcs, rectangles) in the foreground color.</p>
 *
 * @since MIDP 1.0
 */

public class Display {

/*
 * ************* public member variables
 */

    /**
     * Image type for <code>List</code> element image.
     *
     * <P>The value of <code>LIST_ELEMENT</code> is <code>1</code>.</P>
     * 
     * @see #getBestImageWidth(int imageType)
     * @see #getBestImageHeight(int imageType)
     * @since MIDP 2.0
     */
    public static final int LIST_ELEMENT = 1;

    /**
     * Image type for <code>ChoiceGroup</code> element image.
     * 
     * <P>The value of <code>CHOICE_GROUP_ELEMENT</code> is <code>2</code>.</P>
     * 
     * @see #getBestImageWidth(int imageType)
     * @see #getBestImageHeight(int imageType)
     * @since MIDP 2.0
     */
    public static final int CHOICE_GROUP_ELEMENT = 2;

    /**
     * Image type for <code>Alert</code> image.
     * 
     * <P>The value of <code>ALERT</code> is <code>3</code>.</P>
     * 
     * @see #getBestImageWidth(int imageType)
     * @see #getBestImageHeight(int imageType)
     * @since MIDP 2.0
     */
    public static final int ALERT = 3;

    /**
     * A color specifier for use with <code>getColor</code>.
     * <code>COLOR_BACKGROUND</code> specifies the background color of
     * the screen.
     * The background color will always contrast with the foreground color.
     *
     * <p>
     * <code>COLOR_BACKGROUND</code> has the value <code>0</code>.
     *
     * @see #getColor
     * @since MIDP 2.0
     */
    public static final int COLOR_BACKGROUND = 0;

    /** 
     * A color specifier for use with <code>getColor</code>.
     * <code>COLOR_FOREGROUND</code> specifies the foreground color,
     * for text characters
     * and simple graphics on the screen.  Static text or user-editable
     * text should be drawn with the foreground color.  The foreground color
     * will always constrast with background color.
     *
     * <p> <code>COLOR_FOREGROUND</code> has the value <code>1</code>.
     *
     * @see #getColor 
     * @since MIDP 2.0 
     */
    public static final int COLOR_FOREGROUND = 1;

    /**
     * A color specifier for use with <code>getColor</code>.
     * <code>COLOR_HIGHLIGHTED_BACKGROUND</code> identifies the color for the
     * focus, or focus highlight, when it is drawn as a
     * filled in rectangle. The highlighted
     * background will always constrast with the highlighted foreground.
     *
     * <p>
     * <code>COLOR_HIGHLIGHTED_BACKGROUND</code> has the value <code>2</code>.
     *
     * @see #getColor
     * @since MIDP 2.0 
     */
    public static final int COLOR_HIGHLIGHTED_BACKGROUND = 2;

    /**
     * A color specifier for use with <code>getColor</code>.
     * <code>COLOR_HIGHLIGHTED_FOREGROUND</code> identifies the color for text
     * characters and simple graphics when they are highlighted. 
     * Highlighted
     * foreground is the color to be used to draw the highlighted text
     * and graphics against the highlighted background.
     * The highlighted foreground will always constrast with
     * the highlighted background.
     *
     * <p>
     * <code>COLOR_HIGHLIGHTED_FOREGROUND</code> has the value <code>3</code>.
     *
     * @see #getColor
     * @since MIDP 2.0 
     */
    public static final int COLOR_HIGHLIGHTED_FOREGROUND = 3;

    /**
     * A color specifier for use with <code>getColor</code>.
     * <code>COLOR_BORDER</code> identifies the color for boxes and borders
     * when the object is to be drawn in a
     * non-highlighted state.  The border color is intended to be used with 
     * the background color and will contrast with it.
     * The application should draw its borders using the stroke style returned 
     * by <code>getBorderStyle()</code>.
     *
     * <p> <code>COLOR_BORDER</code> has the value <code>4</code>.
     *
     * @see #getColor
     * @since MIDP 2.0 
     */
    public static final int COLOR_BORDER = 4;

    /**
     * A color specifier for use with <code>getColor</code>.
     * <code>COLOR_HIGHLIGHTED_BORDER</code>
     * identifies the color for boxes and borders when the object is to be
     * drawn in a highlighted state.  The highlighted border color is intended
     * to be used with the background color (not the highlighted background
     * color) and will contrast with it.  The application should draw its
     * borders using the stroke style returned <code>by getBorderStyle()</code>.
     *
     * <p> <code>COLOR_HIGHLIGHTED_BORDER</code> has the value <code>5</code>.
     *
     * @see #getColor
     * @since MIDP 2.0 
     */
    public static final int COLOR_HIGHLIGHTED_BORDER = 5;

/*
 * ************* protected member variables
 */

/*
 * ************* package private member variables
 */

    /** Static lock object for LCDUI package */
    static final Object LCDUILock = new Object();

    /** Static lock object for making calls into application code */
    static final Object calloutLock = new Object();

    /** horizontal width */
    static final int WIDTH;

    /** vertical height */
    static final int HEIGHT;
    
    /** height available to draw on in normal mode */
    static final int ADORNEDHEIGHT;

    /** background color for erasing */
    static final int ERASE_COLOR;

    /** border color for non-highlighted border */
    static final int BORDER_COLOR = 0x00AFAFAF; // light gray

    /** border color for highlighted border */
    static final int BORDER_H_COLOR = 0x00606060; // dark gray

    /** pixel depth of display. */
    static final int DISPLAY_DEPTH;

    /** true, if display supports color. */
    static final boolean DISPLAY_IS_COLOR;

    /** true, if the device has a pointing device. */
    static final boolean POINTER_SUPPORTED;

    /** true, if motion events are supported. */
    static final boolean MOTION_SUPPORTED;

    /** true, if repeating events are supported. */
    static final boolean REPEAT_SUPPORTED;

    /** true, if the display is double buffered. */
    static final boolean IS_DOUBLE_BUFFERED;

    /** Standard foreground color */
    static final int FG_COLOR;

    /** Standard background highlight color */
    static final int BG_H_COLOR;

    /** Standard foreground highlight color */
    static final int FG_H_COLOR;

    /** number of alpha levels supported */
    static final int ALPHA_LEVELS;

    /** keyCode for up arrow */
    static final int KEYCODE_UP;

    /** keyCode for down arrow */
    static final int KEYCODE_DOWN;

    /** keyCode for left arrow */
    static final int KEYCODE_LEFT;

    /** keyCode for right arrow */
    static final int KEYCODE_RIGHT;

    /** keyCode for select */
    static final int KEYCODE_SELECT;

/*
 * ************* private member variables
 */

    /** Display manager with private methods. */
    private static DisplayManagerImpl displayManagerImpl;
    /** Device Access manager. */
    private static DisplayDeviceAccess deviceAccess;
    /** event handler for this Display instance. */
    private static EventHandler eventHandler;

    /**  singleton Graphics object */
    private static final Graphics screenGraphics;

    /** display accessor helper class */
    private DisplayAccessor accessor;

    /** MIDlet for this display */
    private MIDlet midlet;

    /** current displayable instance */
    private Displayable current;

    /** true, if last setCurrent was non-null */
    private boolean wantsForeground;

    /** stores key code of the current key pressed at least once */
    // caters to the GameCanvas.getKeyStats()
    // latching behavior. This latched state is cleared
    // when the getKeyStats() is called.
    private int stickyKeyMask;  

    /** stores key code of the current key is currently down */
    // sets the key to 1 when the key
    // is currently down
    private int currentKeyMask;

    /** What gets the MIDlet level events. */
    private MIDletEventListener midletEventListener;

    /** true, if painting operations are suspended. */
    private boolean paintSuspended; // = false;

    /** true, if the Display is the foreground object. */
    private boolean hasForeground; //  = false;

    /** first queue of serialized repaint operations. */
    private static java.util.Vector queue1 = new java.util.Vector();

    /** second queue of serialized repaint operations. */
    private static java.util.Vector queue2 = new java.util.Vector();

    /** current active queue for serially repainted operations. */
    private static java.util.Vector currentQueue = queue1;

    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken;

/*
 * ************* Static initializer, constructor
 */
    static {

        /* done this way because native access to static fields is hard */
        DeviceCaps c = new DeviceCaps();

        WIDTH               = c.width;
        HEIGHT              = c.height;
        ADORNEDHEIGHT       = c.adornedHeight;
        ERASE_COLOR         = c.eraseColor;
        DISPLAY_DEPTH       = c.displayDepth;
        DISPLAY_IS_COLOR    = c.displayIsColor;
        POINTER_SUPPORTED   = c.pointerSupported;
        MOTION_SUPPORTED    = c.motionSupported;
        REPEAT_SUPPORTED    = c.repeatSupported;
        IS_DOUBLE_BUFFERED  = c.isDoubleBuffered;
        FG_COLOR            = 0;
        BG_H_COLOR          = FG_COLOR;
        FG_H_COLOR          = ERASE_COLOR;

        Text.FG_COLOR       = FG_COLOR;
        Text.FG_H_COLOR     = FG_H_COLOR;

        ALPHA_LEVELS        = c.numAlphaLevels;

        KEYCODE_UP          = c.keyCodeUp;
        KEYCODE_DOWN        = c.keyCodeDown;
        KEYCODE_LEFT        = c.keyCodeLeft;
        KEYCODE_RIGHT       = c.keyCodeRight;
        KEYCODE_SELECT      = c.keyCodeSelect;

        c = null; // let the DeviceCaps instance be garbage collected

        /* Let com.sun.midp classes call in to this class. */
        displayManagerImpl = new DisplayManagerImpl();
        DisplayManagerFactory.SetDisplayManagerImpl(displayManagerImpl);
        deviceAccess = new DisplayDeviceAccess();
        eventHandler = getEventHandler();

        screenGraphics = Graphics.getGraphics(null);
    }


    /**
     * initializes the display with an accessor helper class.
     *
     * @param m MIDlet that owns this display, can be null
     */
    Display(MIDlet m) {
        midlet = m;
        accessor = new DisplayAccessor();
        drawTrustedIcon(false);
    }

/*
 * ************* public methods
 */

    /**
     * Gets the <code>Display</code> object that is unique to this
     * <code>MIDlet</code>.
     * @param m <code>MIDlet</code> of the application
     * @return the display object that application can use for its user
     * interface
     *
     * @throws NullPointerException if <code>m</code> is <code>null</code>
     */
    public static Display getDisplay(MIDlet m) {
        MIDletState ms;
        Display d;

        synchronized (LCDUILock) {
            // Find or create if necessary the Display
            ms = MIDletStateMap.getState(m);
            if (ms != null) {
                d = ms.getDisplay();
                if (d != null) {
                    return d;
                }
            }

            throw new
                IllegalStateException("No display created for given MIDlet");
        }
    }

    /**
     * Returns one of the colors from the high level user interface
     * color scheme, in the form <code>0x00RRGGBB</code> based on the
     * <code>colorSpecifier</code> passed in.
     *
     * @param colorSpecifier the predefined color specifier;
     *  must be one of
     *  {@link #COLOR_BACKGROUND},
     *  {@link #COLOR_FOREGROUND},
     *  {@link #COLOR_HIGHLIGHTED_BACKGROUND},
     *  {@link #COLOR_HIGHLIGHTED_FOREGROUND},
     *  {@link #COLOR_BORDER}, or
     *  {@link #COLOR_HIGHLIGHTED_BORDER}
     * @return color in the form of <code>0x00RRGGBB</code>
     * @throws IllegalArgumentException if <code>colorSpecifier</code>
     * is not a valid color specifier
     * @since MIDP 2.0 
     */
    public int getColor(int colorSpecifier) {
        switch (colorSpecifier) {
        case COLOR_BACKGROUND:
            return ERASE_COLOR;
        case COLOR_FOREGROUND:
            return FG_COLOR;
        case COLOR_HIGHLIGHTED_BACKGROUND:
            return BG_H_COLOR;
        case COLOR_HIGHLIGHTED_FOREGROUND:
            return FG_H_COLOR;
        // REMINDER: COLOR_BORDER color and
        // COLOR_HIGHLIGHTED_BORDER color will
        // be changed once HI input is avaiable.
        case COLOR_BORDER:
            return BORDER_COLOR;
        case COLOR_HIGHLIGHTED_BORDER:
            return BORDER_H_COLOR;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the stroke style used for border drawing
     * depending on the state of the component
     * (highlighted/non-highlighted). For example, on a monochrome
     * system, the border around a non-highlighted item might be
     * drawn with a <code>DOTTED</code> stroke style while the border around a
     * highlighted item might be drawn with a <code>SOLID</code> stroke style.
     *
     * @param highlighted <code>true</code> if the border style being
     * requested is for the
     * highlighted state, <code>false</code> if the border style being
     * requested is for the
     * non-highlighted state
     * @return {@link Graphics#DOTTED} or {@link Graphics#SOLID}
     * @since MIDP 2.0 
     */
    public int getBorderStyle(boolean highlighted) {
        return (highlighted == true ? Graphics.SOLID : Graphics.DOTTED);
    }

    /**
     * Gets information about color support of the device.
     * @return <code>true</code> if the display supports color, 
     * <code>false</code> otherwise
     */
    public boolean isColor() {
        return DISPLAY_IS_COLOR;
    }

    /**
     * Gets the number of colors (if <code>isColor()</code> is
     * <code>true</code>)
     * or graylevels (if <code>isColor()</code> is <code>false</code>)
     * that can be
     * represented on the device.<P>
     * Note that the number of colors for a black and white display is
     * <code>2</code>.
     * @return number of colors
     */
    public int numColors() {
        return (1 << DISPLAY_DEPTH);
    }

    /**
     * Gets the number of alpha transparency levels supported by this
     * implementation.  The minimum legal return value is
     * <code>2</code>, which indicates
     * support for full transparency and full opacity and no blending.  Return
     * values greater than <code>2</code> indicate that alpha blending
     * is supported.  For
     * further information, see <a href="Image.html#alpha">Alpha
     * Processing</a>.
     *
     * @return number of alpha levels supported
     * @since MIDP 2.0
     */
    public int numAlphaLevels() {
        return ALPHA_LEVELS;
    }

    /**
     * Gets the current <code>Displayable</code> object for this
     * <code>MIDlet</code>.  The
     * <code>Displayable</code> object returned may not actually be
     * visible on the display
     * if the <code>MIDlet</code> is running in the background, or if
     * the <code>Displayable</code> is
     * obscured by a system screen.  The {@link Displayable#isShown()
     * Displayable.isShown()} method may be called to determine whether the
     * <code>Displayable</code> is actually visible on the display. 
     *
     * <p> The value returned by <code>getCurrent()</code> may be
     * <code>null</code>. This
     * occurs after the application has been initialized but before the first
     * call to <code>setCurrent()</code>. </p>
     *
     * @return the <code>MIDlet's</code> current <code>Displayable</code> object
     * @see #setCurrent
     */
    public Displayable getCurrent() {
        return current;
    }

    /**
     * Requests that a different <code>Displayable</code> object be
     * made visible on the
     * display.  The change will typically not take effect immediately.  It
     * may be delayed so that it occurs between event delivery method
     * calls, although it is not guaranteed to occur before the next event
     * delivery method is called.  The <code>setCurrent()</code> method returns
     * immediately, without waiting for the change to take place.  Because of
     * this delay, a call to <code>getCurrent()</code> shortly after a
     * call to <code>setCurrent()</code>
     * is unlikely to return the value passed to <code>setCurrent()</code>.
     *
     * <p> Calls to <code>setCurrent()</code> are not queued.  A
     * delayed request made by a
     * <code>setCurrent()</code> call may be superseded by a subsequent call to
     * <code>setCurrent()</code>.  For example, if screen
     * <code>S1</code> is current, then </p>
     *
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     d.setCurrent(S2);
     *     d.setCurrent(S3);     </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     *
     * <p> may eventually result in <code>S3</code> being made
     * current, bypassing <code>S2</code>
     * entirely. </p>
     *
     * <p> When a <code>MIDlet</code> application is first started,
     * there is no current
     * <code>Displayable</code> object.  It is the responsibility of
     * the application to
     * ensure that a <code>Displayable</code> is visible and can
     * interact with the user at
     * all times.  Therefore, the application should always call
     * <code>setCurrent()</code>
     * as part of its initialization. </p>
     *
     * <p> The application may pass <code>null</code> as the argument to
     * <code>setCurrent()</code>.  This does not have the effect of
     * setting the current
     * <code>Displayable</code> to <code>null</code>; instead, the
     * current <code>Displayable</code>
     * remains unchanged.  However, the application management software may
     * interpret this call as a request from the application that it is
     * requesting to be placed into the background.  Similarly, if the
     * application is in the background, passing a non-null
     * reference to <code>setCurrent()</code> may be interpreted by
     * the application
     * management software as a request that the application is
     * requesting to be
     * brought to the foreground.  The request should be considered to be made
     * even if the current <code>Displayable</code> is passed to the
     * <code>setCurrent()</code>.  For
     * example, the code </p>
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *   d.setCurrent(d.getCurrent());    </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * <p> generally will have no effect other than requesting that the
     * application be brought to the foreground.  These are only requests,
     * and there is no requirement that the application management
     * software comply with these requests in a timely fashion if at all. </p>
     *
     * <p> If the <code>Displayable</code> passed to
     * <code>setCurrent()</code> is an {@link Alert
     * Alert}, the previously current <code>Displayable</code>, if
     * any, is restored after
     * the <code>Alert</code> has been dismissed.  If there is a
     * current <code>Displayable</code>, the
     * effect is as if <code>setCurrent(Alert, getCurrent())</code>
     * had been called.  Note
     * that this will result in an exception being thrown if the current
     * <code>Displayable</code> is already an alert.  If there is no
     * current <code>Displayable</code>
     * (which may occur at startup time) the implementation's previous state
     * will be restored after the <code>Alert</code> has been
     * dismissed.  The automatic
     * restoration of the previous <code>Displayable</code> or the
     * previous state occurs
     * only when the <code>Alert's</code> default listener is present
     * on the <code>Alert</code> when it
     * is dismissed.  See <a href="Alert.html#commands">Alert Commands and
     * Listeners</a> for details.</p>
     *
     * <p>To specify the
     * <code>Displayable</code> to be shown after an
     * <code>Alert</code> is dismissed, the application
     * should use the {@link #setCurrent(Alert,Displayable) setCurrent(Alert,
     * Displayable)} method.  If the application calls
     * <code>setCurrent()</code> while an
     * <code>Alert</code> is current, the <code>Alert</code> is
     * removed from the display and any timer
     * it may have set is cancelled. </p>
     *
     * <p> If the application calls <code>setCurrent()</code> while a
     * system screen is
     * active, the effect may be delayed until after the system screen is
     * dismissed.  The implementation may choose to interpret
     * <code>setCurrent()</code> in
     * such a situation as a request to cancel the effect of the system
     * screen, regardless of whether <code>setCurrent()</code> has
     * been delayed. </p>
     *
     * @param nextDisplayable the <code>Displayable</code> requested
     * to be made current;
     * <code>null</code> is allowed
     * @see #getCurrent
     */
    public void setCurrent(Displayable nextDisplayable) {
        synchronized (LCDUILock) {
            if (nextDisplayable instanceof Alert) {
                /*
                 * This implicitly goes back to the current screen.
                 *
                 * If there is a pending screen change, we take that
                 * into account also.  This probably acts according to
                 * the principle of least astonishment.  Also, it
                 * has the effect of preventing an implicit call of
                 * setCurrent(Alert, Alert) because nextScreen is
                 * never an alert.
                 *
                 * REMIND: This is subject to spec interpretation;
                 * the handling of setCurrent(Alert) is a weak area.
                 */

                // NOTE: Removed the copy of the Alert
                ((Alert)nextDisplayable).setReturnScreen(current);
            }

            setCurrentImpl(nextDisplayable);
        } // synchronized
    }

    /**
     * Requests that this <code>Alert</code> be made current, and that
     * <code>nextDisplayable</code> be
     * made current
     * after the <code>Alert</code> is dismissed.  This call returns
     * immediately regardless
     * of the <code>Alert's</code> timeout value or whether it is a
     * modal alert.  The
     * <code>nextDisplayable</code> must not be an <code>Alert</code>,
     * and it must not be <code>null</code>.
     *
     * <p>The automatic advance to <code>nextDisplayable</code> occurs only 
     * when the <code>Alert's</code> default listener is present on
     * the <code>Alert</code> when it
     * is dismissed.  See <a href="Alert.html#commands">Alert Commands and
     * Listeners</a> for details.</p>
     * 
     * <p> In other respects, this method behaves identically to
     * {@link #setCurrent(Displayable) setCurrent(Displayable)}. </p>
     *
     * @param alert the alert to be shown
     * @param nextDisplayable the <code>Displayable</code> to be
     * shown after this alert is  dismissed
     * 
     * @throws NullPointerException if alert or
     * <code>nextDisplayable</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>nextDisplayable</code>
     * is an <code>Alert</code>
     * @see Alert
     * @see #getCurrent
     */
    public void setCurrent(Alert alert, Displayable nextDisplayable) {
        if ((alert == null) || (nextDisplayable == null)) {
            throw new NullPointerException();
        }

        if (nextDisplayable instanceof Alert) {
            throw new IllegalArgumentException();
        }

        synchronized (LCDUILock) {
            alert.setReturnScreen(nextDisplayable);
            setCurrentImpl(alert);
        }
    }

    /**
     * Requests that the <code>Displayable</code> that contains this
     * <code>Item</code> be made current,
     * scrolls the <code>Displayable</code> so that this
     * <code>Item</code> is visible, and possibly
     * assigns the focus to this <code>Item</code>.  The containing
     * <code>Displayable</code> is first
     * made current as if {@link #setCurrent(Displayable)
     * setCurrent(Displayable)} had been called.  When the containing
     * <code>Displayable</code> becomes current, or if it is already
     * current, it is
     * scrolled if necessary so that the requested <code>Item</code>
     * is made visible.
     * Then, if the implementation supports the notion of input focus, and if
     * the <code>Item</code> accepts the input focus, the input focus
     * is assigned to the
     * <code>Item</code>.
     *
     * <p>This method always returns immediately, without waiting for the
     * switching of the <code>Displayable</code>, the scrolling, and
     * the assignment of
     * input focus to take place.</p>
     *
     * <p>It is an error for the <code>Item</code> not to be contained
     * within a container.
     * It is also an error if the <code>Item</code> is contained
     * within an <code>Alert</code>.</p>
     *
     * @param item the item that should be made visible
     * @throws IllegalStateException if the item is not owned by a container
     * @throws IllegalStateException if the item is owned by an 
     * <code>Alert</code>
     * @throws NullPointerException if <code>item</code> is <code>null</code>
     * @since MIDP 2.0
     */
    public void setCurrentItem(Item item) {
        synchronized (LCDUILock) {

            Screen nextDisplayable = item.getOwner();
            if (nextDisplayable instanceof Form) {
                ((Form)nextDisplayable).setCurrentItem(item);
            }

            if (nextDisplayable == null) {
                throw new IllegalStateException();
            }

            setCurrentImpl(nextDisplayable);
        } // synchronized
    }

    /**
     * Causes the <code>Runnable</code> object <code>r</code> to have
     * its <code>run()</code> method
     * called later, serialized with the event stream, soon after completion of
     * the repaint cycle.  As noted in the
     * <a href="./package-summary.html#events">Event Handling</a>
     * section of the package summary,
     * the methods that deliver event notifications to the application
     * are all called serially. The call to <code>r.run()</code> will
     * be serialized along with
     * the event calls into the application. The <code>run()</code>
     * method will be called exactly once for each call to
     * <code>callSerially()</code>. Calls to <code>run()</code> will
     * occur in the order in which they were requested by calls to
     * <code>callSerially()</code>.
     *
     * <p> If the current <code>Displayable</code> is a <code>Canvas</code>
     * that has a repaint pending at the time of a call to
     * <code>callSerially()</code>, the <code>paint()</code> method of the
     * <code>Canvas</code> will be called and
     * will return, and a buffer switch will occur (if double buffering is in
     * effect), before the <code>run()</code> method of the
     * <code>Runnable</code> is called.
     * If the current <code>Displayable</code> contains one or more
     * <code>CustomItems</code> that have repaints pending at the time
     * of a call to <code>callSerially()</code>, the <code>paint()</code>
     * methods of the <code>CustomItems</code> will be called and will
     * return before the <code>run()</code> method of the
     * <code>Runnable</code> is called.
     * Calls to the
     * <code>run()</code> method will occur in a timely fashion, but
     * they are not guaranteed
     * to occur immediately after the repaint cycle finishes, or even before
     * the next event is delivered. </p>
     *
     * <p> The <code>callSerially()</code> method may be called from
     * any thread. The call to
     * the <code>run()</code> method will occur independently of the
     * call to <code>callSerially()</code>.
     * In particular, <code>callSerially()</code> will <em>never</em>
     * block waiting
     * for <code>r.run()</code>
     * to return. </p>
     *
     * <p> As with other callbacks, the call to <code>r.run()</code>
     * must return quickly. If
     * it is necessary to perform a long-running operation, it may be initiated
     * from within the <code>run()</code> method. The operation itself
     * should be performed
     * within another thread, allowing <code>run()</code> to return. </p>
     *
     * <p> The <code>callSerially()</code> facility may be used by
     * applications to run an
     * animation that is properly synchronized with the repaint cycle. A
     * typical application will set up a frame to be displayed and then call
     * <code>repaint()</code>.  The application must then wait until
     * the frame is actually
     * displayed, after which the setup for the next frame may occur.  The call
     * to <code>run()</code> notifies the application that the
     * previous frame has finished
     * painting.  The example below shows <code>callSerially()</code>
     * being used for this
     * purpose. </p>
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *     class Animation extends Canvas
     *         implements Runnable {
     *
     *     // paint the current frame
     *     void paint(Graphics g) { ... }
     *
     *        Display display; // the display for the application
     *
     *        void paint(Graphics g) { ... } // paint the current frame
     *
     *        void startAnimation() {
     *            // set up initial frame
     *            repaint();
     *            display.callSerially(this);
     *        }
     *
     *        // called after previous repaint is finished
     *        void run() {
     *            if ( &#47;* there are more frames *&#47; ) {
     *                // set up the next frame
     *                repaint();
     *                display.callSerially(this);
     *            }
     *        }
     *     }    </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * @param r instance of interface <code>Runnable</code> to be called
     */
    public void callSerially(Runnable r) {
        if (r == null) {
            throw new NullPointerException();
        }
        synchronized (LCDUILock) {
            currentQueue.addElement(r);
            eventHandler.scheduleCallSerially();
        }
    }

    /**
     * Requests a flashing effect for the device's backlight.  The flashing
     * effect is intended to be used to attract the user's attention or as a
     * special effect for games.  Examples of flashing are cycling the
     * backlight on and off or from dim to bright repeatedly.
     * The return value indicates if the flashing of the backlight
     * can be controlled by the application.
     *
     * <p>The flashing effect occurs for the requested duration, or it is
     * switched off if the requested duration is zero.  This method returns
     * immediately; that is, it must not block the caller while the flashing
     * effect is running.</p>
     *
     * <p>Calls to this method are honored only if the
     * <code>Display</code> is in the
     * foreground.  This method MUST perform no action
     * and return <CODE>false</CODE> if the
     * <code>Display</code> is in the background.
     *
     * <p>The device MAY limit or override the duration. For devices
     * that do not include a controllable backlight, calls to this
     * method return <CODE>false</CODE>.
     *
     * @param duration the number of milliseconds the backlight should be
     * flashed, or zero if the flashing should be stopped
     *
     * @return <CODE>true</CODE> if the backlight can be controlled
     *           by the application and this display is in the foreground,
     *          <CODE>false</CODE> otherwise
     *
     * @throws IllegalArgumentException if <code>duration</code> is negative
     *
     * @since MIDP 2.0
     */
    public boolean flashBacklight(int duration) {

        if (!hasForeground) {
            return false;
        }

        return deviceAccess.flashBacklight(duration);
    }
    
    /**
     * Requests operation of the device's vibrator.  The vibrator is
     * intended to be used to attract the user's attention or as a
     * special effect for games.  The return value indicates if the
     * vibrator can be controlled by the application.
     *
     * <p>This method switches on the vibrator for the requested
     * duration, or switches it off if the requested duration is zero.
     * If this method is called while the vibrator is still activated
     * from a previous call, the request is interpreted as setting a
     * new duration. It is not interpreted as adding additional time
     * to the original request. This method returns immediately; that
     * is, it must not block the caller while the vibrator is
     * running. </p>
     *
     * <p>Calls to this method are honored only if the
     * <code>Display</code> is in the foreground.  This method MUST
     * perform no action and return <CODE>false</CODE> if the
     * <code>Display</code> is in the background.</p>
     *
     * <p>The device MAY limit or override the duration.  For devices
     * that do not include a controllable vibrator, calls to this
     * method return <CODE>false</CODE>.</p>
     *
     * @param duration the number of milliseconds the vibrator should be run,
     * or zero if the vibrator should be turned off
     *
     * @return <CODE>true</CODE> if the vibrator can be controlled by the
     *           application and this display is in the foreground,
     *          <CODE>false</CODE> otherwise
     *
     * @throws IllegalArgumentException if <code>duration</code> is negative
     *
     * @since MIDP 2.0
     */
    public boolean vibrate(int duration) {

        if (!hasForeground) {
            return false;
        }

        if (duration < 0) {
            throw new IllegalArgumentException();
        }
        
        if (nVibrate(duration) <= 0) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * Returns the best image width for a given image type.
     * The image type must be one of
     * {@link #LIST_ELEMENT},
     * {@link #CHOICE_GROUP_ELEMENT}, or
     * {@link #ALERT}.
     * 
     * @param imageType the image type
     * @return the best image width for the image type, may be zero if
     * there is no best size; must not be negative
     * @throws IllegalArgumentException if <code>imageType</code> is illegal
     * @since MIDP 2.0
     */
    public int getBestImageWidth(int imageType) {
        switch (imageType) {
        case LIST_ELEMENT:
        case CHOICE_GROUP_ELEMENT:
            return ChoiceGroup.PREFERRED_IMG_W;
        case ALERT:
            return Display.WIDTH;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the best image height for a given image type.
     * The image type must be one of
     * {@link #LIST_ELEMENT},
     * {@link #CHOICE_GROUP_ELEMENT}, or
     * {@link #ALERT}.
     * 
     * @param imageType the image type
     * @return the best image height for the image type, may be zero if
     * there is no best size; must not be negative
     * @throws IllegalArgumentException if <code>imageType</code> is illegal
     * @since MIDP 2.0
     */
    public int getBestImageHeight(int imageType) {
        switch (imageType) {
        case LIST_ELEMENT:
        case CHOICE_GROUP_ELEMENT:
            return ChoiceGroup.PREFERRED_IMG_H;
        case ALERT:
            // return max height ignoring title and ticker and
            // allow for 2 lines of Text to be visible without scrolling
            return Display.ADORNEDHEIGHT - 2 * Screen.CONTENT_HEIGHT;
        default:
            throw new IllegalArgumentException();
        }
    }


/*
 * ************* protected methods
 */

/*
 * ************* package private methods
 */

    /**
     * Set the current Displayable and notify the display manager if
     * the has current state has changed.
     *
     * @param nextDisplayable The next Displayable to display
     */
    void setCurrentImpl(Displayable nextDisplayable) {
        boolean previousWantsForeground = wantsForeground;

        /*
         * note: this method handles the setCurrent(null) then
         * setCurrent(getCurrent()) case
         */

        wantsForeground = nextDisplayable != null;

        if (wantsForeground) {
            if (nextDisplayable != current) {
                if (hasForeground) {
                    // If a call to setCurrent() is coming in while in a
                    // suspended state, notify the event handler to dismiss
                    // the system screen
                    if (paintSuspended) {
                        eventHandler.clearSystemScreen();
                        paintSuspended = false;
                    }

                    eventHandler.scheduleScreenChange(this, nextDisplayable);

                    /*
                     * have the foreground, no need to notify the display
                     * manager
                     */
                    return;
                }

                // set current before we notify the display manager
                current = nextDisplayable;
            }
        }

        if (wantsForeground != previousWantsForeground) {
            // only notify the display manager on a change
            displayManagerImpl.notifyWantsForeground(this.accessor,
                                                     wantsForeground);
        }
    }

    /**
     * Change the current screen to the given displayable.
     *
     * @param d The Displayable to make current
     */
    void screenChange(Displayable d) {
        synchronized (LCDUILock) {
            if (current == d && !paintSuspended) {
                return;
            } else if (paintSuspended) {
                // If we happen to be suspended, simply update our current
                // screen to be the new Displayable and return. When we are
                // resumed, registerNewCurrent() will be called with the
                // newly set screen
                current = d;
                return;
            }
        }

        // Its ok to ignore the foreground/paintSuspended state from here
        // on, the last thing registerNewCurrent() will do is try to do
        // a repaint(), which will not occur if the
        // foreground/paintSuspended status has changed in the meantime.
        registerNewCurrent(d, false);
    }

    /**
     * Called by Alert when a modal alert has exited.
     *
     * @param returnScreen The Displayable to return to after this
     *                     Alert is cleared.
     */
    void clearAlert(Displayable returnScreen) {
        eventHandler.scheduleScreenChange(this, returnScreen);
    }

    /**
     * play a sound.
     * @param t type of alert
     * @return true, if sound was played.
     */
    boolean playAlertSound(AlertType t) {

        if (!paintSuspended && hasForeground) {
            try {
                // SYNC NOTE: playAlertSound is a native method, no locking
                // necessary
                return playAlertSound(t.getType());
            } catch (Exception e) {}
        }

        return false;
    }

    /**
     * Schedule a Form invalidation based on the given Item.
     * The item may be null.
     *
     * @param item The Item which caused the invalidation
     */
    void invalidate(Item item) {
        eventHandler.scheduleInvalidate(item);
    }

    /**
     * Schedule the notificaton of an ItemStateListener due to
     * a change in the given item.
     *
     * @param item The Item which has changed
     */
    void itemStateChanged(Item item) {
        eventHandler.scheduleItemStateChanged(item);
    }

    /**
     * get the associated MIDlet from the current display.
     * @return MIDlet that is associated with the current Display.
     */
    MIDlet getMIDlet() {
        return midlet;
    }

    /**
     * Request a repaint for the given Displayable.  The rectangle to be
     * painted is in x,y,w,h.  If delay is greater than zero, the repaint
     * may be deferred until that interval (in milliseconds) has elapsed.
     *
     * If the given Displayable is not current, the request is ignored.
     * This is safe because whenever a Displayable becomes current, it gets
     * a full repaint anyway.
     *
     * The target Object is optional. It will be packaged along with the
     * repaint request and arrive later when the repaint is serviced in
     * the callPaint() routine - IF this paint request has not been
     * coalesced with other repaints.
     *
     * @param d displayable object to be drawn
     * @param x upper left corner x-coordinate
     * @param y upper left corner y-coordinate
     * @param w horizontal width
     * @param h vertical height
     * @param target an optional paint target
     */
    void repaintImpl(Displayable d, int x, int y, int w, int h,
                     Object target) {

        synchronized (LCDUILock) {
            if (paintSuspended || !hasForeground || d != current) {
                return;
            }
        }

        eventHandler.scheduleRepaint(x, y, w, h, target);
    }

    /**
     * Process any pending repaint requests immediately.
     *
     * @param d The Displayable which is requesting the repaint
     *          (Used to determine if the Displayable making the
     *           request is currently being shown)
     *
     * SYNC NOTE: this method performs its own locking of
     * LCDUILock.  Therefore, callers
     * must not hold any locks when they call this method.
     */
    void serviceRepaints(Displayable d) {

        synchronized (LCDUILock) {
            if (paintSuspended || !hasForeground || d != current) {
                return;
            }
        }

        eventHandler.serviceRepaints();
    }

    /**
     * Process the specified repaint request immediately.
     *
     * @param x1 The x origin of the paint bounds
     * @param y1 The y origin of the paint bounds
     * @param x2 The x coordinate of the lower right bounds
     * @param y2 The y coordinate of the lower right bounds
     * @param target The optional paint target
     *
     * SYNC NOTE: this method performs its own locking of
     * LCDUILock and calloutLock.  Therefore, callers
     * must not hold any locks when they call this method.
     */
    void repaint(int x1, int y1, int x2, int y2, Object target) {
        Displayable currentCopy = null;

        synchronized (LCDUILock) {
            if (paintSuspended || !hasForeground) {
                return;
            }
            currentCopy = current;
        }

        if (currentCopy == null) {
            return;
        }

        screenGraphics.reset(x1, y1, x2, y2);
        current.callPaint(screenGraphics, target);
        refresh(x1, y1, x2, y2);
    }

    /**
     * Return the key code that corresponds to the specified game
     * action on the device.  gameAction must be a defined game action
     * (Canvas.UP, Canvas.DOWN, Canvas.FIRE, etc.)
     * <B>Post-conditions:</B><BR> The key code of the key that
     * corresponds to the specified action is returned.  The return
     * value will be -1 if the game action is invalid or not supported
     * by the device.
     *
     * @param gameAction The game action to obtain the key code for.
     * @return the key code.
     */
    static int getKeyCode(int gameAction) {
        return eventHandler.getKeyCode(gameAction);
    }

    /**
     * get the current vertical scroll position.
     * @return the vertical scroll position.
     */
    int getVerticalScrollPosition() {
        // SYNC NOTE: No need to lock here because 'current'
        // can only be null once, at startup, so we don't care
        // if 'current' changes values, just that it isn't null
        if (current != null) {
            return current.getVerticalScrollPosition();
        } else {
            return 0;
        }
    }

    /**
     * get the current vertical scroll proportion.
     * @return the vertical scroll proportion.
     */
    int getVerticalScrollProportion() {
        // SYNC NOTE: No need to lock here because 'current'
        // can only be null once, at startup, so we don't care
        // if 'current' changes values, just that it isn't null
         if (current != null) {
            return current.getVerticalScrollProportion();
        } else {
            return 100;
        }
    }

    /**
     * Return the game action associated with the given key code on
     * the device.  keyCode must refer to a key that is mapped as a
     * game key on the device The game action of the key is returned.
     * The return value will be 0 if the key is not mapped to a game
     * action or not present on the device.
     *
     * @param keyCode the key code
     * @return the corresponding game action (UP, DOWN, LEFT, RIGHT, FIRE, etc.)
     */
    static int getGameAction(int keyCode) {
        return eventHandler.getGameAction(keyCode);
    }

    /**
     * returns 0 if keyCode is not a system key.  Otherwise, returns
     * one of the EventHandler.SYSTEM_KEY_ constants.
     * @param keyCode get the system equivalent key.
     * @return translated system key or zero if it is not a system key.
     */
    static int getSystemKey(int keyCode) {
        return eventHandler.getSystemKey(keyCode);
    }

    /**
     * Gets an informative key string for a key. The string returned
     * should resemble the text physically printed on the key. For
     * example, on a device with function keys F1 through F4, calling
     * this method on the keycode for the F1 key will return the
     * string "F1". A typical use for this string will be to compose
     * help text such as "Press F1 to proceed."
     *
     * <p>There is no direct mapping from game actions to key
     * names. To get the string name for a game action, the
     * application must call
     *
     * <p><code>getKeyName(getKeyCode(GAME_A))</code>
     * @param keyCode the key code being requested
     * @return a string name for the key, or null if no name is available
     */
    static String getKeyName(int keyCode) {
        return eventHandler.getKeyName(keyCode);
    }

    /**
     * Update the system's account of abstract commands
     *
     * SYNC NOTE: Calls to this method should be synchronized
     * on the LCDUILock
     */
    void updateCommandSet() {

        Command[] screenCommands = current.getCommands();
        int screenComCount = current.getCommandCount();
        for (int i = 0; i < screenComCount; i++) {
             screenCommands[i].setInternalID(i);
        }

        Item curItem = current.getCurrentItem();
        if (curItem == null) {
            eventHandler.updateCommandSet(null, 0,
                                          screenCommands, screenComCount);
        } else {
            Command[] itemCommands = curItem.getCommands();
            int itemComCount = curItem.getCommandCount();
            for (int i = 0; i < itemComCount; i++) {
                itemCommands[i].setInternalID(i + screenComCount);
            }
            eventHandler.updateCommandSet(itemCommands, itemComCount,
                                          screenCommands, screenComCount);
        }
    }

    /**
     * is the current display visible.
     * @param d displayble instance to check, if current and visible.
     * @return true, if the Display is visible and the object is current.
     */
    boolean isShown(Displayable d) {
        // SYNC NOTE: calls to isShown() should be synchronized on
        // the LCDUILock.
        return hasForeground
            && !paintSuspended
            && (current == d);
    }

    /**
     * This is a utility method used to handle any Throwables
     * caught while calling application code. The default
     * implementation will simply call printStackTrace() on
     * the Throwable. Note, the parameter must be non-null or
     * this method will generate a NullPointerException.
     *
     * @param t The Throwable caught during the call into
     *          application code.
     */
    static void handleThrowable(Throwable t) {
        t.printStackTrace();
    }

    /**
     * set the current vertical scroll position and proportion.
     * @param scrollPosition vertical scroll position.
     * @param scrollProportion vertical scroll proportion.
     */
    native void setVerticalScroll(int scrollPosition, int scrollProportion);

    /**
     * set the input, mode.
     * @param mode type of input to accept.
     */
    native void setInputMode(int mode);

    /**
     * Control the drawing of the trusted MIDlet
     * icon in native.
     * @param drawTrusted true if the icon should be drawn, 
     *                    false if it should not.
     */
    private native void drawTrustedIcon(boolean drawTrusted);

    /**
     * See if we're using the display's Graphics object. 
     * @param gfx Graphics object to compare to <code>screenGraphics</code>.
     * @return true if <code>gfx</code> equals <code>screenGraphics</code>,
     *         false otherwise.
     */
    static boolean isGraphicsDisplay(Graphics gfx) {
        return screenGraphics == gfx;
    }

/*
 * ************* private methods
 */

    /**
     *  get the class that handle I/O event stream for this display.
     * @return handle to the event handler for this display
     */
    private static EventHandler getEventHandler() {

        String n = Configuration.getProperty(
            "com.sun.midp.lcdui.eventHandler");

        try {
            return (EventHandler) (Class.forName(n)).newInstance();
        } catch (Exception e) { }

        if (Configuration.getProperty("microedition.configuration") != null) {
            try {
                return (EventHandler) (Class.forName(
                    "com.sun.midp.lcdui.AutomatedEventHandler")).newInstance();
            } catch (Exception e) { }

            try {
                return (EventHandler) (Class.forName(
                    "com.sun.midp.lcdui.DefaultEventHandler")).newInstance();
            } catch (Exception e) { }

            throw new Error("Unable to establish EventHandler");
        }

        try {
            return (EventHandler)
                (Class.forName(
                    "com.sun.midp.lcdui.AWTEventHandler")).newInstance();
        } catch (Exception e) { }

        throw new Error("Unable to establish EventHandler");
    }

    /**
     * Registers a new Displayable object to this Display. This means that
     * it is now the current Displayable object, eligible to receive input
     * events, and eligible to paint. If necessary, showNotify() is called
     * on this Displayable and hideNotify() is called on the Displayable
     * being replaced. This method is used to initialize a Displayable as
     * a result of either:
     *  - a SCREEN change timerEvent()
     *  - a call to resumePainting()
     *  - a change in foreground status of this Display (results in a call
     *    to resumePainting())
     *
     * @param newCurrent The Displayable to take effect as the new
     *                      "current" Displayable
     * @param fgChange  If True, then this call to registerNewCurrent() is a
     *                  result of this Display being moved to the foreground,
     *                  i.e. setForeground(true)
     */
    private void registerNewCurrent(Displayable newCurrent, boolean fgChange)
    {
        // If this Display is not in the foreground, simply record
        // the new Displayable and return. When this Display resumes
        // the foreground, this method will be called again.

        Displayable currentCopy = null;

        // SYNC NOTE: The implementation of callShowNotify() will
        // use LCDUILock to lock around its internal handling.
        // Canvas will override callShowNotify() to first call
        // super(), and then obtain a lock
        // on calloutLock around a call to the applications
        // showNotify() method.

        if (newCurrent != null) {
            newCurrent.callShowNotify(Display.this);
        }

        synchronized (LCDUILock) {
            if (fgChange) {
                hasForeground = true;

                if (newCurrent == null) {
                    /*
                     * At least clear the screen so the last display, which
                     * could be destroyed (like the Selector) is not still
                     * showing.
                     */
                    screenGraphics.reset(0, 0, WIDTH, HEIGHT);
                    screenGraphics.setColor(Display.ERASE_COLOR);
                    screenGraphics.fillRect(0, 0, WIDTH, HEIGHT);
                    refresh(0, 0, WIDTH, HEIGHT);
                    eventHandler.updateCommandSet(null, 0, null, 0);

                    /*
                     * Without a displayable, we can shortcircuit the rest
                     * of the call and simply return.
                     */
                    return;
                }
            }


            // This will suppress drags, repeats and ups until a
            // corresponding down is seen.
            accessor.sawPointerPress = accessor.sawKeyPress = false;

            // We re-set our suspended state to false.
            paintSuspended = false;

            // We make a copy of the current Displayable to call
            // hideNotify() when we're done
            currentCopy = current;

            current = newCurrent;

            // set mapping between GameCanvas and DisplayAccess
            // set Game key event flag based on value passed in
            // GameCanvas constructor.
            if (current instanceof GameCanvas) {
                GameMap.register(current, accessor);
                stickyKeyMask = currentKeyMask = 0;
            } else {
                // set the keymask to -1 when
                // the displayable is not a GameCanvas.
                stickyKeyMask = currentKeyMask = -1;
            }

            setVerticalScroll(
                current.getVerticalScrollPosition(),
                current.getVerticalScrollProportion());

            // Next, update the command set
                updateCommandSet();

        } // synchronized

        // SYNC NOTE: The implementation of callHideNotify()
        // will use LCDUILock to lock around its internal handling.
        // Canvas will override callHideNotify(), first calling
        // super(), and then obtaining calloutLock around a call
        // to the application's hideNotify() method.

        // NOTE: the reason we test for currentCopy != current is
        // for cases when the Display has interrupted its suspension
        // by a system screen to immediately return to its "current"
        // Displayable. In this case, currentCopy == current, and
        // we've just called showNotify() above (no need to call
        // hideNotify())

        if (currentCopy != null && currentCopy != current) {
            currentCopy.callHideNotify(Display.this);
        }
        repaint(0, 0, WIDTH, HEIGHT, null);
    } // registerNewCurrent()

    /**
     * run the serially repainted operations.
     */
    private void getCallSerially() {
        java.util.Vector q = null;

        synchronized (LCDUILock) {
            q = currentQueue;
            currentQueue = (q == queue1) ? queue2 : queue1;
        }

        // SYNC NOTE: we synch on calloutLock for the call into
        // application code
        synchronized (calloutLock) {
            for (int i = 0; i < q.size(); i++) {
                try {
                    Runnable r = (Runnable) q.elementAt(i);
                    r.run();
                } catch (Throwable thr) {
                    handleThrowable(thr);
                }
            }
        }

        q.removeAllElements();
    }

    /**
     * Invalidate the current Form possibly due to an item's request
     *
     * @param item the Item which caused the invalidation
     */
    private void callInvalidate(Item item) {
        Displayable currentCopy = current;

        if (currentCopy != null) {
            currentCopy.callInvalidate(item);
        }
    }

    /**
     * Notify an ItemStateChangeListener of a change in the given
     * item.
     *
     * @param item the Item which has changed
     */
    private void callItemStateChanged(Item item) {
        Displayable currentCopy = current;

        if (currentCopy != null) {
            currentCopy.callItemStateChanged(item);
        }
    }

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    private static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken != null) {
            return;
        }
        
        classSecurityToken = token;
    }

    /**
     * redraw a portiion of the display.
     *
     * @param x1 upper left corner x-coordinate
     * @param y1 upper left corner y-coordinate
     * @param x2 lower right corner x-coordinate
     * @param y2 lower right corner y-coordinat
     */
    private native void refresh(int x1, int y1, int x2, int y2);

    /**
     * play a sound.
     * @param alertType type of alert
     * @return true, if sound was played.
     */
    private native boolean playAlertSound(int alertType);

    /**
     * Plays the vibration.
     * @param dur the duration in milli seconds
     * @return if it's successful, return 1, othewise return 0
     *
     */
    native private int nVibrate(int dur);

    /**
     * ************* Inner Class, DisplayAccessor
     */

    /** This is nested inside Display so that it can see the private fields */
    class DisplayAccessor implements DisplayAccess {

        /**
         * display painting.
         */
        public void suspendPainting() {
            Displayable currentCopy = null;

            synchronized (Display.LCDUILock) {
                paintSuspended = true;
                currentCopy = current;
                if (current instanceof Screen) {
                    ((Screen)current).resetToTop = false;
                }
            }

            // SYNC NOTE: The implementation of callHideNotify()
            // will use LCDUILock to lock around its internal handling.
            // Canvas will override callHideNotify(), first calling
            // super(), and then obtaining calloutLock around a call
            // to the application's hideNotify() method.

            if (currentCopy != null) {
                currentCopy.callHideNotify(Display.this);
            }

            // We want to re-set the scroll indicators when we suspend so
            // that the overtaking screen doesn't have to do it
            setVerticalScroll(0, 100);
        }

        /**
         * resume paininting , if doing repaint operations.
         */
        public void resumePainting() {
            registerNewCurrent(current, false);
        }

        /**
         * Called from the event delivery system when a command is seen.
         * @param id The id of the command for listener notification
         *           (as is returned by Command.getID())
         */
        public void commandAction(int id) {
            Command commands[];
            CommandListener listener;
            ItemCommandListener itemListener = null;

            Command c = null;
            Displayable currentCopy = null;
            Item curItem = null;

            synchronized (LCDUILock) {
                if (current == null) {
                    return;
                }

                    // See if one of the screen commands was activated
                if (((listener = current.getCommandListener()) != null) &&
                        (commands = current.getCommands()) != null) {

                    for (int i = 0, nc = current.getCommandCount();
                            i < nc; i++) {
                        if (commands[i] != null && commands[i].getID() == id) {
                            c = commands[i];
                            currentCopy = current;
                            break;
                        }
                    }
                }

                if ((c == null) &&
                        ((curItem = current.getCurrentItem()) != null)) {

                    // See if one of the item commands was activated
                    if (((itemListener = curItem.getItemCommandListener())
                        != null) &&
                            (commands = curItem.getCommands()) != null) {

                        for (int i = 0, nc = curItem.getCommandCount();
                                i < nc; i++) {
                            if (commands[i] != null &&
                                    commands[i].getID() == id) {
                                c = commands[i];
                                break;
                            }
                        }
                    }
                }

                if (c == null) {
                    // This means that in the time it took to receive the
                    // Command event, that Command has since been removed
                    // from the current Displayable, or the Displayable from
                    // which that Command originated is no longer current
                    return;
                }

                // commit any pending user interface for the current 
                // displayable or item
                if (currentCopy != null) {
                    currentCopy.commitPendingInteraction();
                } else {
                    curItem.commitPendingInteraction();
                }
            } // synchronized

            // Protect from any unexpected application exceptions
            try {
                // SYNC NOTE: We release the lock on LCDUILock and acquire
                // calloutLock before calling into application code
                synchronized (calloutLock) {
                    if (currentCopy != null) {
                        // screen command was activated
                        listener.commandAction(c, currentCopy);
                    } else {
                        itemListener.commandAction(c, curItem);
                    }
                }
            } catch (Throwable thr) {
                handleThrowable(thr);
            }
        } // commandAction

        /**
         * Called from the event delivery loop when a pointer event is seen.
         * @param type kind of pointer event
         * @param x x-coordinate of pointer event
         * @param y y-coordinate of pointer event
         */
        public void pointerEvent(int type, int x, int y) {
            Displayable currentCopy = null;
            int eventType = -1;

            synchronized (LCDUILock) {
                if (current == null) {
                    return;
                }
                currentCopy = current;

                switch (type) {
                    case EventHandler.PRESSED:
                        sawPointerPress = true;
                        eventType = 0;
                        break;
                    case EventHandler.RELEASED:
                        if (sawPointerPress) {
                            eventType = 1;
                        }
                        break;
                    case EventHandler.DRAGGED:
                        if (sawPointerPress) {
                            eventType = 2;
                         }
                        break;
                }
            } // synchronized

            // SYNC NOTE: Since we may call into application code,
            // we do so outside of LCDUILock
            switch (eventType) {
                case -1:
                    return;
                case 0:
                    currentCopy.callPointerPressed(x, y);
                    break;
                case 1:
                    currentCopy.callPointerReleased(x, y);
                    break;
                case 2:
                    currentCopy.callPointerDragged(x, y);
                    break;
                default:
                    // this is an error
                    break;
            }
        } // pointerEvent()

        /**
         * Called from the event delivery loop when a key event is seen.
         * @param type kind of key event - pressed, release, repeated, typed
         * @param keyCode key code of entered key
         */
        public void keyEvent(int type, int keyCode) {
            Displayable currentCopy = null;
            int eventType = -1;


            synchronized (LCDUILock) {
                if (current == null) {
                    return;
                }
                currentCopy = current;

                switch (type) {
                    case EventHandler.PRESSED:
                        sawKeyPress = true;
                        eventType = 0;
                        break;
                    case EventHandler.RELEASED:
                        if (sawKeyPress) {
                            eventType = 1;
                        }
                        break;
                    case EventHandler.REPEATED:
                        if (sawKeyPress) {
                            eventType = 2;
                        }
                        break;
                    case EventHandler.TYPED:
                        eventType = 3;
                }
                // used later by getKeyMask()
                if (currentKeyMask > -1 && eventType != -1) {
                    if (eventType == 1) {
                        releaseKeyMask(keyCode);
                    } else {
                        // set the mask on key press, repeat or type.
                        // don't set the mask when a key was released.
                        setKeyMask(keyCode);
                    }
                }
            } // synchronized

            // SYNC NOTE: Since we may call into application code,
            // we do so outside of LCDUILock
            switch (eventType) {
                case -1:
                    return;
                case 0:
                    currentCopy.callKeyPressed(keyCode);
                    break;
                case 1:
                    currentCopy.callKeyReleased(keyCode);
                    break;
                case 2:
                    currentCopy.callKeyRepeated(keyCode);
                    break;
                case 3:
                    currentCopy.callKeyTyped((char)keyCode);
                    break;
                default:
                    // this is an error
                    break;
            }
        } // keyEvent()

        /**
         * Change the current screen to the given displayable.
         *
         * @param d The Displayable to make current
         */
        public void screenChange(Displayable d) {
            synchronized (LCDUILock) {
                if (current == d && !paintSuspended) {
                    return;
                } else if (paintSuspended || !hasForeground) {
                    // If we happen to be suspended, simply update our current
                    // screen to be the new Displayable and return. When we are
                    // resumed, registerNewCurrent() will be called with the
                    // newly set screen
                    current = d;
                    return;
                }
            }

            // Its ok to ignore the foreground/paintSuspended state from here
            // on, the last thing registerNewCurrent() will do is try to do
            // a repaint(), which will not occur if the
            // foreground/paintSuspended status has changed in the meantime.
            registerNewCurrent(d, false);
        }

        /**
         * Repaint the given area of the screen
         *
         * @param x1 The x origin of the paint bounds
         * @param y1 The y origin of the paint bounds
         * @param x2 The x coordinate of the lower right bounds
         * @param y2 The y coordinate of the lower right bounds
         * @param target The optional paint target
         */
        public void repaint(int x1, int y1, int x2, int y2, Object target) {
            // Its ok to ignore the foreground/paintSuspended state from here.
            // repaint() will not succeed if the foreground/paintSuspended
            // status has changed in the meantime.
            Display.this.repaint(x1, y1, x2, y2, target);
        }

        /**
         * Process any pending call serially objects
         */
        public void callSerially() {
            // This line is no longer necessary. The event handler will
            // already have processed any pending repaints before processing
            // the callSerially() event.
            // eventHandler.serviceRepaints();
            getCallSerially();
        }

        /**
         * Perform an invalidate if the current displayable is a Form
         *
         * @param src the Item which caused the invalidation (may be null)
         */
        public void callInvalidate(Item src) {
            Display.this.callInvalidate(src);
        }

        /**
         * Notify any ItemStateChangeListener that the given Item has changed
         *
         * @param src the Item which has changed
         */
        public void callItemStateChanged(Item src) {
            Display.this.callItemStateChanged(src);
        }

        /**
         * Called from the event delivery loop when an input method
         * event is seen.
         * @param str input text string
         */
        public void inputMethodEvent(String str) {
            TextBox textBoxCopy = null;

            synchronized (LCDUILock) {
                if (current instanceof TextBox) {
                    textBoxCopy = (TextBox) current;
                }
            }

            // SYNC NOTE: TextBox.insert() does its own locking so we
            // move the call outside of our lock using a local variable
            if (textBoxCopy != null) {
                textBoxCopy.insert(str, textBoxCopy.getCaretPosition());
            }
        }

        /**
         * get the flag indicating the most recently set
         *  Displayable was non-null.
         * @return true, if most recent displayable was non-null.
         */
        public boolean wantsForeground() {
            return wantsForeground;
        }

        /**
         * get a hnadle to the current display
         * @return current Display handle.
         */
        public Display getDisplay() {
            // return display;
            return Display.this;
        }

        /**
         * Notify the display of a change in its the foreground/background
         * status.
         *
         * @param hasForeground true if the Display should be put in
         * the foreground.
         */
        public void foregroundNotify(boolean hasForeground) {
            if (hasForeground && !Display.this.hasForeground) {
                if (Scheduler.getScheduler().isDispatchThread()) {
                    // NOTE: This will have to be removed when Display
                    // changes queued in the event handler
                    eventHandler.serviceRepaints();
                }
                registerNewCurrent(current, true);
            } else {
                Displayable currentCopy = null;

                synchronized (Display.LCDUILock) {
                    Display.this.hasForeground = false;
                    paintSuspended = true;
                    currentCopy = current;
                }

                // SYNC NOTE: The implementation of callHideNotify()
                // will use LCDUILock to lock around its internal handling.
                // Canvas will override callHideNotify(), first calling
                // super(), and then obtaining calloutLock around a call
                // to the application's hideNotify() method.

                if (currentCopy != null) {
                    currentCopy.callHideNotify(Display.this);
                }

                // We want to re-set the scroll indicator when we suspend so
                // that the overtaking screen doesn't have to do it
                setVerticalScroll(0, 100);

            }
        }

        /**
         * Called to get key mask of all the keys that were pressed.
         * @return keyMask  The key mask of all the keys that were pressed.
         */
        public int getKeyMask() {
            synchronized (LCDUILock) {
                // don't release currently pressed keys
                int savedMaskCopy = stickyKeyMask | currentKeyMask;
                stickyKeyMask = 0;
                return savedMaskCopy;
            }
        }

        /**
         * Flushes the entire off-screen buffer to the display.
         * @param d The Displayable 
         * @param offscreen_buffer The image buffer 
         * @param x The left edge of the region to be flushed
         * @param y The top edge of the region to be flushed
         * @param width The width of the region to be flushed
         * @param height The height of the region to be flushed
         */
        public void flush(Displayable d, Image offscreen_buffer,
                   int x, int y, int width, int height) {

            synchronized (LCDUILock) {
                if (paintSuspended || !hasForeground || d != current) {
                    return;
                }
        
                int x2 = x + width;
                int y2 = y + height;

                screenGraphics.reset(x, y, x2, y2);
                screenGraphics.drawImage(offscreen_buffer, 
                                         x, y, 
                                         Graphics.TOP|Graphics.LEFT);
                refresh(x, y, x2, y2);
            }
        }

        /**
         * Set the trusted icon for this Display. When ever this display is in
         * the foreground the given icon will be displayed in the area reserved 
         * for the trusted icon. Only callers with the internal MIDP 
         * permission can use this method.
         *
         * @param token security token of the call that has internal MIDP
         *              permission
         * @param drawTrusted true to draw the trusted icon
         */
        public void setTrustedIcon(SecurityToken token, boolean drawTrusted) {
            
            token.checkIfPermissionAllowed(Permissions.MIDP);
            
            drawTrustedIcon(drawTrusted);
        }

        /**
         * Called to set key mask of all the keys that were pressed.
         * @param keyCode The key code to set the key mask.
         */
        private void setKeyMask(int keyCode) {
            if (paintSuspended || !hasForeground) {
                return;
            } 

            // set the mask of keys pressed 
            switch (eventHandler.getGameAction(keyCode)) {
            case Canvas.UP:
                stickyKeyMask = stickyKeyMask | GameCanvas.UP_PRESSED;
                currentKeyMask = currentKeyMask | GameCanvas.UP_PRESSED;
                break;
            case Canvas.DOWN:
                stickyKeyMask = stickyKeyMask | GameCanvas.DOWN_PRESSED;
                currentKeyMask = currentKeyMask | GameCanvas.DOWN_PRESSED;
                break;
            case Canvas.LEFT:
                stickyKeyMask = stickyKeyMask | GameCanvas.LEFT_PRESSED;
                currentKeyMask = currentKeyMask | GameCanvas.LEFT_PRESSED;
                break;
            case Canvas.RIGHT:
                stickyKeyMask = stickyKeyMask | GameCanvas.RIGHT_PRESSED;
                currentKeyMask = currentKeyMask | GameCanvas.RIGHT_PRESSED;
                break;
            case Canvas.FIRE:
                stickyKeyMask = stickyKeyMask | GameCanvas.FIRE_PRESSED;
                currentKeyMask = currentKeyMask | GameCanvas.FIRE_PRESSED;
                break;
            case Canvas.GAME_A:
                stickyKeyMask = stickyKeyMask | GameCanvas.GAME_A_PRESSED;
                currentKeyMask = currentKeyMask | GameCanvas.GAME_A_PRESSED;
                break;
            case Canvas.GAME_B:
                stickyKeyMask = stickyKeyMask | GameCanvas.GAME_B_PRESSED;
                currentKeyMask = currentKeyMask | GameCanvas.GAME_B_PRESSED;
                break;
            case Canvas.GAME_C:
                stickyKeyMask = stickyKeyMask | GameCanvas.GAME_C_PRESSED;
                currentKeyMask = currentKeyMask | GameCanvas.GAME_C_PRESSED;
                break;
            case Canvas.GAME_D:
                stickyKeyMask = stickyKeyMask | GameCanvas.GAME_D_PRESSED;
                currentKeyMask = currentKeyMask | GameCanvas.GAME_D_PRESSED;
            }
        }

        /**
         * Called to release key mask of all the keys that were release.
         * @param keyCode The key code to release the key mask.
         */
        private void releaseKeyMask(int keyCode) {

            if (paintSuspended || !hasForeground) {
                currentKeyMask = 0;
                return;
            } 
            // set the mask of keys pressed 
            switch (eventHandler.getGameAction(keyCode)) {
            case Canvas.UP:
                currentKeyMask = currentKeyMask & ~ GameCanvas.UP_PRESSED;
                break;
            case Canvas.DOWN:
                currentKeyMask = currentKeyMask & ~ GameCanvas.DOWN_PRESSED;
                break;
            case Canvas.LEFT:
                currentKeyMask = currentKeyMask & ~ GameCanvas.LEFT_PRESSED;
                break;
            case Canvas.RIGHT:
                currentKeyMask = currentKeyMask & ~ GameCanvas.RIGHT_PRESSED;
                break;
            case Canvas.FIRE:
                currentKeyMask = currentKeyMask & ~ GameCanvas.FIRE_PRESSED;
                break;
            case Canvas.GAME_A:
                currentKeyMask = currentKeyMask & ~ GameCanvas.GAME_A_PRESSED;
                break;
            case Canvas.GAME_B:
                currentKeyMask = currentKeyMask & ~ GameCanvas.GAME_B_PRESSED;
                break;
            case Canvas.GAME_C:
                currentKeyMask = currentKeyMask & ~ GameCanvas.GAME_C_PRESSED;
                break;
            case Canvas.GAME_D:
                currentKeyMask = currentKeyMask & ~ GameCanvas.GAME_D_PRESSED;
            }
        }

        /** local handle for current Display. */
        // private Display display;

        // No events will be delivered while these are false
        // This is our attempt at avoiding spurious up events
        /** true, if a pointer press is in progress. */
        boolean sawPointerPress;
        /** true, if a key press is in progress. */
        boolean sawKeyPress;


    } // END DisplayAccessor Class

/*
 * ************* Inner Class, DisplayManager
 */

    /**
     * This real display manager. It will manage
     * the list of active displays, passing any events it gets from
     * the event handler to the foreground display. It is upto the
     * scheduler to activate displays and deactivate displays,
     * however the display manager can suspend a display and resume
     * a display in response to events, it can also tell the scheduler
     * to destroy an active midlet or all midlets.
     */
    private static class DisplayManagerImpl implements DisplayManager {

        /** What should get system events. */
        private Vector systemEventListeners = new Vector(5, 5);

        /** Active displays. */
        private Vector displays = new Vector(5, 5);

        /**
         * This object is created to avoid a lot null checks.
         * It will get the system events when there is no foreground.
         */
        private final DisplayAccess noForeground = new Display(null).accessor;

        /** What should get system events. */
        private DisplayAccess foreground = noForeground;

        /** A suspended MIDlet waiting to be resumed. */
        private DisplayAccess suspended;

        /** If true all MIDlet are suspended. */
        private boolean allSuspended;

        /** The display that got preempted. */
        private DisplayAccess preempted;

        /**
         * Add a listener for system events.
         *
         * @param l object to notify with system events such as shutdown
         */
        public void addSystemEventListener(SystemEventListener l) {
            systemEventListeners.addElement(l);
        }

        /**
         * Places the display of the given MIDlet in the list of active
         * displays
         * and registers an object to receive events on behalf of the MIDlet.
         *
         * @param l object to notify with MIDlet events such as pause.
         * @param m MIDlet to activate
         */
        public void activate(MIDletEventListener l, MIDlet m) {
            Display d = getDisplay(m);
            DisplayAccess da = d.accessor;

            synchronized (displays) {
                if (displays.indexOf(da) != -1) {
                    // do not allow duplicates
                    return;
                }

                d.midletEventListener = l;
                displays.addElement(da);

                notifyWantsForeground(da, da.wantsForeground());
            }
        }

        /**
         * Removes the display of the given MIDlet from the list of active
         * displays
         * and unregisters MIDlet's event listener.
         *
         * @param m MIDlet to deactivate
         */
        public void deactivate(MIDlet m) {
            DisplayAccess da = getDisplay(m).accessor;

            synchronized (displays) {
                if (da == suspended) {
                    suspended = null;
                    return;
                }

                displays.removeElement(da);

                // there are more 2 special cases, foreground and preempted

                if (da == foreground) {
                    eventHandler.clearSystemScreen();
                    notifyWantsForeground(da, false);
                    return;
                }

                if (da == preempted) {
                    preempted = noForeground;
                    return;
                }
            }
        }

        /**
         * Preempt the current displayable with
         * the given displayable until donePreempting is called.
         * The preemptor should stop preempting when a destroyMIDlet event
         * occurs. The event will have a null MIDlet parameter.
         *
         * @param token security token for the calling class
         * @param l object to notify with the destroy MIDlet event.
         * @param d displayable to show the user
         * @param waitForDisplay if true this method will wait if the
         *        screen is being preempted by another thread.
         *
         * @return an preempt token object to pass to donePreempting done if
         * prempt will happen, else null
         *
         * @exception SecurityException if the caller does not have permission
         *   the internal MIDP permission.
         * @exception InterruptedException if another thread interrupts the
         *   calling thread while this method is waiting to preempt the
         *   display.
         */
        public Object preemptDisplay(SecurityToken token,
                MIDletEventListener l, Displayable d, boolean waitForDisplay)
                throws InterruptedException {
            Display tempDisplay;

            token.checkIfPermissionAllowed(Permissions.MIDP);

            synchronized (displays) {
                if (preempted != null || allSuspended) {

                    if (!waitForDisplay) {
                        return null;
                    }

                    displays.wait();
                }
            
                tempDisplay = new Display(null);
                tempDisplay.setCurrent(d);
                tempDisplay.midletEventListener = l;

                foreground.foregroundNotify(false);
                preempted = foreground;
                foreground = tempDisplay.accessor;
                foreground.foregroundNotify(true);

                // reuse one of our private object as the preempt token
                return displays;
            }
        }

        /**
         * Display the displayable that was being displayed before
         * preemptDisplay was called.
         *
         * @param preemptToken the token returned from preemptDisplay
         */
        public void donePreempting(Object preemptToken) {
            // only trusted callers are given a private object
            if (preemptToken != displays) {
                return;
            }

            synchronized (displays) {
                foreground.foregroundNotify(false);

                if (!allSuspended) {
                    if (preempted.wantsForeground()) {
                        foreground = preempted;
                        foreground.foregroundNotify(true);
                    } else {
                        /*
                         * force notifyWantsForeground to search the list
                         * temp no
                         * current. Start with noForeground since the preempted
                         * current
                         * display already got a foregroundNotify(false)
                         * when preempted.
                         */
                        foreground = noForeground;
                        notifyWantsForeground(noForeground, false);
                    }

                    // Another thread may be waiting to preempt
                    displays.notify();
                }

                preempted = null;
            }
        }

        /**
         * Release the system event listener set during the call to
         * getDisplayManger.
         *
         * @param l object that was being notified with system events
         */
        public void releaseSystemEventListener(SystemEventListener l) {
            systemEventListeners.removeElement(l);
        }

        /**
         * Suspend Pause all to allow the system to use the
         * display. This will result in calling pauseApp() on the
         * all of the active MIDlets. A subsequent 'resumeAll()' call will
         * return the all of the paused midlets to active status.
         * <p>
         * Called by the system event handler within Display.
         */
        public void suspendAll() {
            synchronized (displays) {
                if (allSuspended) {
                    return;
                }

                allSuspended = true;

                foreground.foregroundNotify(false);

                for (int i = 0; i < displays.size(); i++) {
                    Display d =
                        ((DisplayAccess)displays.elementAt(i)).getDisplay();
                    d.midletEventListener.pauseMIDlet(d.getMIDlet());
                }

                if (suspended != null) {
                    displays.addElement(suspended);
                    suspended = null;
                }
            }
        }

        /**
         * Resume the currently suspended state. This is a result
         * of the underlying system returning control to MIDP.
         * Any previously paused foreground MIDlet will be restarted
         * and the Display will be refreshed.
         * <p>
         * Called by the system event handler within Display.
         * <p>
         */
        public void resumeAll() {
            synchronized (displays) {
                allSuspended = false;

                for (int i = 0; i < displays.size(); i++) {
                    DisplayAccess da = (DisplayAccess)displays.elementAt(i);
                    Display d = da.getDisplay();

                    d.midletEventListener.startMIDlet(d.getMIDlet());
                }

                if (foreground.wantsForeground()) {
                    foreground.foregroundNotify(true);
                    return;
                }

                /*
                 * force notifyWantsForeground to search the list
                 * temp no current. Start with noForeground since the previous
                 * foreground display already got a foregroundNotify(false)
                 * when suspened.
                 */
                foreground = noForeground;
                notifyWantsForeground(foreground, false);

                // Another thread may be waiting to preempt the display
                displays.notify();
            }
        }

        /**
         * Shutdown all running MIDlets and prepare the MIDP runtime
         * to exit completely.
         */
        public void shutdown() {
            SystemEventListener l;

            synchronized (systemEventListeners) {
                for (int i = 0; i < systemEventListeners.size(); i++) {
                     l = (SystemEventListener)
                             systemEventListeners.elementAt(i);
                     l.shutdown();
                }
            }
        }

        /**
         * Suspend the current foreground MIDlet and return to the
         * AMS or "selector" to possibly run another MIDlet in the
         * currently active suite. Currently, the RI does not support
         * running multiple MIDlets, but if it did, this system
         * callback would allow it.
         * <p>
         * Called by the system event handler within Display.
         */
        public void suspendCurrent() {
            Display d;

            synchronized (displays) {
                if (foreground == noForeground || suspended != null ||
                       allSuspended || preempted != null) {
                    return;
                }

                d = foreground.getDisplay();
                d.midletEventListener.pauseMIDlet(d.getMIDlet());
                suspended = foreground;
                deactivate(d.getMIDlet());
            }
        }
 
        /**
         * Resume the currently suspended state. This is a result
         * of the underlying system returning control to MIDP.
         * Any previously paused foreground MIDlet will be restarted
         * and the Display will be refreshed.
         * <p>
         * Called by the system event handler within Display.
         */
        public void resumePrevious() {
            Display d;

            synchronized (displays) {
                if (suspended == null) {
                    return;
                }

                d = suspended.getDisplay();

                d.midletEventListener.startMIDlet(d.getMIDlet());

                displays.addElement(suspended);

                if (suspended.wantsForeground()) {
                    foreground.foregroundNotify(false);
                    foreground = suspended;
                    foreground.foregroundNotify(true);
                }

                suspended = null;
            }
        }

        /**
         * Kill the current foreground MIDlet and return to the
         * AMS or "selector" to possibly run another MIDlet in the
         * currently active suite. This is a system callback which
         * allows a user to forcibly exit a running MIDlet in cases
         * where it is necessary (such as a rogue MIDlet which does
         * not provide exit capability).
         * <p>
         * Called by the system event handler within Display.
         */
        public void killCurrent() {
            Display d;

            synchronized (displays) {
                if (foreground == noForeground) {
                    // There is no current, all MIDlets are paused
                    return;
                }

                d = foreground.getDisplay();
                d.midletEventListener.destroyMIDlet(d.getMIDlet());
            }
        }

        /**
         * Called by event delivery when an  Command is fired.
         * The parameter is an index into the list of Commands that are
         * current, i.e. those associated with the visible Screen.
         * @param id The integer id of the Command that fired (as returned
         *           by Command.getID())
         */
        public void commandAction(int id) {
            foreground.commandAction(id);
        }

        /**
         * Called by event delivery when a pen event is processed.
         * The type is one of EventHandler.PRESSED, EventHandler.RELEASED,
         * or EventHandler.DRAGGED.
         * @param type The type of event (press, release or drag)
         * @param x The x coordinate of the location of the pen     
         * @param y The y coordinate of the location of the pen     
         */
        public void pointerEvent(int type, int x, int y) {
            foreground.pointerEvent(type, x, y);
        }

        /**
         * Called by event delivery when a key event is processed.
         * The type is one of EventHandler.PRESSED, EventHandler.RELEASED,
         * or EventHandler.REPEATED.
         * @param type The type of event (press, release or repeat)
         * @param keyCode The key code for the key that registered the event
         */
        public void keyEvent(int type, int keyCode) {
            foreground.keyEvent(type, keyCode);
        }
        
        /**
         * Called by event delivery when a screen change needs to occur
         *
         * @param parent parent Display of the Displayable
         * @param screen The Displayable to make current in the Display
         */
        public void screenChange(Display parent, Displayable screen) {
            parent.screenChange(screen);
        }

        /**
        * Initializes the security token for this class, so it can
        * perform actions that a normal MIDlet Suite cannot.
        *
        * @param token security token for this class.
        */
        public void initSecurityToken(SecurityToken token) {
            Display.initSecurityToken(token);
        }

        /**
         * Create a display and return its internal access object.
         *
         * @param token security token for the calling class
         * @param midlet MIDlet that will own this display
         *
         * @return new display's access object
         *
         * @exception SecurityException if the caller does not have permission
         *   the internal MIDP permission.
         */
        public DisplayAccess createDisplay(SecurityToken token,
                                           MIDlet midlet) {
            token.checkIfPermissionAllowed(Permissions.MIDP);

            return new Display(midlet).accessor;
        }

        /**
         * Get the Image of the trusted icon for this Display.
         * Only callers with the internal MIDP permission can use this method.
         *
         * @return an Image of the trusted icon.
         */
        public Image getTrustedMIDletIcon() {
            MIDletSuite suite = Scheduler.getScheduler().getMIDletSuite();

            if (suite != null) {
                suite.checkIfPermissionAllowed(Permissions.MIDP);
            }
            
            return ImmutableImage.createIcon("trustedmidlet_icon.png");
        }

        /**
         * Called by event delivery when a repaint should occur
         *
         * @param x1 The origin x coordinate of the repaint region
         * @param y1 The origin y coordinate of the repaint region
         * @param x2 The bounding x coordinate of the repaint region
         * @param y2 The bounding y coordinate of the repaint region
         * @param target The optional paint target
         */
        public void repaint(int x1, int y1, int x2, int y2, Object target) {
            foreground.repaint(x1, y1, x2, y2, target);
        }

        /**
         * Called by event delivery to batch process all pending serial
         * callbacks
         */
        public void callSerially() {
            foreground.callSerially();
        }

        /**
         * Called by event delivery to process a Form invalidation
         * possibly caused by a specific item
         *
         * @param item the Item which caused the invalidation
         */
        public void callInvalidate(Item item) {
            foreground.callInvalidate(item);
        }

        /**
         * Called by event delivery to process an Item state change
         *
         * @param item the Item which has changed state
         */
        public void callItemStateChanged(Item item) {
            foreground.callItemStateChanged(item);
        }

        /**
         * Called when the system needs to temporarily prevent the application
         * from painting the screen.  The primary use of this method is to
         * allow
         * a system service to temporarily utilize the screen, e.g. to provide
         * input method or  command processing.
         *
         * This method should prevent application-based paints (i.e. those
         * generated by Canvas.repaint(), Canvas.serviceRepaints() or some
         * internal paint method on another kind of Displayable) from changing
         * the contents of the screen in any way.
         */
        public void suspendPainting() {
            foreground.suspendPainting();
        }

        /**
         * Called when the system is ready to give up its control over the
         * screen.  The application should receive a request for a full
         * repaint when this is called, and is subsequently free to process
         * paint events from Canvas.repaint(), Canvas.serviceRepaints() or
         * internal paint methods on Displayable.
         */
        public void resumePainting() {
            foreground.resumePainting();
        }

        /**
         * Called by event delivery when an input method event is processed.
         * @param str The string from the input method.
         */
        public void inputMethodEvent(String str) {
            foreground.inputMethodEvent(str);
        }

        /**
         * Called to notify the display manager when a display
         * wants its current displayable displayed or not.
         * <p>
         * If it is the current foreground display and wantsForeground is false
         * then another MIDlet may be made foreground display.
         * If it is not the foreground display by wantsForeground is true,
         * it will get the display if there are no other displays that
         * that want the display (wantsForeground is true).
         *
         * @param display the Display being  changed
         * @param wantsForeground is true if the Displable is being set to
         *        non-null, <code>false</code> otherwise.
         */
        private synchronized void notifyWantsForeground(DisplayAccess display,
                boolean wantsForeground) {
            DisplayAccess newForeground;

            synchronized (displays) {
                if (allSuspended ||
                       (display == foreground && wantsForeground) ||
                       (display != foreground && (!wantsForeground ||
                           foreground.wantsForeground()))) {
                    // no need to change the foreground
                    return;
                }

                /*
                 * Start with no foreground and then find a display
                 * this is done because timimg issues, the display may have
                 * been removed from the list by the scheduler, while
                 * the MIDlet called setCurrent.
                 */
                newForeground = noForeground;

                for (int i = 0; i < displays.size(); i++) {
                    DisplayAccess temp = (DisplayAccess)displays.elementAt(i);

                    if (temp.getDisplay().getCurrent() != null) {
                        // this display is a candidate for the foreground
                        newForeground = temp;
                    }

                    if (temp.wantsForeground()) {
                        // this display wants the foreground
                        newForeground = temp;
                        break;
                    }
                }

                foreground.foregroundNotify(false);
                foreground = newForeground;
                foreground.foregroundNotify(true);
                return;
            }
        }
    }
}

/*
 * ************* Class, DeviceCaps
 */

/**
 * Current device capabilities.
 */
class DeviceCaps {
    /** horizontal width of the current device */
    int width;
    /** vertical height of the current device */
    int height;
    /** 
     * vertical height available to draw on when in normal 
     * i.e. non-fullscreen mode 
     */
    int adornedHeight;
    /** color to use when erasing pixels in the device. */
    int eraseColor;
    /** depth of the display, e.g. 8 is full color */
    int displayDepth;
    /** true, if the device supports color */
    boolean displayIsColor;
    /** true, if the device supports a pointing device. */
    boolean pointerSupported;
    /** true, if the device supports motion events. */
    boolean motionSupported;
    /** true, if the device supported repeated events. */
    boolean repeatSupported;
    /** true, if the device supports double buffering. */
    boolean isDoubleBuffered;
    /** number of alpha levels supported by this device */
    int numAlphaLevels;
    /** keyCode for up arrow */
    int keyCodeUp;
    /** keyCode for down arrow */
    int keyCodeDown;
    /** keyCode for left arrow */
    int keyCodeLeft;
    /** keyCode for right arrow */
    int keyCodeRight;
    /** keyCode for select */
    int keyCodeSelect;

    /** initialize the device capabilities. */
    DeviceCaps() {
        init();
    }
    /** 
     * native method to retreive initial settings for 
     * display capabilities
     */
    private native void init();
}
