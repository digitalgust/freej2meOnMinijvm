package java.awt;

import java.awt.event.ActionEvent;
import java.util.EventObject;

public class AWTEvent extends EventObject {

    /**
     * The event mask for selecting component events.
     */
    public final static long COMPONENT_EVENT_MASK = 0x01;

    /**
     * The event mask for selecting container events.
     */
    public final static long CONTAINER_EVENT_MASK = 0x02;

    /**
     * The event mask for selecting focus events.
     */
    public final static long FOCUS_EVENT_MASK = 0x04;

    /**
     * The event mask for selecting key events.
     */
    public final static long KEY_EVENT_MASK = 0x08;

    /**
     * The event mask for selecting mouse events.
     */
    public final static long MOUSE_EVENT_MASK = 0x10;

    /**
     * The event mask for selecting mouse motion events.
     */
    public final static long MOUSE_MOTION_EVENT_MASK = 0x20;

    /**
     * The event mask for selecting window events.
     */
    public final static long WINDOW_EVENT_MASK = 0x40;

    /**
     * The event mask for selecting action events.
     */
    public final static long ACTION_EVENT_MASK = 0x80;

    /**
     * The event mask for selecting adjustment events.
     */
    public final static long ADJUSTMENT_EVENT_MASK = 0x100;

    /**
     * The event mask for selecting item events.
     */
    public final static long ITEM_EVENT_MASK = 0x200;

    /**
     * The event mask for selecting text events.
     */
    public final static long TEXT_EVENT_MASK = 0x400;

    /**
     * The event mask for selecting input method events.
     */
    public final static long INPUT_METHOD_EVENT_MASK = 0x800;

    /**
     * The pseudo event mask for enabling input methods.
     * We're using one bit in the eventMask so we don't need
     * a separate field inputMethodsEnabled.
     */
    final static long INPUT_METHODS_ENABLED_MASK = 0x1000;

    /**
     * The event mask for selecting paint events.
     */
    public final static long PAINT_EVENT_MASK = 0x2000;

    /**
     * The event mask for selecting invocation events.
     */
    public final static long INVOCATION_EVENT_MASK = 0x4000;

    /**
     * The event mask for selecting hierarchy events.
     */
    public final static long HIERARCHY_EVENT_MASK = 0x8000;

    /**
     * The event mask for selecting hierarchy bounds events.
     */
    public final static long HIERARCHY_BOUNDS_EVENT_MASK = 0x10000;

    /**
     * The event mask for selecting mouse wheel events.
     *
     * @since 1.4
     */
    public final static long MOUSE_WHEEL_EVENT_MASK = 0x20000;

    /**
     * The event mask for selecting window state events.
     *
     * @since 1.4
     */
    public final static long WINDOW_STATE_EVENT_MASK = 0x40000;

    /**
     * The event mask for selecting window focus events.
     *
     * @since 1.4
     */
    public final static long WINDOW_FOCUS_EVENT_MASK = 0x80000;

    /**
     * WARNING: there are more mask defined privately.  See
     * SunToolkit.GRAB_EVENT_MASK.
     */

    /**
     * The maximum value for reserved AWT event IDs. Programs defining
     * their own event IDs should use IDs greater than this value.
     */
    public final static int RESERVED_ID_MAX = 1999;

    static int initedId = (int) (Math.random() * Integer.MAX_VALUE);

    protected void initIDs() {
        initedId++;
    }

    protected int id;

    public AWTEvent(Object source, int id) {
        super(source);
        this.id = id;
        switch (id) {
            case ActionEvent.ACTION_PERFORMED:
                break;
            default:
        }
    }

    public int getID() {
        return id;
    }
}
