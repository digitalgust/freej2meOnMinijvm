package java.awt.event;

import java.awt.*;

public abstract class InputEvent extends ComponentEvent {

    /**
     * The Shift key modifier constant.
     * It is recommended that SHIFT_DOWN_MASK be used instead.
     */
    public static final int SHIFT_MASK = Event.SHIFT_MASK;

    /**
     * The Control key modifier constant.
     * It is recommended that CTRL_DOWN_MASK be used instead.
     */
    public static final int CTRL_MASK = Event.CTRL_MASK;

    /**
     * The Meta key modifier constant.
     * It is recommended that META_DOWN_MASK be used instead.
     */
    public static final int META_MASK = Event.META_MASK;

    /**
     * The Alt key modifier constant.
     * It is recommended that ALT_DOWN_MASK be used instead.
     */
    public static final int ALT_MASK = Event.ALT_MASK;

    /**
     * The AltGraph key modifier constant.
     */
    public static final int ALT_GRAPH_MASK = 1 << 5;

    /**
     * The Mouse Button1 modifier constant.
     * It is recommended that BUTTON1_DOWN_MASK be used instead.
     */
    public static final int BUTTON1_MASK = 1 << 4;

    /**
     * The Mouse Button2 modifier constant.
     * It is recommended that BUTTON2_DOWN_MASK be used instead.
     * Note that BUTTON2_MASK has the same value as ALT_MASK.
     */
    public static final int BUTTON2_MASK = Event.ALT_MASK;

    /**
     * The Mouse Button3 modifier constant.
     * It is recommended that BUTTON3_DOWN_MASK be used instead.
     * Note that BUTTON3_MASK has the same value as META_MASK.
     */
    public static final int BUTTON3_MASK = Event.META_MASK;

    /**
     * The Shift key extended modifier constant.
     *
     * @since 1.4
     */
    public static final int SHIFT_DOWN_MASK = 1 << 6;

    /**
     * The Control key extended modifier constant.
     *
     * @since 1.4
     */
    public static final int CTRL_DOWN_MASK = 1 << 7;

    /**
     * The Meta key extended modifier constant.
     *
     * @since 1.4
     */
    public static final int META_DOWN_MASK = 1 << 8;

    /**
     * The Alt key extended modifier constant.
     *
     * @since 1.4
     */
    public static final int ALT_DOWN_MASK = 1 << 9;

    /**
     * The Mouse Button1 extended modifier constant.
     *
     * @since 1.4
     */
    public static final int BUTTON1_DOWN_MASK = 1 << 10;

    /**
     * The Mouse Button2 extended modifier constant.
     *
     * @since 1.4
     */
    public static final int BUTTON2_DOWN_MASK = 1 << 11;

    /**
     * The Mouse Button3 extended modifier constant.
     *
     * @since 1.4
     */
    public static final int BUTTON3_DOWN_MASK = 1 << 12;

    /**
     * The AltGraph key extended modifier constant.
     *
     * @since 1.4
     */
    public static final int ALT_GRAPH_DOWN_MASK = 1 << 13;


    static final int MODIFIERS = SHIFT_DOWN_MASK - 1;

    /**
     * The input event's Time stamp in UTC format.  The time stamp
     * indicates when the input event was created.
     *
     * @serial
     * @see #getWhen()
     */
    long when;

    /**
     * The state of the modifier mask at the time the input
     * event was fired.
     *
     * @serial
     * @see #getModifiers()
     * @see #getModifiersEx()
     * @see java.awt.event.KeyEvent
     * @see java.awt.event.MouseEvent
     */
    int modifiers;


    boolean consumed;


    InputEvent(Component source, int id, long when, int modifiers) {
        super(source, id);
        this.when = when;
        this.modifiers = modifiers;
    }


    public boolean isShiftDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }

    public boolean isControlDown() {
        return (modifiers & CTRL_MASK) != 0;
    }

    public boolean isMetaDown() {
        return (modifiers & META_MASK) != 0;
    }

    public boolean isAltDown() {
        return (modifiers & ALT_MASK) != 0;
    }

    public boolean isAltGraphDown() {
        return (modifiers & ALT_GRAPH_MASK) != 0;
    }

    public long getWhen() {
        return when;
    }

    public int getModifiers() {
        return modifiers & MODIFIERS;
    }

    public int getModifiersEx() {
        return modifiers & ~MODIFIERS;
    }

    public void consume() {
        consumed = true;
    }

    public boolean isConsumed() {
        return consumed;
    }


    public static String getModifiersExText(int modifiers) {
        StringBuffer buf = new StringBuffer();
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.meta", "Meta"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.control", "Ctrl"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.alt", "Alt"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.shift", "Shift"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.altGraph", "Alt Graph"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.button1", "Button1"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.button2", "Button2"));
            buf.append("+");
        }
        if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0) {
            buf.append(Toolkit.getProperty("AWT.button3", "Button3"));
            buf.append("+");
        }
        if (buf.length() > 0) {
            buf.setLength(buf.length() - 1); // remove trailing '+'
        }
        return buf.toString();
    }
}
