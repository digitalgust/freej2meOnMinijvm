package java.awt.event;

import java.awt.*;

public class MouseEvent {

    /**
     * The first number in the range of ids used for mouse events.
     */
    public static final int MOUSE_FIRST = 500;

    /**
     * The last number in the range of ids used for mouse events.
     */
    public static final int MOUSE_LAST = 507;

    /**
     * The "mouse clicked" event. This <code>MouseEvent</code>
     * occurs when a mouse button is pressed and released.
     */
    public static final int MOUSE_CLICKED = MOUSE_FIRST;

    /**
     * The "mouse pressed" event. This <code>MouseEvent</code>
     * occurs when a mouse button is pushed down.
     */
    public static final int MOUSE_PRESSED = 1 + MOUSE_FIRST; //Event.MOUSE_DOWN

    /**
     * The "mouse released" event. This <code>MouseEvent</code>
     * occurs when a mouse button is let up.
     */
    public static final int MOUSE_RELEASED = 2 + MOUSE_FIRST; //Event.MOUSE_UP

    /**
     * The "mouse moved" event. This <code>MouseEvent</code>
     * occurs when the mouse position changes.
     */
    public static final int MOUSE_MOVED = 3 + MOUSE_FIRST; //Event.MOUSE_MOVE

    /**
     * The "mouse entered" event. This <code>MouseEvent</code>
     * occurs when the mouse cursor enters the unobscured part of component's
     * geometry.
     */
    public static final int MOUSE_ENTERED = 4 + MOUSE_FIRST; //Event.MOUSE_ENTER

    /**
     * The "mouse exited" event. This <code>MouseEvent</code>
     * occurs when the mouse cursor exits the unobscured part of component's
     * geometry.
     */
    public static final int MOUSE_EXITED = 5 + MOUSE_FIRST; //Event.MOUSE_EXIT

    /**
     * The "mouse dragged" event. This <code>MouseEvent</code>
     * occurs when the mouse position changes while a mouse button is pressed.
     */
    public static final int MOUSE_DRAGGED = 6 + MOUSE_FIRST; //Event.MOUSE_DRAG

    /**
     * The "mouse wheel" event.  This is the only <code>MouseWheelEvent</code>.
     * It occurs when a mouse equipped with a wheel has its wheel rotated.
     *
     * @since 1.4
     */
    public static final int MOUSE_WHEEL = 7 + MOUSE_FIRST;

    /**
     * Indicates no mouse buttons; used by {@link #getButton}.
     *
     * @since 1.4
     */
    public static final int NOBUTTON = 0;

    public static final int BUTTON1 = 1;

    public static final int BUTTON2 = 2;

    public static final int BUTTON3 = 3;

    int x;

    int y;

    int xAbs, yAbs;
    int clickCount;

    int button;
    boolean popupTrigger;


    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getButton() {
        return button;
    }

    public int getClickCount() {
        return clickCount;
    }

    public int getXOnScreen() {
        return xAbs;
    }

    public int getYOnScreen() {
        return yAbs;
    }

    public MouseEvent(Component source, int id, long when, int modifiers,
                      int x, int y, int clickCount, boolean popupTrigger,
                      int button) {
        this(source, id, when, modifiers, x, y, 0, 0, clickCount, popupTrigger, button);
        Point eventLocationOnScreen = new Point(0, 0);
        try {
            this.xAbs = eventLocationOnScreen.x + x;
            this.yAbs = eventLocationOnScreen.y + y;
        } catch (IllegalComponentStateException e) {
            this.xAbs = 0;
            this.yAbs = 0;
        }
    }

    public MouseEvent(Component source, int id, long when, int modifiers,
                      int x, int y, int clickCount, boolean popupTrigger) {
        this(source, id, when, modifiers, x, y, clickCount, popupTrigger, NOBUTTON);
    }

    public MouseEvent(Component source, int id, long when, int modifiers,
                      int x, int y, int xAbs, int yAbs,
                      int clickCount, boolean popupTrigger, int button) {
        this.x = x;
        this.y = y;
        this.xAbs = xAbs;
        this.yAbs = yAbs;
        this.clickCount = clickCount;
        this.popupTrigger = popupTrigger;
        if (button < NOBUTTON) {
            throw new IllegalArgumentException("Invalid button value :" + button);
        }
        this.button = button;
    }

}
