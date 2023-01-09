/*
 * @(#)Alert.java	1.101 02/10/09 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

import java.util.Timer;
import java.util.TimerTask;

import com.sun.midp.lcdui.Resource;
import com.sun.midp.lcdui.Text;

/**
 * An alert is a screen that shows data to the user and waits for a certain
 * period of time before proceeding to the next
 * <code>Displayable</code>. An alert can
 * contain a text string and an image.
 * The intended use of <code>Alert</code> is to inform the user about
 * errors and other
 * exceptional conditions.
 *
 * <P>The application can set the alert time to be infinity with
 * <code> setTimeout(Alert.FOREVER)</code>
 * in which case the <code>Alert</code> is considered to be <em>modal</em> and
 * the implementation provide a feature that allows the
 * user to &quot;dismiss&quot; the alert, whereupon the next
 * <code>Displayable</code>
 * is displayed as if the timeout had expired immediately.</P>
 *
 * <P>If an application specifies an alert to be of a
 * timed variety <em>and</em> gives it too much content such that it must
 * scroll,
 * then it automatically becomes a modal alert.</P>
 *
 * <P> An alert may have an <code>AlertType</code> associated with it
 * to provide an indication of the nature of the alert.
 * The implementation may use this type to play an
 * appropriate sound when the <code>Alert</code> is presented to the user.
 * See {@link AlertType#playSound(javax.microedition.lcdui.Display) 
 * AlertType.playSound()}.</P>
 *
 * <P>An alert may contain an optional <code>Image</code>.  The
 * <code>Image</code> may be mutable or
 * immutable.  If the <code>Image</code> is mutable, the effect is as
 * if a snapshot of its
 * contents is taken at the time the <code>Alert</code> is constructed
 * with this <code>Image</code> and
 * when <code>setImage</code> is called with an <code>Image</code>.
 * This snapshot is used whenever the contents of the
 * <code>Alert</code> are to be
 * displayed.  Even if the application subsequently draws into the
 * <code>Image</code>, the
 * snapshot is not modified until the next call to <code>setImage</code>.  The
 * snapshot is <em>not</em> updated when the <code>Alert</code>
 * becomes current or becomes
 * visible on the display.  (This is because the application does not have
 * control over exactly when <code>Displayables</code> appear and
 * disappear from the
 * display.)</P>
 *
 * <a name="indicator"></a>
 * <h3>Activity Indicators</h3>
 * 
 * <P>An alert may contain an optional {@link Gauge} object that is used as an 
 * activity or progress indicator.  By default, an <code>Alert</code>
 * has no activity
 * indicator; one may be set with the {@link #setIndicator} method.
 * The <code>Gauge</code>
 * object used for the activity indicator must conform to all of the following 
 * restrictions:</P>
 *
 * <ul>
 * <li>it must be non-interactive;</li>
 * <li>it must not be owned by another container (<code>Alert</code>
 * or <code>Form</code>);</li>
 * <li>it must not have any <code>Commands</code>;</li>
 * <li>it must not have an <code>ItemCommandListener</code>;</li>
 * <li>it must not have a label (that is, its label must be
 * <code>null</code>;</li>
 * <li>its preferred width and height must both be unlocked; and</li>
 * <li>its layout value must be <code>LAYOUT_DEFAULT</code>.</li>
 * </ul>
 *
 * <P>It is an error for the application to attempt to use a
 * <code>Gauge</code> object that
 * violates any of these restrictions.  In addition, when the
 * <code>Gauge</code> object is
 * being used as the indicator within an <code>Alert</code>, the
 * application is prevented
 * from modifying any of these pieces of the <code>Gauge's</code> state.</P>
 *
 * <a name="commands"></a>
 * <h3>Commands and Listeners</h3>
 *
 * <P>Like the other <code>Displayable</code> classes, an
 * <code>Alert</code> can accept <code>Commands</code>, which
 * can be delivered to a <code>CommandListener</code> set by the
 * application.  The <code>Alert</code>
 * class adds some special behavior for <code>Commands</code> and listeners.</P>
 *
 * <P>When it is created, an <code>Alert</code> implicitly has the
 * special <code>Command</code>
 * {@link #DISMISS_COMMAND} present on it.  If the application adds any 
 * other <code>Commands</code> to the <code>Alert</code>,
 * <code>DISMISS_COMMAND</code> is implicitly removed.  If the
 * application removes all other <code>Commands</code>,
 * <code>DISMISS_COMMAND</code> is implicitly
 * restored.  Attempts to add or remove <code>DISMISS_COMMAND</code>
 * explicitly are
 * ignored.  Thus, there is always at least one <code>Command</code>
 * present on an <code>Alert</code>.
 * </P>
 *
 * <P>If there are two or more <code>Commands</code> present on the
 * <code>Alert</code>, it is
 * automatically turned into a modal <code>Alert</code>, and the
 * timeout value is always
 * {@link #FOREVER}.  The <code>Alert</code> remains on the display
 * until a <code>Command</code> is
 * invoked.  If the Alert has one Command (whether it is DISMISS_COMMAND or it
 * is one provided by the application), the <code>Alert</code> may have
 * the timed behavior
 * as described above.  When a timeout occurs, the effect is the same as if
 * the user had invoked the <code>Command</code> explicitly.</P>
 *
 * <P>When it is created, an <code>Alert</code> implicitly has a
 * <code>CommandListener</code> called the
 * <em>default listener</em> associated with it.  This listener may be
 * replaced by an application-provided listener through use of the {@link
 * #setCommandListener} method.  If the application removes its listener by
 * passing <code>null</code> to the <code>setCommandListener</code> method,
 * the default listener is implicitly restored.</P>
 *
 * <P>The {@link Display#setCurrent(Alert, Displayable)} method and the {@link
 * Display#setCurrent(Displayable)} method (when called with an
 * <code>Alert</code>) define
 * special behavior for automatically advancing to another
 * <code>Displayable</code> after
 * the <code>Alert</code> is dismissed.  This special behavior occurs
 * only when the default
 * listener is present on the <code>Alert</code> at the time it is
 * dismissed or when a
 * command is invoked.  If the user invokes a <code>Command</code> and
 * the default listener
 * is present, the default listener ignores the <code>Command</code>
 * and implements the
 * automatic-advance behavior.</P>
 *
 * <P>If the application has set its own <code>CommandListener</code>, the
 * automatic-advance behavior is disabled.  The listener code is responsible
 * for advancing to another <code>Displayable</code>.  When the
 * application has provided a
 * listener, <code>Commands</code> are invoked normally by passing
 * them to the listener's
 * <code>commandAction</code> method.  The <code>Command</code> passed
 * will be one of the
 * <code>Commands</code> present on the <code>Alert</code>: either
 * <code>DISMISS_COMMAND</code> or one of the
 * application-provided <code>Commands</code>.</P>
 *
 * <P>The application can restore the default listener by passing
 * <code>null</code> to the <code>setCommandListener</code> method.</P>
 * 
 * <P>
 * <strong>Note:</strong> An application may set a {@link Ticker Ticker}
 * with {@link Displayable#setTicker Displayable.setTicker} on an
 * <code>Alert</code>, however it may not be displayed due to
 * implementation restrictions.
 * </P>
 *
 * @see AlertType
 * @since MIDP 1.0
 */

public class Alert extends Screen {

// *****************************************************
//  Public members
// *****************************************************

    /**
     * <code>FOREVER</code> indicates that an <code>Alert</code> is
     * kept visible until the user
     * dismisses it.  It is used as a value for the parameter to
     * {@link #setTimeout(int) setTimeout()}
     * to indicate that the alert is modal.  Instead of waiting for a
     * specified period of time, a modal <code>Alert</code> will wait
     * for the user to take
     * some explicit action, such as pressing a button, before proceeding to
     * the next <code>Displayable</code>.
     *
     * <P>Value <code>-2</code> is assigned to <code>FOREVER</code>.</P>
     */
    public final static int FOREVER = -2;

    /**
     * A <code>Command</code> delivered to a listener to indicate that
     * the <code>Alert</code> has been
     * dismissed.  This Command is implicitly present an on
     * <code>Alert</code> whenever
     * there are no other Commands present.  The field values of
     * <code>DISMISS_COMMAND</code> are as follows:
     *
     * <ul>
     * <li>label = &quot;&quot; (an empty string)</li>
     * <li>type = Command.OK</li>
     * <li>priority = 0</li>
     * </ul>
     *
     * <p>The label value visible to the application must be as specified 
     * above.  However, the implementation may display
     * <code>DISMISS_COMMAND</code> to the
     * user using an implementation-specific label.</p>
     *
     * <p>Attempting to add or remove <code>DISMISS_COMMAND</code>
     * from an <code>Alert</code> has no
     * effect.  However, <code>DISMISS_COMMAND</code> is treated as an
     * ordinary <code>Command</code> if
     * it is used with other <code>Displayable</code> types.</p>
     *
     * @since MIDP 2.0
     */
    public final static Command DISMISS_COMMAND =
        new Command("", Command.OK, 0);

// *****************************************************
//  Constructor(s)
// *****************************************************

    /**
     * Constructs a new, empty <code>Alert</code> object with the
     * given title. If <code>null</code> is
     * passed, the <code>Alert</code> will have no title.  Calling
     * this constructor is
     * equivalent to calling
     *
     * <pre>
     *    <code>Alert(title, null, null, null)</code>
     * </pre>
     *
     * @param title the title string, or <code>null</code>
     *
     * @see #Alert(String, String, Image, AlertType)
     */
    public Alert(String title) {
        this(title, null, null, null);
    }

    /**
     * Constructs a new <code>Alert</code> object with the given title,
     * content
     * string and image, and alert type.
     * The layout of the contents is implementation dependent.
     * The timeout value of this new alert is the same value that is
     * returned by <code>getDefaultTimeout()</code>.
     * The <code>Image</code> provided may either be mutable or immutable.
     * The handling and behavior of specific <code>AlertTypes</code>
     * is described in
     * {@link AlertType}.  <code>null</code> is allowed as the value
     * of the <code>alertType</code>
     * parameter and indicates that the <code>Alert</code> is not to
     * have a specific alert
     * type.  <code>DISMISS_COMMAND</code> is the only
     * <code>Command</code> present on the new
     * <code>Alert</code>.  The <code>CommandListener</code>
     * associated with the new <code>Alert</code> is the
     * <em>default listener</em>.  Its behavior is described in more detail in 
     * the section <a href="#commands">Commands and Listeners</a>.
     *
     * @param title the title string, or <code>null</code> if there is no title
     * @param alertText the string contents, or <code>null</code> if there 
     * is no string
     * @param alertImage the image contents, or <code>null</code> if there 
     * is no image
     * @param alertType the type of the <code>Alert</code>, or
     * <code>null</code>
     * if the <code>Alert</code> has no
     * specific type
     */
    public Alert(String title, String alertText,
                 Image alertImage, AlertType alertType) {

        super(title);

        synchronized (Display.LCDUILock) {
            this.time = DEFAULT_TIMEOUT;
            this.text = alertText;
            this.type = alertType;

            setImageImpl(alertImage);
        }
        layout();
    }

// *****************************************************
//  Public methods
// *****************************************************

    /**
     * Gets the default time for showing an <code>Alert</code>.  This
     * is either a
     * positive value, which indicates a time in milliseconds, or the special
     * value
     * {@link #FOREVER FOREVER},
     * which indicates that <code>Alerts</code> are modal by default.  The
     * value returned will vary across implementations and is presumably
     * tailored to be suitable for each.
     *
     * @return default timeout in milliseconds, or <code>FOREVER</code>
     */
    public int getDefaultTimeout() {
        synchronized (Display.LCDUILock) {
            return DEFAULT_TIMEOUT;
        }
    }

    /**
     * Gets the time this <code>Alert</code> will be shown.  This is
     * either a positive
     * value, which indicates a time in milliseconds, or the special value
     * <code>FOREVER</code>, which indicates that this
     * <code>Alert</code> is modal.  This value is not
     * necessarily the same value that might have been set by the
     * application
     * in a call to {@link #setTimeout}.  In particular, if the
     * <code>Alert</code> is made
     * modal because its contents is large enough to scroll, the value
     * returned by <code>getTimeout</code> will be <code>FOREVER</code>.
     *
     * @return timeout in milliseconds, or <code>FOREVER</code>
     * @see #setTimeout
     */
    public int getTimeout() {
        synchronized (Display.LCDUILock) {
            if ((height > viewport[HEIGHT]) || (getCommandCount() > 1)) {
                return FOREVER;
            } else {
                return time;
            }
        }
    }

    /**
     * Set the time for which the <code>Alert</code> is to be shown.
     * This must either
     * be a positive time value in milliseconds, or the special value
     * <code>FOREVER</code>.
     *
     * @param time timeout in milliseconds, or <code>FOREVER</code>
     * @throws IllegalArgumentException if time is not positive and is
     * not <code>FOREVER</code>
     * @see #getTimeout
     */
    public void setTimeout(int time) {
        if (time <= 0 && time != FOREVER) {
            throw new IllegalArgumentException();
        }

        synchronized (Display.LCDUILock) {
            this.time = time;
        }
    }

    /**
     * Gets the type of the <code>Alert</code>.
     * @return a reference to an instance of <code>AlertType</code>,
     * or <code>null</code>
     * if the <code>Alert</code>
     * has no specific type
     * @see #setType
     */
    public AlertType getType() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return type;
    }

    /**
     * Sets the type of the <code>Alert</code>.
     * The handling and behavior of specific <code>AlertTypes</code>
     * is described in
     * {@link AlertType}.
     * @param type an <code>AlertType</code>, or <code>null</code> if the
     * <code>Alert</code> has no
     * specific type
     * @see #getType
     */
    public void setType(AlertType type) {
        synchronized (Display.LCDUILock) {
            this.type = type;
        }
    }

    /**
     * Gets the text string used in the <code>Alert</code>.
     * @return the <code>Alert's</code> text string, or <code>null</code> 
     * if there is no text
     * @see #setString
     */
    public String getString() {
        // SYNC NOTE: no locking necessary
        return text;
    }

    /**
     * Sets the text string used in the <code>Alert</code>.
     *
     * <p>If the <code>Alert</code> is visible on the display when its
     * contents are updated
     * through a call to <code>setString</code>, the display will be
     * updated with the new
     * contents as soon as it is feasible for the implementation to do so.
     * </p>
     *
     * @param str the <code>Alert's</code> text string, or <code>null</code>
     * if there is no text
     * @see #getString
     */
    public void setString(String str) {
        synchronized (Display.LCDUILock) {
            text = str;
            layout();
            if (isShown()) {
                repaintContents();
            }
        }
    }

    /**
     * Gets the <code>Image</code> used in the <code>Alert</code>.
     * @return the <code>Alert's</code> image, or <code>null</code> 
     * if there is no image
     * @see #setImage
     */
    public Image getImage() {
        synchronized (Display.LCDUILock) {
            if (mutableImage != null) {
                return mutableImage;
            } else {
                return image;
            }
        }
    }

    /**
     * Sets the <code>Image</code> used in the <code>Alert</code>.
     * The <code>Image</code> may be mutable or
     * immutable.  If <code>img</code> is <code>null</code>, specifies
     * that this <code>Alert</code> has no image.
     * If <code>img</code> is mutable, the effect is as if a snapshot is taken
     * of <code>img's</code> contents immediately prior to the call to
     * <code>setImage</code>.  This
     * snapshot is used whenever the contents of the
     * <code>Alert</code> are to be
     * displayed.  If <code>img</code> is already the
     * <code>Image</code> of this <code>Alert</code>, the effect
     * is as if a new snapshot of img's contents is taken.  Thus, after
     * painting into a mutable image contained by an <code>Alert</code>, the
     * application can call
     *
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *    alert.setImage(alert.getImage());    </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * <p>to refresh the <code>Alert's</code> snapshot of its
     * <code>Image</code>.</p>
     *
     * <p>If the <code>Alert</code> is visible on the display when its
     * contents are updated
     * through a call to <code>setImage</code>, the display will be
     * updated with the new
     * snapshot as soon as it is feasible for the implementation to do so.
     * </p>
     *
     * @param img the <code>Alert's</code> image, or <code>null</code>
     * if there is no image
     * @see #getImage
     */
    public void setImage(Image img) {
        synchronized (Display.LCDUILock) {
            setImageImpl(img);
            layout();
            if (isShown()) {
                repaintContents();
            }
        }
    }

    /**
     * Sets an activity indicator on this <code>Alert</code>.  The
     * activity indicator is a
     * {@link Gauge} object.  It must be in a restricted state in order for it
     * to be used as the activity indicator for an <code>Alert</code>.
     * The restrictions
     * are listed <a href="#indicator">above</a>.  If the
     * <code>Gauge</code> object
     * violates any of these restrictions,
     * <code>IllegalArgumentException</code> is thrown.
     *
     * <p>If <code>indicator</code> is <code>null</code>, this removes any
     * activity indicator present on this <code>Alert</code>.</p>
     *
     * @param indicator the activity indicator for this <code>Alert</code>,
     * or <code>null</code> if 
     * there is to be none
     *
     * @throws IllegalArgumentException if <code>indicator</code> does not
     * meet the restrictions for its use in an <code>Alert</code>
     * @see #getIndicator
     * @since MIDP 2.0
     */
    public void setIndicator(Gauge indicator) {
        synchronized (Display.LCDUILock) {
            if (indicator == null) {
                if (this.indicator != null) {
                    // The Alert no longer owns this Gauge
                    this.indicator.setOwner(null);
                }
            } else {
                if (!isConformantIndicator(indicator)) {
                    throw new IllegalArgumentException("Gauge in wrong state");
                }
                indicator.setOwner(this);
            }

            if (this.indicator != null) {
                this.indicator.setOwner(null);
            }

            this.indicator = indicator;
            layout();
            if (isShown()) {
                repaintContents();
            }
        }
    }

    /**
     * Gets the activity indicator for this <code>Alert</code>.
     * 
     * @return a reference to this <code>Alert's</code> activity indicator,
     * or <code>null</code> if 
     * there is none
     * @see #setIndicator
     * @since MIDP 2.0
     */
    public Gauge getIndicator() {
        // SYNC NOTE: no locking necessary
        return indicator;
    }

    /**
     * Similar to {@link Displayable#addCommand}, however when the
     * application first adds a command to an <code>Alert</code>,
     * {@link #DISMISS_COMMAND} is implicitly removed.  Calling this
     * method with <code>DISMISS_COMMAND</code> as the parameter has
     * no effect.
     *
     * @param cmd the command to be added
     *
     * @throws NullPointerException if cmd is <code>null</code>
     */
    public void addCommand(Command cmd) {
        if (cmd == null) {
            throw new NullPointerException();
        }

        if (cmd == DISMISS_COMMAND) {
            return;
        }

        synchronized (Display.LCDUILock) {
            super.addCommandImpl(cmd);
        }
    }

    /**
     * Similar to {@link Displayable#removeCommand}, however when the
     * application removes the last command from an
     * <code>Alert</code>, {@link #DISMISS_COMMAND} is implicitly
     * added.  Calling this method with <code>DISMISS_COMMAND</code>
     * as the parameter has no effect.
     *
     * @param cmd the command to be removed
     */
    public void removeCommand(Command cmd) {
        if (cmd == DISMISS_COMMAND) {
            return;
        }

        synchronized (Display.LCDUILock) {
            super.removeCommandImpl(cmd);
        }
    }

    /**
     * The same as {@link Displayable#setCommandListener} but with the 
     * following additional semantics.  If the listener parameter is
     * <code>null</code>, the <em>default listener</em> is restored.
     * See <a href="#commands">Commands and Listeners</a> for the definition 
     * of the behavior of the default listener.
     *
     * @param l the new listener, or <code>null</code>
     */
    public void setCommandListener(CommandListener l) {
        synchronized (Display.LCDUILock) {
            userCommandListener = l;
        }
    }

// *****************************************************
//  Package private methods
// *****************************************************

    /**
     * Special CommandListener instance to handle execution of
     * the default "OK" Command
     */
    CommandListener implicitListener = new CommandListener() {
        /**
         * Handle the execution of the given Command and Displayable.
         *
         * @param c The Command to execute
         * @param s The Displayable from which the Command originated
         */
        public void commandAction(Command c, Displayable s) {
            // SYNC NOTE: We are protected by the calloutLock obtained
            //  previously. (either in Display or the timeout task)
            //  We do not need to re-aquire the calloutLock when
            //  calling the application's command listener.
            if (userCommandListener != null) {
                // Application has set its own listener
                if (c == OK) {
                    c = DISMISS_COMMAND; // translate 'OK' to 'DISMISS_COMMAND'
                }
                userCommandListener.commandAction(c, s);
            } else {
                // Treat all commands as if they were 'DISMISS'
                synchronized (Display.LCDUILock) {
                    currentDisplay.clearAlert(returnScreen);
                }
            }

        }
    };

    /**
     * Notify this Alert that is being displayed on the
     * given Display and wether it needs to initialize its
     * highlight
     *
     * @param d The Display this Alert will be shown on
     */
    void callShowNotify(Display d) {
        super.callShowNotify(d);

        if (type != null) {
            d.playAlertSound(type);
        }

        layout();

        int timeout = getTimeout();
        if (timeout == FOREVER) {
            if (getCommandCount() == 0) {
                // Add implicit command
                super.addCommandImpl(OK);
            }
        } else {
            if (timeoutTimer == null) {
                timeoutTimer = new Timer();
            }
            timerTask = new timeoutTask(this);
            timeoutTimer.schedule(timerTask, timeout);
        }
        super.setCommandListener(implicitListener);
    }

    /**
     * Notify this Alert that it will no longer be displayed
     * on the given Display
     *
     * @param d The Display showing this Alert
     */
    void callHideNotify(Display d) {
        super.callHideNotify(d);
        super.removeCommandImpl(OK);
        super.setCommandListener(null);
        if (timerTask != null) {
            try {
                timerTask.cancel();
                timerTask = null;
            } catch (Throwable t) { }
        }
    }

    /**
     * Set the Image for this Alert.
     *
     * @param img The img to use for this Alert
     */
    void setImageImpl(Image img) {
        if (img != null && img.isMutable()) {
            this.image = Image.createImage(img);   // use immutable copy of img
            this.mutableImage = img;
        } else { 
            this.image = img;
            this.mutableImage = null;        // Make sure to clear mutableImage
        }
    }

    /**
     * Paint the contents of this Alert given the graphics context.
     *
     * @param g The Graphics object to paint this Alert to
     * @param target the target Object of this repaint
     */
    void callPaint(Graphics g, Object target) {
        super.callPaint(g, target);

        // Vertically center alert content if the alert height
        // is less than the view port height.
        int yOffset = (viewport[HEIGHT] - height) / 2;
        if (yOffset < 0) {
            yOffset = 0;
        }
        int xOffset;

        // Translate into screen coordinates
        yOffset += viewport[Y] - view[Y];
        xOffset  = viewport[X] - view[X];

        // center and paint the image
        if (image != null) {
            int tX = (viewport[WIDTH] - image.getWidth()) / 2;
            if (tX < 0) {
                tX = 0;
            }
            tX += xOffset;

            g.translate(tX, yOffset);

            g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);

            g.translate(-tX, -yOffset);

            // amount of space between Image and next component
            yOffset += image.getHeight() + CELL_SPACING;
        }

        // Paint the activity indicator
        if (indicator != null) {
            // We do not need to center the activity indicator.
            // That has already been done in the layout() method.
            int tX = indicator.bounds[X] + xOffset;

            g.translate(tX, yOffset);

            indicator.callPaint(g, viewport[WIDTH], viewport[HEIGHT]);

            g.translate(-tX, -yOffset);

            // amount of space between indicator and next component
            yOffset += indicator.bounds[HEIGHT] + CELL_SPACING;
        }

        // paint the text
        if (text != null) {
            g.translate(xOffset, yOffset);

            Text.paint(text, Screen.CONTENT_FONT, g, viewport[WIDTH] - xOffset,
                       (viewport[HEIGHT] + viewport[Y]) - yOffset, 0, 
                       Text.NORMAL, null);

            g.translate(-xOffset, -yOffset);
        }

        setVerticalScroll();
    }

    /**
     * Layout the content of this Alert given the width and
     * height parameters
     */
    void layout() {
        super.layout();

        // height of image, if any
        height = (image != null) ? image.getHeight() + CELL_SPACING : 0;

        // height of activity indicator, if any
        if (indicator != null) {
            if (indicator.bounds == null) {
                indicator.bounds = new int[4];
            }

            int pW      = indicator.callPreferredWidth(-1);
            int pH      = indicator.callPreferredHeight(-1);

            // Center indicator horizontally
            int xOffset = (viewport[WIDTH] - pW) / 2;
            if (xOffset < 0) {
                xOffset = 0;
            }

            // We assign the item a bounds which is its pixel location,
            // width, and height in coordinates which represent offsets
            // of the viewport origin (that is, are in the viewport
            // coordinate space)
            indicator.bounds[X]      = xOffset;
            indicator.bounds[Y]      = height;
            indicator.bounds[WIDTH]  = pW;
            indicator.bounds[HEIGHT] = pH;

            height += pH + CELL_SPACING;
        }

        // height of alert text, if any
        height += Text.getHeightForWidth(text, Screen.CONTENT_FONT, 
                                         viewport[WIDTH], 0);

        // Adjust indicator if the Alert is shorter than the screen
        if ((indicator != null) && (height < viewport[HEIGHT])) {
            // Shift bounds to vertically center indicator on the screen
            indicator.bounds[Y] += (viewport[HEIGHT] - height) / 2;
        }

        view[HEIGHT] = height;
    }

    /**
     * Set the screen that the display should return to
     * when this Alert is dismissed.
     *
     * @param d The Displayable to display when this Alert is completed
     */
    void setReturnScreen(Displayable d) {
        if (d != null) {
            this.returnScreen = d;
        } else {
            this.returnScreen = new Form("");
        }
    }

    /**
     * Get the screen the display should return to
     * when this Alert is dismissed.
     *
     * @return The Displayable to display when this Alert is completed
     */
    Displayable getReturnScreen() {
        return returnScreen;
    }

    /**
     * Verify the activity indicator is conformant with the spec
     * requirements for addition to an Alert.
     *
     * @param ind The indicator to be verified
     * @return boolean True if the gauge is conformant; false otherwise
     */
    boolean isConformantIndicator(Gauge ind) {
        return ((ind.isInteractive() == false) &&
                (ind.getOwner() == null) &&
                (ind.getCommandCount() == 0) &&
                (ind.getItemCommandListener() == null) &&
                (ind.getLabel() == null) &&
                (ind.getLayout() == Item.LAYOUT_DEFAULT) &&
                (ind.lockedWidth == -1) &&
                (ind.lockedHeight == -1));
    }

    /**
     * Handle a key press
     *
     * @param keyCode the key which was pressed
     */
    void callKeyPressed(int keyCode) {
        int gameAction = Display.getGameAction(keyCode);
        switch (gameAction) {
            case Canvas.UP:
            case Canvas.DOWN:
                if (scrollViewport(gameAction)) {
                    repaintContents();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Handle a key repeat
     *
     * @param keyCode the key which was repeated
     */
    void callKeyRepeated(int keyCode) {
        callKeyPressed(keyCode);
    }

    /**
     * Scroll the viewport in the specified direction.
     *
     * @param dir direction to scroll the viewport.
     * @return boolean True if the viewport was scrolled
     */
    boolean scrollViewport(int dir) {
        if ((dir == Canvas.DOWN) && 
            (view[Y] + viewport[HEIGHT] < height)) {
                view[Y] += SCROLL_AMOUNT;
                return true;
        } else if ((dir == Canvas.UP) && 
                   (view[Y] > 0)) {
                view[Y] -= SCROLL_AMOUNT;
                return true;
        }

        return false;
    }

// *****************************************************
//  Package private members
// *****************************************************

// *****************************************************
//  Private members
// *****************************************************

    /**
     * Static default Command for "OK"
     */
    private static final Command OK =
        new Command(Resource.getString("Done"), Command.OK, 0);

    /**
     * Default spacing between elements of this Alert
     */
    private static final int CELL_SPACING = 6;

    /**
     * Number of pixels to scroll when using up/down keys
     */
    private static final int SCROLL_AMOUNT = 40;

    /**
     * The default timeout of all alerts
     */
    private static final int DEFAULT_TIMEOUT = 2000;

    /**
     * The type of this alert
     */
    private AlertType type;

    /**
     * The layout object for the alert text string
     */
    private String text;

    /**
     * The image of this alert
     */
    private Image image;

    /**
     * A reference to the original, mutable Image passed to setImage(). This
     * is only so getImage() can return the correct Image Object.
     */
    private Image mutableImage;

    /**
     * The activity indicator for this alert
     */
    private Gauge indicator;

    /**
     * The timeout value of this alert
     */
    private int time;

    /**
     * The overall height of this alert
     */
    private int height;

    /**
     * A Timer which serves all Alert objects to schedule
     * their timout tasks
     */
    private static Timer timeoutTimer;

    /**
     * A TimerTask which will be set to expire this Alert after
     * its timeout period has elapsed.
     */
    private TimerTask timerTask;

    /**
     * The screen which the display will return to when this Alert
     * is completed
     */
    private Displayable returnScreen;
    
    /**
     * The application's command listener
     */
    private CommandListener userCommandListener;

// *****************************************************
//  Internal Class
// *****************************************************

    /**
     * A TimerTask subclass which will notify the Display to
     * make the 'returnScreen' of this Alert the new current screen.
     */
    class timeoutTask extends TimerTask {

    /** The Alert for this timeout task */
        Displayable alert;

    /**
     * Create a new timeout task
     *
     * @param d the Alert
     */
        public timeoutTask(Displayable d) {
            super();
            alert = d;
        }

        /**
         * Simply set the Display's current screen to be this
         * Alert's return screen
         */
        public void run() {
            synchronized (Display.LCDUILock) {
                Command cmds[] = getCommands();
                Command c;
                if (cmds == null) {
                    c = Alert.DISMISS_COMMAND;
                } else {
                    c = cmds[0];
                }
                // Timer task functions as if the default
                // Command had been activated
                try {
                    synchronized (Display.calloutLock) {
                        implicitListener.commandAction(c, alert);
                    }
                } catch (Throwable thr) {
                    Display.handleThrowable(thr);
                }
            }
        }
    }

}
