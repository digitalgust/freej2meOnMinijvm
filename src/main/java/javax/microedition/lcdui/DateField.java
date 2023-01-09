/*
 * @(#)DateField.java	1.137 02/10/09 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.sun.midp.lcdui.Resource;
import com.sun.midp.lcdui.Text;

/**
 * A <code>DateField</code> is an editable component for presenting
 * date and time (calendar)
 * information that may be placed into a <code>Form</code>. Value for
 * this field can be
 * initially set or left unset. If value is not set then the UI for the field
 * shows this clearly. The field value for &quot;not initialized
 * state&quot; is not valid
 * value and <code>getDate()</code> for this state returns <code>null</code>.
 * <p>
 * Instance of a <code>DateField</code> can be configured to accept
 * date or time information
 * or both of them. This input mode configuration is done by
 * <code>DATE</code>, <code>TIME</code> or
 * <code>DATE_TIME</code> static fields of this
 * class. <code>DATE</code> input mode allows to set only
 * date information and <code>TIME</code> only time information
 * (hours, minutes). <code>DATE_TIME</code>
 * allows to set both clock time and date values.
 * <p>
 * In <code>TIME</code> input mode the date components of
 * <code>Date</code> object
 * must be set to the &quot;zero epoch&quot; value of January 1, 1970.
 * <p>
 * Calendar calculations in this field are based on default locale and defined
 * time zone. Because of the calculations and different input modes date object
 * may not contain same millisecond value when set to this field and get back
 * from this field.
 * @since MIDP 1.0
 */
public class DateField extends Item {

    /**
     * Input mode for date information (day, month, year). With this mode this
     * <code>DateField</code> presents and allows only to modify date
     * value. The time
     * information of date object is ignored.
     *
     * <P>Value <code>1</code> is assigned to <code>DATE</code>.</P>
     */
    public static final int DATE = 1;

    /**
     * Input mode for time information (hours and minutes). With this mode this
     * <code>DateField</code> presents and allows only to modify
     * time. The date components
     * should be set to the &quot;zero epoch&quot; value of January 1, 1970 and
     * should not be accessed.
     *
     * <P>Value <code>2</code> is assigned to <code>TIME</code>.</P>
     */
    public static final int TIME = 2;

    /**
     * Input mode for date (day, month, year) and time (minutes, hours)
     * information. With this mode this <code>DateField</code>
     * presents and allows to modify
     * both time and date information.
     *
     * <P>Value <code>3</code> is assigned to <code>DATE_TIME</code>.</P>
     */
    public static final int DATE_TIME = 3;

    /**
     * Creates a <code>DateField</code> object with the specified
     * label and mode. This call
     * is identical to <code>DateField(label, mode, null)</code>.
     *
     * @param label item label
     * @param mode the input mode, one of <code>DATE</code>, <code>TIME</code>
     * or <code>DATE_TIME</code>
     * @throws IllegalArgumentException if the input <code>mode's</code>
     * value is invalid
     */
    public DateField(String label, int mode) {
        this(label, mode, null);
    }

    /**
     * Creates a date field in which calendar calculations are based
     * on specific
     * <code>TimeZone</code> object and the default calendaring system for the
     * current locale.
     * The value of the <code>DateField</code> is initially in the
     * &quot;uninitialized&quot; state.
     * If <code>timeZone</code> is <code>null</code>, the system's
     * default time zone is used.
     *
     * @param label item label
     * @param mode the input mode, one of <code>DATE</code>, <code>TIME</code>
     * or <code>DATE_TIME</code>
     * @param timeZone a specific time zone, or <code>null</code> for the
     * default time zone
     * @throws IllegalArgumentException if the input <code>mode's</code> value
     * is invalid
     */
    public DateField(String label, int mode, java.util.TimeZone timeZone) {
        super(label);

        synchronized (Display.LCDUILock) {
            if ((mode != DATE) && (mode != TIME) && (mode != DATE_TIME)) {
                throw new IllegalArgumentException("Invalid input mode");
            }

            this.mode = mode;

            if (timeZone == null) {
                timeZone = TimeZone.getDefault();
            }

            this.currentDate = Calendar.getInstance(timeZone);
        } // synchronized
    }

    /**
     * Returns date value of this field. Returned value is
     * <code>null</code> if field
     * value is
     * not initialized. The date object is constructed according the rules of
     * locale specific calendaring system and defined time zone.
     *
     * In <code>TIME</code> mode field the date components are set to
     * the &quot;zero
     * epoch&quot; value of January 1, 1970. If a date object that presents time
     * beyond one day from this &quot;zero epoch&quot; then this field
     * is in &quot;not
     * initialized&quot; state and this method returns <code>null</code>.
     *
     * In <code>DATE</code> mode field the time component of the calendar is set
     * to zero when
     * constructing the date object.
     *
     * @return date object representing time or date depending on input mode
     * @see #setDate
     */
    public java.util.Date getDate() {
        synchronized (Display.LCDUILock) {
            // NOTE: 
            // defensive copy of the Date object is necessary 
            // because CLDC's Calendar returns a reference to an internal, 
            // shared Date object.  See bugID: 4479408.
            return (initialized ? 
                new java.util.Date(currentDate.getTime().getTime()) : null);

        } // synchronized
    }

    /**
     * Sets a new value for this field. <code>null</code> can be
     * passed to set the field
     * state to &quot;not initialized&quot; state. The input mode of
     * this field defines
     * what components of passed <code>Date</code> object is used.<p>
     *
     * In <code>TIME</code> input mode the date components must be set
     * to the &quot;zero
     * epoch&quot; value of January 1, 1970. If a date object that presents time
     * beyond one day then this field is in &quot;not initialized&quot; state.
     * In <code>TIME</code> input mode the date component of
     * <code>Date</code> object is ignored and time
     * component is used to precision of minutes.<p>
     *
     * In <code>DATE</code> input mode the time component of
     * <code>Date</code> object is ignored.<p>
     *
     * In <code>DATE_TIME</code> input mode the date and time
     * component of <code>Date</code> are used but
     * only to precision of minutes.
     *
     * @param date new value for this field
     * @see #getDate
     */
    public void setDate(java.util.Date date) {
        synchronized (Display.LCDUILock) {
            if (date == null) {
                initialized = false;
            } else {
                currentDate.setTime(date);

                if (mode == TIME) {
                    // NOTE:
                    // It is unclear from the spec what should happen 
                    // when DateField with TIME mode is set to
                    // a value that is on a day other than 1/1/1970.
                    //
                    // Two possible interpretations of the spec are:
                    // 1. DateField is put into the "uninitialized" state; or
                    // 2. The time portion of the DateField is set to the 
                    //    time-of-day portion of the Date object passed in, 
                    //    and the date portion of the DateField is set 
                    //    to 1/1/1970.
                    //
                    // Currently we are using the first approach.
                    initialized =
                        (currentDate.get(Calendar.YEAR)  == 1970) &&
                        (currentDate.get(Calendar.MONTH) == Calendar.JANUARY)
                        && (currentDate.get(Calendar.DATE)   == 1);
                } else {
                    // Currently spec does not prohibit from losing
                    // irrelevant for that mode information
                    // so we always zero out hours and minutes

                    // NOTE: the specification doesn't prohibit 
                    // the loss of information irrelevant to 
                    // the current input mode, so we always zero out the
                    // hours and minutes.
                    if (mode == DATE) {
                        currentDate.set(Calendar.HOUR, 0);
                        currentDate.set(Calendar.MINUTE, 0);
                    }
                    initialized = true;
                }

                // always ignore seconds and milliseconds
                currentDate.set(Calendar.SECOND, 0);
                currentDate.set(Calendar.MILLISECOND, 0);
            }
            invalidate();
        } // synchronized
    }

    /**
     * Gets input mode for this date field. Valid input modes are
     * <code>DATE</code>, <code>TIME</code> and <code>DATE_TIME</code>.
     *
     * @return input mode of this field
     * @see #setInputMode
     */
    public int getInputMode() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return mode;
    }

    /**
     * Set input mode for this date field. Valid input modes are
     * <code>DATE</code>, <code>TIME</code> and <code>DATE_TIME</code>.
     *
     * @param mode the input mode, must be one of <code>DATE</code>,
     * <code>TIME</code> or <code>DATE_TIME</code>
     * @throws IllegalArgumentException if an invalid value is specified
     * @see #getInputMode
     */
    public void setInputMode(int mode) {
        if ((mode != DATE) && (mode != TIME) && (mode != DATE_TIME)) {
            throw new IllegalArgumentException("Invalid input mode");
        }

        synchronized (Display.LCDUILock) {
            if (this.mode != mode) {
                int oldMode = this.mode;
                this.mode = mode;

                // While the input mode is changed
                // some irrelevant values for new mode could be lost. 
                // Currently that is allowed by the spec.

                // So for TIME mode we make sure that time is set
                // on a zero epoch date
                // and for DATE mode we zero out hours and minutes
                if (mode == TIME) {
                    currentDate.set(Calendar.YEAR, 1970);
                    currentDate.set(Calendar.MONTH, Calendar.JANUARY);
                    currentDate.set(Calendar.DATE, 1);
                } else if (mode == DATE) {
                    currentDate.set(Calendar.HOUR, 0);
                    currentDate.set(Calendar.MINUTE, 0);
                }
                invalidate();
            }
        } // synchronized
    }

    // package private

    /**
     * Determine if this Item should have a newline after it
     *
     * @return true if it should have a newline after
     */
    boolean equateNLA() {
        if (super.equateNLA()) {
            return true;
        }

        return ((layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }
               

    /**
     * Determine if this Item should have a newline before it
     *
     * @return true if it should have a newline before
     */
    boolean equateNLB() {
        if (super.equateNLB()) {
            return true;
        }

        return ((layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }

    /**
     * Called from the Date Editor to save the selected Date.
     *
     * @param date The Date object to which current date should be set.
     */
    void saveDate(java.util.Date date) {
        initialized = true;
        
        currentDate.setTime(date);
        invalidate();
    }

    /**
     * Get the minimum width of this Item
     *
     * @return the minimum width
     */
    int callMinimumWidth() {
        return Screen.CONTENT_FONT.stringWidth("Www,99 Www 0000") + 2;
    }

    /**
     * Get the preferred width of this Item
     *
     * @param h the tentative content height in pixels, or -1 if a
     * tentative height has not been computed
     * @return the preferred width
     */
    int callPreferredWidth(int h) {
        return callMinimumWidth();
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
        if (mode == DATE_TIME) {
            return getLabelHeight(w) +
                (Screen.CONTENT_HEIGHT * 2) + LABEL_PAD;
        } else {
            return getLabelHeight(w) + Screen.CONTENT_HEIGHT;
        }
    }

    /**
     * Paint this DateField
     *
     * @param g the Graphics object to be used for rendering the item
     * @param width current width of the item in pixels
     * @param height current height of the item in pixels
     */
    void callPaint(Graphics g, int width, int height) {

        // draw label
        int labelHeight = super.paintLabel(g, width) + LABEL_PAD;
        g.translate(0, labelHeight);

        int offset = 0;
        String str;
        switch (mode) {
            case TIME:
            case DATE_TIME:
                str = toString(TIME);
                if (highlight == 0 && hasFocus) {
                    g.fillRect(2, 0, Screen.CONTENT_FONT.stringWidth(str),
                               Screen.CONTENT_HEIGHT);
                    g.setColor(Display.FG_H_COLOR);
                }
                g.drawString(str, 2, 0,
                            Graphics.LEFT | Graphics.TOP);
                g.setColor(Display.FG_COLOR);
                if (mode == TIME) {
                    break;
                }
                offset = Screen.CONTENT_HEIGHT;
            case DATE:
                str = toString(DATE);
                if ((highlight == 0 && mode == DATE && hasFocus) ||
                    (highlight == 1 && mode == DATE_TIME && hasFocus)) {

                    g.fillRect(2, offset, Screen.CONTENT_FONT.stringWidth(str),
                               Screen.CONTENT_HEIGHT);
                    g.setColor(Display.FG_H_COLOR);
                }
                g.drawString(toString(DATE), 2, offset,
                            Graphics.LEFT | Graphics.TOP);
                g.setColor(Display.FG_COLOR);
        }

        g.translate(0, -labelHeight);
    }

    /**
     * Called by the system to traverse this DateField
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the container's viewport
     * @param viewportHeight the height of the container's viewport
     * @param visRect passes the visible rectangle into the method, and
     * returns the updated traversal rectangle from the method
     * @return true if internal traversal had occurred, false if traversal
     * should proceed out
     */
    boolean callTraverse(int dir, int viewportWidth, int viewportHeight,
                         int[] visRect) {

        super.callTraverse(dir, viewportWidth, viewportHeight, visRect);

        int numEls = (mode == DATE_TIME) ? 2 : 1;

        if (!traversedIn) {
            traversedIn = true;

            if (highlight == -1) {
                switch (dir) {
                    case Canvas.UP:
                        highlight = numEls - 1;
                        break;
                    case Canvas.DOWN:
                        highlight = 0;
                        break;
                    case CustomItem.NONE:
                }
            }
        } else {

            if (dir == Canvas.UP) {
                if (highlight > 0) {
                    highlight--;
                } else {
                    return false;
                }
            } else if (dir == Canvas.DOWN) {
                if (highlight < (numEls - 1)) {
                    highlight++;
                } else {
                    return false;
                }
            }
        }

        visRect[Y] = getLabelHeight(visRect[WIDTH]) + LABEL_PAD;
        if (highlight > 0) {
            visRect[Y] += Screen.CONTENT_HEIGHT;
        }
        visRect[HEIGHT] = Screen.CONTENT_HEIGHT;

        repaint();
        return true;
    }

    /**
     * Called by the system to indicate traversal has left this Item
     */
    void callTraverseOut() {
        super.callTraverseOut();

        traversedIn = false;
    }

    /**
     * Called by the system to signal a key press
     *
     * @param keyCode the key code of the key that has been pressed
     */
    void callKeyPressed(int keyCode) {
        if (keyCode != Display.KEYCODE_SELECT) {
            return;
        }

        Screen returnScreen = getOwner();

        if (editor == null) {
            editor = new EditScreen(returnScreen, this);
        }

        switch (mode) {
        case DATE:
            if (!initialized) {
                currentDate.set(Calendar.HOUR, 0);
                currentDate.set(Calendar.MINUTE, 0);
                currentDate.set(Calendar.SECOND, 0);
                currentDate.set(Calendar.MILLISECOND, 0);
            }
            editor.setDateTime(currentDate.getTime(), DATE);
            break;

        case TIME:
            editor.setDateTime(initialized ? currentDate.getTime() : EPOCH,
                               TIME);
            break;

        case DATE_TIME:
            editor.setDateTime(currentDate.getTime(),
                               (highlight < 1) ? TIME : DATE);
        }

        returnScreen.resetToTop = false;
        returnScreen.currentDisplay.setCurrent(editor);
    }

    /**
     * Get the am/pm text given a Calandar
     *
     * @param calendar The Calendar object to retrieve the time from
     * @return String The am/pm text based on the time in the calendar
     */
    static String ampmString(Calendar calendar) {
        int hour   = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (hour >= 12) {
            return ((minute == 0) && (hour == 12)) ? "noon" : "PM";
        } else {
            return ((minute == 0) && (hour == 00)) ? "mid." : "AM";
        }
    }

    /**
     * Get the day of the week text given a Calendar
     *
     * @param calendar The Calendar object to retrieve the date from
     * @return String The day of the week text based on the date in the
     *                  calendar
     */
    static String dayOfWeekString(Calendar calendar) {
        String str;
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
        case Calendar.SUNDAY:    str = "Sun"; break;
        case Calendar.MONDAY:    str = "Mon"; break;
        case Calendar.TUESDAY:   str = "Tue"; break;
        case Calendar.WEDNESDAY: str = "Wed"; break;
        case Calendar.THURSDAY:  str = "Thu"; break;
        case Calendar.FRIDAY:    str = "Fri"; break;
        case Calendar.SATURDAY:  str = "Sat"; break;
        default: 
            str = Integer.toString(calendar.get(Calendar.DAY_OF_WEEK));
        }
        return str;
    }

    /**
     * Translate the mode of a DateField into a readable string
     *
     * @param mode The mode to translate
     * @return String A human readable string representing the mode of the
     *              DateField
     */
    String toString(int mode) {
        if (mode == DATE) {
            if (!initialized) {
                    return Resource.getString("<date>");
            }

            return Resource.getDateString(
                dayOfWeekString(currentDate),
                twoDigits(currentDate.get(Calendar.DATE)),
                MONTH_NAMES[currentDate.get(Calendar.MONTH)].substring(0, 3),
                Integer.toString(currentDate.get(Calendar.YEAR)));

        } else if (mode == TIME) {
            if (!initialized) {
                    return Resource.getString("<time>");
            }

            if (CLOCK_USES_AM_PM) {
                return Resource.getTimeString(
                    twoDigits(currentDate.get(Calendar.HOUR)),
                    twoDigits(currentDate.get(Calendar.MINUTE)),
                    twoDigits(currentDate.get(Calendar.SECOND)),
                    ampmString(currentDate));
            } else {
                return Resource.getTimeString(
                    twoDigits(currentDate.get(Calendar.HOUR_OF_DAY)),
                    twoDigits(currentDate.get(Calendar.MINUTE)),
                    twoDigits(currentDate.get(Calendar.SECOND)),
                    null);
            }
        } else {
            if (!initialized) {
                    return Resource.getString("<date/time>");
            }

            return Resource.getDateTimeString(
                dayOfWeekString(currentDate),
                twoDigits(currentDate.get(Calendar.DATE)),
                MONTH_NAMES[currentDate.get(Calendar.MONTH)].substring(0, 3),
                Integer.toString(currentDate.get(Calendar.YEAR)),
                twoDigits(currentDate.get(Calendar.HOUR_OF_DAY)),
                twoDigits(currentDate.get(Calendar.MINUTE)),
                twoDigits(currentDate.get(Calendar.SECOND)),
                ampmString(currentDate));
        }
    }

    /**
     * Static zero epoch Date - used as a default value for
     * uninitialized DateFields with TIME mode.
     */
    static final java.util.Date EPOCH = new java.util.Date(0);

    // REMIND: Needs to be localized?
    /**
     * Static array holding the names of the 12 months
     */
    static final String MONTH_NAMES[] = {
        "January", "February", "March",     "April",   "May",      "June",
        "July",    "August",   "September", "October", "November", "December"
    };

    /**
     * table of trigonometric functions, in 16.16 fixed point
     */
    static final int TRIG_TABLE[] = {
        65535, // cos 0
        65525, // cos 1
        65495, // cos 2
        65445, // cos 3
        65375, // cos 4
        65285, // cos 5
        65175, // cos 6
        65046, // cos 7
        64897, // cos 8
        64728, // cos 9
        64539, // cos 10
        64330, // cos 11
        64102, // cos 12
        63855, // cos 13
        63588, // cos 14
        63301, // cos 15
        62996, // cos 16
        62671, // cos 17
        62327, // cos 18
        61964, // cos 19
        61582, // cos 20
        61182, // cos 21
        60762, // cos 22
        60325, // cos 23
        59869, // cos 24
        59394, // cos 25
        58902, // cos 26
        58392, // cos 27
        57863, // cos 28
        57318, // cos 29
        56754, // cos 30
        56174, // cos 31
        55576, // cos 32
        54962, // cos 33
        54330, // cos 34
        53683, // cos 35
        53018, // cos 36
        52338, // cos 37
        51642, // cos 38
        50930, // cos 39
        50202, // cos 40
        49459, // cos 41
        48701, // cos 42
        47929, // cos 43
        47141, // cos 44
        46340, // cos 45
        45524, // cos 46
        44694, // cos 47
        43851, // cos 48
        42994, // cos 49
        42125, // cos 50
        41242, // cos 51
        40347, // cos 52
        39439, // cos 53
        38520, // cos 54
        37589, // cos 55
        36646, // cos 56
        35692, // cos 57
        34728, // cos 58
        33753, // cos 59
        32767, // cos 60
        31771, // cos 61
        30766, // cos 62
        29752, // cos 63
        28728, // cos 64
        27696, // cos 65
        26655, // cos 66
        25606, // cos 67
        24549, // cos 68
        23485, // cos 69
        22414, // cos 70
        21336, // cos 71
        20251, // cos 72
        19160, // cos 73
        18063, // cos 74
        16961, // cos 75
        15854, // cos 76
        14742, // cos 77
        13625, // cos 78
        12504, // cos 79
        11380, // cos 80
        10251, // cos 81
        9120,  // cos 82
        7986,  // cos 83
        6850,  // cos 84
        5711,  // cos 85
        4571,  // cos 86
        3429,  // cos 87
        2287,  // cos 88
        1143,  // cos 89
        0      // cos 90
    };

    /**
     * Utility method to return the cosine of an angle
     *
     * @param angle The angle to compute the cosine of
     * @return int The cosine of the angle
     */
    static int cos(int angle) {
        angle += 360000;
        angle %= 360;

        if (angle >= 270) {
            return TRIG_TABLE[360 - angle];
        } else if (angle >= 180) {
            return -TRIG_TABLE[angle - 180];
        } else if (angle >= 90) {
            return -TRIG_TABLE[180 - angle];
        } else {
            return TRIG_TABLE[angle];
        }
    }

    /**
     * Utility method to return the sin of an angle
     *
     * @param angle The angle to compute the sin of
     * @return int The sin of the angle
     */
    static int sin(int angle) {
        return cos(angle - 90);
    }

    // private

    /**
     * A utility method to return a numerical digit as two digits
     * if it is less than 10
     *
     * @param n The number to convert
     * @return String The String representing the number in two digits
     */
    private static String twoDigits(int n) {
        if (n == 0) {
            return "00";
        } else if (n < 10) {
            return "0" + n;
        } else {
            return "" + n;
        }
    }

    /**
     * The highlight of this DateField
     */
    private int highlight = -1;

    /**
     * A flag indicating a prior call to callTraverse()
     */
    private boolean traversedIn;

    /**
     * A flag indicating the initialization state of this DateField
     */
    private boolean initialized; // = false;
    /**
     * The mode of this DateField
     */
    private int mode;
    /**
     * The editor for this DateField
     */
    private EditScreen editor = null;

    /**
     * The last saved date.
     * This is used for making the last saved date bold.
     */
    private Calendar currentDate;

    /**
     * Flag to signal the clock representation uses AM and PM notation
     */
    private static final boolean CLOCK_USES_AM_PM = true;
    /**
     * The image representing an up arrow
     */
    private static final Image ARROW_UP;
    /**
     * The image representing an down arrow
     */
    private static final Image ARROW_DOWN;
    /**
     * The image representing an left arrow
     */
    private static final Image ARROW_LEFT;
    /**
     * The image representing an right arrow
     */
    private static final Image ARROW_RIGHT;

    static {
        /*
         * Initialize the icons necessary for the date editor.
         */
        ARROW_UP    = ImmutableImage.createIcon("date_up.png");
        ARROW_DOWN  = ImmutableImage.createIcon("date_down.png");
        ARROW_LEFT  = ImmutableImage.createIcon("date_left.png");
        ARROW_RIGHT = ImmutableImage.createIcon("date_right.png");

    }

    /**
     * The EditScreen class is a special editor for a DateField.
     * It can edit both date and time.
     */
    class EditScreen extends Screen implements CommandListener {

        /**
         * The calendar holding the date/time for this editor
         */
        Calendar calendar;

        /**
         * The mode of this editor (Date, Time, Date & Time)
         */
        int mode;

        /**
         * The DateField being edited by this editor
         */
        DateField field;

        /**
         * The encapsulating Screen holding the DateField being
         * edited by this editor (allows us to return to this screen
         * when we are done editing).
         */
        Screen returnScreen;

        /**
         * The selected time element
         */
        int timeSel = 0;

        /**
         * Flag to signal whether a select action transfers focus or not
         * (false by default)
         */
        private static final boolean SELECT_TRANSFERS_FOCUS = false;

        /**
         * Special command to go "back" from the editor to the DateField
         */
        Command Back = new Command(
            Resource.getString("Back"), Command.BACK, 0);
        /**
         * Special command to "ok" the changes done in the editor
         */
        Command OK   = new Command
            (Resource.getString("Save"), Command.OK, 1);

        /**
         * Flag to format am/pm string
         */
        private boolean ampmAfterTime = Resource.isAMPMafterTime();
        /**
         * The width and height of the editor
         */
        private int width, height;

        /**
         * The initial highlight
         */
        private int highlight;
        /**
         * The last day of the month
         */
        private int lastDay;
        /**
         * The day offset
         */
        private int dayOffset;

        /**
         * Create a new EditScreen
         *
         * @param returnScreen The Screen to return to from the editor
         * @param field The DateField this EditScreen is editing
         */
        EditScreen(Screen returnScreen, DateField field) {
            super(field.getLabel());

            this.returnScreen = returnScreen;
            this.field = field;
            this.calendar = Calendar.getInstance();

            addCommand(OK);
            addCommand(Back);
            setCommandListener(this);
        }

        /**
         * Handle a command action
         *
         * @param cmd The Command to handle
         * @param s   The Displayable with the Command
         */
        public void commandAction(Command cmd, Displayable s) {
            Form form = null;
            Item item = null;

            synchronized (Display.LCDUILock) {
                if (cmd == OK) {
                    field.saveDate(calendar.getTime());
                    item = field;
                    form = (Form)item.getOwner();
                }
                currentDisplay.setCurrent(returnScreen);

            } // synchronized

            // SYNC NOTE: Move the call to the application's
            // ItemStateChangedListener outside the lock
            if (form != null) {
                form.itemStateChanged(item);
            }
        }

        /**
         * Set the current date and time
         *
         * @param currentValue The Date to set the editor to
         * @param mode The operating mode of the DateField
         */
        void setDateTime(Date currentValue, int mode) {
            calendar.setTime(currentValue);

            this.mode = mode;
            if (CLOCK_USES_AM_PM && !ampmAfterTime) {
                timeSel = Calendar.AM_PM;
            } else {
                timeSel = Calendar.HOUR;
            }
            // highlight = calendar.get(Calendar.DATE);
            highlight = -1; // highlight the year to start with

            this.callRepaint();                    // Call Screen.callRepaint()
        }

        /**
        * notify this editor it is being shown on the given Display
        *
        * @param d the Display showing this Form
        */
        void callShowNotify(Display d) {
            super.callShowNotify(d);

            layout();

            setDayOffset();
            lastDay = daysInMonth(calendar.get(Calendar.MONTH),
            calendar.get(Calendar.YEAR));
        }

        /**
         * Paint the content of this editor
         *
         * @param g The Graphics object to paint to
         * @param target the target Object of this repaint
         */
        void callPaint(Graphics g, Object target) {
            super.callPaint(g, target);

            g.translate(viewport[X], viewport[Y] + 5);

            if (mode == TIME) {
                paintClock(g);
            } else {
                paintCalendar(g);
            }

            g.translate(-viewport[X], -(viewport[Y] + 5));
        }

        /**
         * Layout the content of this editor given the width/height
         */
        void layout() {
            super.layout();

            this.width = viewport[WIDTH];
            this.height = viewport[HEIGHT];
        }

        /**
         * Initialize the highlight of this editor
         *
         * @param vpY
         * @param vpH
         * @return int Always returns 0
         */
        int initHilight(int vpY, int vpH) {
            if (mode == DATE) {
                int hilightBottom = highlightY(true);
                if (hilightBottom > vpH) {
                    return (hilightBottom - vpH);
                }
            }
            return 0;
        }

        /**
         * Handle a key press
         *
         * @param keyCode the key which was pressed
         */
        void callKeyPressed(int keyCode) {

            int gameAction = Display.getGameAction(keyCode);
            switch (gameAction) {
                case Canvas.FIRE:
                    selectFired();
                    break;
                case Canvas.UP:
                case Canvas.DOWN:
                case Canvas.LEFT:
                case Canvas.RIGHT:
                    if (mode == DATE) {
                        traverseDate(gameAction, bounds[Y],
                                    bounds[Y] + bounds[HEIGHT]);
                    } else {
                        traverseClock(gameAction, bounds[Y],
                                    bounds[Y] + bounds[HEIGHT]);
                    }
                    this.callRepaint();      // Call Screen.callRepaint()
                    break;
            }
        }

        /**
         * Handle a key repeat
         *
         * @param keyCode the key which was repeated
         */
        void callKeyRepeated(int keyCode) {
            int gameAction = Display.getGameAction(keyCode);

            switch (gameAction) {
                case Canvas.UP:
                case Canvas.DOWN:
                case Canvas.LEFT:
                case Canvas.RIGHT:
                    if (mode == DATE) {
                        traverseDate(gameAction, bounds[Y],
                                    bounds[Y] + bounds[HEIGHT]);
                    } else {
                        traverseClock(gameAction, bounds[Y],
                                    bounds[Y] + bounds[HEIGHT]);
                    }
                    this.callRepaint();       // Call Screen.callRepaint()
   
            }
        }

        /**
         * Handle a selection
         */
        void selectFired() {

            if (!SELECT_TRANSFERS_FOCUS) {
                return;
            }

            synchronized (Display.LCDUILock) {
                if (mode == DATE) {
                    if (highlight > 0) {
                        highlight = -1;
                    } else if (highlight == -1) {
                        highlight = 0;
                    } else {
                        highlight = calendar.get(Calendar.DATE);
                    }
                    if (highlight > 0) {
                        calendar.set(Calendar.DATE, highlight);
                    }
                } else {
                    if (timeSel == Calendar.MINUTE) {
                        timeSel = Calendar.HOUR;
                    } else {
                        timeSel = Calendar.MINUTE;
                    }
                }
                this.callRepaint();                // Call Screen.callRepaint()
            } // synchronized
        }

        /**
         * Paint the clock
         *
         * @param g The Graphics to paint to
         */
        void paintClock(Graphics g) {
            int hour   = calendar.get(Calendar.HOUR) % 12;
            int minute = calendar.get(Calendar.MINUTE);

            g.setColor(Display.ERASE_COLOR);
            g.fillRect(0, 0, width, height);

            int digits_height = large.getHeight() 
                              + ARROW_UP.getHeight()
                              + ARROW_DOWN.getHeight()
                              + 2;

            int clockSize = height - digits_height;
            if (width < clockSize) {
                clockSize = width;
            }
            if (60 < clockSize) {
                clockSize = 60;
            }

            // For reference, the above if statements are replacing
            // this Math() call below.
            // Math.min(60, Math.min(width, height - digits_height));

            g.translate((width - clockSize) / 2, 
                        (height - (clockSize + digits_height)) / 2);

            g.setColor(Display.FG_COLOR);

            g.drawRoundRect(0, 0, clockSize, clockSize, 
                            clockSize / 2, clockSize / 2);
            g.drawLine(clockSize / 2, 0, clockSize / 2, 5);
            g.drawLine(clockSize / 2, clockSize, 
                       clockSize / 2, clockSize - 5);
            g.drawLine(0, clockSize / 2, 5, clockSize / 2);
            g.drawLine(clockSize, clockSize / 2, 
                       clockSize - 5, clockSize / 2);

            int minuteAngle = 90 - (minute * 6);
            int hourAngle   = 90 - (hour * 30 + (minute / 2));

            g.translate(clockSize / 2, clockSize / 2);
            g.drawLine(0, 0,
                        (cos(hourAngle)*clockSize / 4) >> 16,
                       -(sin(hourAngle)*clockSize / 4) >> 16);

            g.drawLine(0, 0,
                        (cos(minuteAngle)*(clockSize / 2 - 10)) >> 16,
                       -(sin(minuteAngle)*(clockSize / 2 - 10)) >> 16);

            g.translate(0, clockSize / 2 + 2 + ARROW_UP.getHeight());

            g.setFont(large);

            if (CLOCK_USES_AM_PM) {
                String ampm = Resource.getString(ampmString(calendar));

                if (hour == 0) {
                    hour = 12;
                }

                String timeString;

                if (ampmAfterTime) {
                    timeString = 
                        twoDigits(hour) + ":" + twoDigits(minute) + " " + ampm;
                } else {
                    timeString = 
                        ampm + " " + twoDigits(hour) + ":" + twoDigits(minute);
                }

                g.translate(-large.stringWidth(timeString) / 2, 0);

                g.drawString(timeString,
                             0, 0, Graphics.LEFT | Graphics.TOP);

                int dX;
                int w;
                int h = large.getBaselinePosition() + 1;
                int offset;
                int len;

                if (ampmAfterTime) {
                    if (timeSel == Calendar.HOUR) {
                        offset = 0;
                        len = 2;
                    } else if (timeSel == Calendar.MINUTE) {
                        offset = 3;
                        len = 2;
                    } else {
                        offset = 6;
                        len = timeString.length() - offset;
                    }
                } else {
                    offset = ampm.length();
                    if (timeSel == Calendar.HOUR) {
                        offset += 1;
                        len = 2;
                    } else if (timeSel == Calendar.MINUTE) {
                        offset += 4;
                        len = 2;
                    } else {
                        len = offset;
                        offset = 0;
                    }
                }

                dX = large.substringWidth(timeString, 0, offset);
                w = large.substringWidth(timeString, offset, len);

                g.fillRect(dX, 1, w, h -1);

                g.setColor(Display.FG_H_COLOR);
                g.drawSubstring(timeString, offset, len,
                                dX, 0, Graphics.LEFT | Graphics.TOP);

                if (ampmAfterTime) {
                    if (timeSel != Calendar.HOUR) {
                        g.drawImage(ARROW_LEFT, 
                                    -1, h / 2 + 2,
                                    Graphics.RIGHT | Graphics.VCENTER);
                    }
                    if (timeSel != Calendar.AM_PM) {
                        g.drawImage(ARROW_RIGHT,
                                    large.stringWidth(timeString) + 1,
                                    h / 2 + 2,
                                    Graphics.LEFT | Graphics.VCENTER);
                    }
                } else {
                    if (timeSel != Calendar.AM_PM) {
                        g.drawImage(ARROW_LEFT,
                                    -1, h / 2 + 2,
                                    Graphics.RIGHT | Graphics.VCENTER);
                    }
                    if (timeSel != Calendar.MINUTE) {
                        g.drawImage(ARROW_RIGHT,
                                    large.stringWidth(timeString) + 1,
                                    h / 2 + 2,
                                    Graphics.LEFT | Graphics.VCENTER);
                    }
                }

                g.drawImage(ARROW_UP,
                            dX + w / 2, 0, Graphics.HCENTER | Graphics.BOTTOM);
                g.drawImage(ARROW_DOWN,
                            dX + w / 2, h + 1,
                            Graphics.HCENTER | Graphics.TOP);
            } else {
                g.drawString(":", 0, 0, Graphics.LEFT | Graphics.TOP);

                hour = calendar.get(Calendar.HOUR_OF_DAY);

                String str = hour + "";
                int w = large.stringWidth(str);
                int h = large.getBaselinePosition() + 1;

                g.translate(-1, 0);
                if (timeSel == Calendar.HOUR) {
                    g.setColor(Display.BG_H_COLOR);
                    g.fillRect(-w, 1, w + 1, h - 1);

                    g.setColor(Display.FG_H_COLOR);
                    g.drawImage(ARROW_UP, -w / 2, 0, 
                                Graphics.HCENTER | Graphics.BOTTOM);
                    g.drawImage(ARROW_DOWN,
                                -w / 2, h + 1,
                                Graphics.HCENTER | Graphics.TOP);
                } else {
                    g.setColor(Display.FG_COLOR);
                    g.drawImage(ARROW_LEFT, -(w + 1), h / 2, 
                                Graphics.RIGHT | Graphics.VCENTER);
                }

                g.drawString(str, 0, 0,
                             Graphics.RIGHT | Graphics.TOP);

                str = twoDigits(minute);
                w   = large.stringWidth(str);

                g.translate(1 + large.charWidth(':'), 0);

                if (timeSel == Calendar.MINUTE) {
                    g.setColor(Display.BG_H_COLOR);
                    g.fillRect(0, 1, w + 1, h - 1);

                    g.setColor(Display.FG_H_COLOR);
                    g.drawImage(ARROW_UP, w / 2, 0, 
                                Graphics.HCENTER | Graphics.BOTTOM);
                    g.drawImage(ARROW_DOWN, w / 2,  h + 1,
                                Graphics.HCENTER | Graphics.TOP);
                } else {
                    g.setColor(Display.FG_COLOR);
                    g.drawImage(ARROW_RIGHT, w + 2, h / 2, 
                                Graphics.LEFT | Graphics.VCENTER);

                }
                g.drawString(twoDigits(minute), 0, 0,
                             Graphics.LEFT | Graphics.TOP);
            }
        }

        /**
         * Traverse the clock
         *
         * @param action
         * @param top
         * @param bottom
         * @return int
         */
        int traverseClock(int action, int top, int bottom) {
            int hrInc = 1;

            switch (action) {
                case Canvas.LEFT:
                    if (timeSel == Calendar.MINUTE) {
                        timeSel = Calendar.HOUR;
                        return 0;
                    } else if (CLOCK_USES_AM_PM) {
                        if (timeSel == Calendar.AM_PM) {
                            timeSel = Calendar.MINUTE;
                        } else if (timeSel == Calendar.HOUR) {
                            timeSel = Calendar.AM_PM;
                        }
                        return 0;
                    }
                            return -1;

                case Canvas.RIGHT:
                    if (timeSel == Calendar.HOUR) {
                        timeSel = Calendar.MINUTE;
                        return 0;
                    } else if (CLOCK_USES_AM_PM) {
                        if (timeSel == Calendar.MINUTE) {
                            timeSel = Calendar.AM_PM;
                        } else if (timeSel == Calendar.AM_PM) {
                            timeSel = Calendar.HOUR;
                        }
                        return 0;
                    }
                    return -1;

                case Canvas.UP:
                    if (CLOCK_USES_AM_PM && (timeSel == Calendar.AM_PM)) {
                        hrInc = 12;
                    } else if (timeSel == Calendar.MINUTE) {
                        int m = calendar.get(Calendar.MINUTE);
                        if (m == 59) {
                            calendar.set(Calendar.MINUTE, 0);
                        } else {
                            calendar.set(Calendar.MINUTE, m + 1);
                            hrInc = 0;
                        }
                    }
                    calendar.set(Calendar.HOUR_OF_DAY,
                        (hrInc + calendar.get(Calendar.HOUR_OF_DAY)) % 24);
                    return 0;

                case Canvas.DOWN:
                    if (CLOCK_USES_AM_PM && (timeSel == Calendar.AM_PM)) {
                        hrInc = 12;
                    } else if (timeSel == Calendar.MINUTE) {
                        int m = calendar.get(Calendar.MINUTE);
                        if (m == 0) {
                            calendar.set(Calendar.MINUTE, 59);
                        } else {
                            calendar.set(Calendar.MINUTE, m - 1);
                            hrInc = 0;
                        }
                    }
                    hrInc = 24 - hrInc;
                    calendar.set(Calendar.HOUR_OF_DAY,
                        (hrInc + calendar.get(Calendar.HOUR_OF_DAY)) % 24);
                    return 0;
                }
            return -1;
        } // traverseClock()

        /**
         * Traverse the Date editor
         *
         * @param action
         * @param top
         * @param bottom
         * @return int
         */
        int traverseDate(int action, int top, int bottom) {
            int scrollHeight = 0;

            switch (action) {
                case Canvas.LEFT:
                    if (highlight == 1) {
                        return -1;
                    }
                    if (highlight > 1) {
                        highlight--;
                        if (calendar.get(Calendar.DAY_OF_WEEK) ==
                                Calendar.SUNDAY) {
                            int topOfHighlight = highlightY(false);
                            if (topOfHighlight < top) {
                                scrollHeight = top - topOfHighlight;
                            }
                        }
                        break;
                    }
                    int year  = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    if (highlight == 0) {
                        if (month > Calendar.JANUARY) {
                            int day = calendar.get(Calendar.DATE);
                            lastDay = daysInMonth(month - 1, year);
                            if (day > lastDay) {
                                calendar.set(Calendar.DATE, lastDay);
                            }
                            calendar.set(Calendar.MONTH, month - 1);
                        } else {
                            lastDay = 31;
                            calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                            calendar.set(Calendar.YEAR,  year - 1);
                        }
                    } else if (highlight == -1) {
                        calendar.set(Calendar.YEAR, year - 1);
                        lastDay = daysInMonth(month, year);
                    }
                    setDayOffset();
                    break;

                case Canvas.RIGHT:
                    if (highlight == lastDay) {
                        return -1;
                    }
                    if ((highlight > 0) && (highlight < lastDay)) {
                        highlight++;
                        if (calendar.get(Calendar.DAY_OF_WEEK) ==
                                Calendar.SATURDAY) {
                            int bottomOfHighlight = highlightY(true);
                            if (bottomOfHighlight > bottom) {
                                scrollHeight = bottomOfHighlight - bottom;
                            }
                        }
                        break;
                    }
                    year  = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    if (highlight == 0) {
                        if (month < Calendar.DECEMBER) {
                            int day = calendar.get(Calendar.DATE);
                            lastDay = daysInMonth(month + 1, year);
                            if (day > lastDay) {
                                calendar.set(Calendar.DATE, lastDay);
                            }
                            calendar.set(Calendar.MONTH, month + 1);
                        } else {
                            calendar.set(Calendar.MONTH, Calendar.JANUARY);
                            calendar.set(Calendar.YEAR, year + 1);
                        }
                    } else if (highlight == -1) {
                        calendar.set(Calendar.YEAR, year + 1);
                        lastDay = daysInMonth(month, year);
                    }
                    setDayOffset();
                    break;

                case Canvas.UP:
                    if (highlight == -1) {
                        return -1;
                    }
                    if (highlight == 0) {
                        highlight = -1;
                    } else if (highlight <= 7) {
                        highlight = 0;
                    } else {
                        highlight -= 7;
                    }
                    int topOfHighlight = highlightY(false);
                    if (topOfHighlight < top) {
                        scrollHeight = top - topOfHighlight;
                    }
                    break;

                case Canvas.DOWN:
                    if (highlight == lastDay) {
                        return -1;
                    }
                    if (highlight == -1) {
                        highlight = 0;
                    } else if (highlight == 0) {
                        highlight = 1;
                    } else if ((highlight + 7) <= lastDay) {
                        highlight += 7;
                    } else if ((highlight + 7) > lastDay) {
                        highlight = lastDay;
                    }
                    int bottomOfHighlight = highlightY(true);
                    if (bottomOfHighlight > bottom) {
                        scrollHeight = bottomOfHighlight - bottom;
                    }
                    break;

                default:
                    return -1;
            }

            if (highlight > 0) {
                calendar.set(Calendar.DATE, highlight);
            }
                return scrollHeight;
        } // traverseDate()

        /**
         * Utility method to calculate the number of days
         * in a month
         *
         * @param month  The month to use
         * @param year  The year the month occurs in
         * @return int  The number of days in the month
         */
        private int daysInMonth(int month, int year) {
            switch (month) {
                case Calendar.JANUARY:
                case Calendar.MARCH:
                case Calendar.MAY:
                case Calendar.JULY:
                case Calendar.AUGUST:
                case Calendar.OCTOBER:
                case Calendar.DECEMBER:
                    return 31;
                case Calendar.FEBRUARY:
                    if (((year % 400) == 0)
                        || (((year & 3) == 0) && ((year % 100) != 0))) {
                        return 29;
                    }
                    return 28;
                case Calendar.APRIL:
                case Calendar.JUNE:
                case Calendar.SEPTEMBER:
                case Calendar.NOVEMBER:
                default:
                    return 30;
            }
        }

        /**
         * The "large" Font
         */
        Font large   = Font.getFont(Font.FACE_SYSTEM,
                                    Font.STYLE_PLAIN,
                                    Font.SIZE_LARGE);

        /**
         * The "regular" font
         */
        Font regular = Font.getFont(Font.FACE_SYSTEM, 
                                    Font.STYLE_PLAIN, 
                                    Font.SIZE_SMALL);

        /**
         * The "bold" font
         */
        Font bold    = Font.getFont(Font.FACE_SYSTEM, 
                                    Font.STYLE_BOLD,
                                    Font.SIZE_SMALL);

        /**
         * Set the highlight
         *
         * @param addLineHeight
         * @return int
         */
        int highlightY(boolean addLineHeight) {
            if (highlight == -1) {
                return addLineHeight ? regular.getBaselinePosition() : 0;
            } else if (highlight == 0) {
                return regular.getBaselinePosition()
                    + (addLineHeight ? regular.getHeight() : 0);
            } else {
                int line = 1 + (highlight + dayOffset - 2) / 7;
                return (regular.getBaselinePosition() + 1) * line +
                       regular.getHeight() +
                       (addLineHeight ? regular.getBaselinePosition() : 0);
            }
        }

        /**
         * Paint the Calendar
         *
         * @param g The Graphics context to paint to
         */
        void paintCalendar(Graphics g) {

            g.setColor(Display.ERASE_COLOR);
            g.fillRect(g.getClipX(), g.getClipY(), 
                       g.getClipWidth(), g.getClipHeight());

            boolean currentDateOnDisplay = false;
            int year      = calendar.get(Calendar.YEAR);
            int month     = calendar.get(Calendar.MONTH);
            int selection = calendar.get(Calendar.DATE);

            // check if currentDate is on display
            int currYear  = currentDate.get(Calendar.YEAR);
            int currMonth = currentDate.get(Calendar.MONTH);
            int currDate  = currentDate.get(Calendar.DATE);
            if ((currYear == year) && (currMonth == month)) {
                currentDateOnDisplay = true;
            }

            g.setFont(regular);
            int w = regular.stringWidth("0000");
            int h = regular.getBaselinePosition();
            g.translate(0, -1);

            if (highlight == -1) {
                g.setColor(Display.BG_H_COLOR);
                g.fillRect(((width - w) / 2) - 1, 1, w + 1, h);
                g.setColor(Display.FG_H_COLOR);
            } else {
                g.setColor(Display.FG_COLOR);
            }

            g.drawString("" + year, width / 2, 0,
                         Graphics.TOP | Graphics.HCENTER);
            g.drawImage(ARROW_LEFT, (width - w) / 2 - 2, h / 2,
                        Graphics.VCENTER | Graphics.RIGHT);
            g.drawImage(ARROW_RIGHT, (width + w) / 2 + 2, h / 2,
                        Graphics.VCENTER | Graphics.LEFT);

            g.translate(0, h + 1);
            w = regular.stringWidth(Resource.getString(MONTH_NAMES[month]));
            h = regular.getHeight();

            if (highlight == 0) {
                g.setColor(Display.BG_H_COLOR);
                g.fillRect(((width - w) / 2) - 1, 1, w + 2, h);
                g.setColor(Display.FG_H_COLOR);
            } else {
                g.setColor(Display.FG_COLOR);
            }

            g.setFont(regular);
            g.drawString(Resource.getString(MONTH_NAMES[month]), width / 2, 0,
                         Graphics.TOP | Graphics.HCENTER);
            g.drawImage(ARROW_LEFT, (width - w) / 2 - 2, h / 2,
                        Graphics.VCENTER | Graphics.RIGHT);
            g.drawImage(ARROW_RIGHT, (width + w) / 2 + 2, h / 2,
                        Graphics.VCENTER | Graphics.LEFT);

            g.translate(0, h);

            int o = width / 14;
            int rem = width % 14 / 2;
            int x = o * (dayOffset * 2 - 1) + rem;
            int y = 0;
            int lastCol = 14 * o;
            h = regular.getBaselinePosition() + 1;
            
            for (int i = 1; i <= lastDay; ++i) {
                String str = "" + i;
                if (i == highlight) {
                    w = regular.stringWidth(str);

                    g.setColor(Display.BG_H_COLOR);
                    g.fillRect(x - w / 2 - 1, y + 1, w + 1, h - 1);
                    g.setColor(Display.FG_H_COLOR);
                } else {
                    g.setColor(Display.FG_COLOR);
                }

                g.setFont(((currentDateOnDisplay) &&
                        (i == currDate) &&
                        (i != highlight)) ? bold : regular);
                g.drawString(str, x, y, Graphics.TOP | Graphics.HCENTER);
                x += 2 * o;
                if (x > lastCol) {
                    x = o + rem;
                    y += h;
                }
            }
        }

        /**
         * Set the day offset
         */
        private void setDayOffset() {
            Date save = calendar.getTime();
            calendar.set(Calendar.DATE, 1);
            dayOffset = calendar.get(Calendar.DAY_OF_WEEK);

            if (Resource.getFirstDayOfWeek() != Calendar.SUNDAY) {
                dayOffset = (dayOffset == 1) ? 7 : (dayOffset - 1);
            }
            calendar.setTime(save);
        }

        /**
         * Determine if this editor is the edit screen for the
         * given Displayable.
         *
         * @param d The Displayable to check
         * @return boolean True if the Displayable is equal to this editor's
         *                  return screen (ie, the screen containing the
         *                  originating field being edited)
         */
        boolean isEditScreen(Displayable d) {
          return d == returnScreen;
        }

    } // EditScreen
}
