package java.awt.event;

public class Event implements java.io.Serializable {

    /**
     * This flag indicates that the Shift key was down when the event
     * occurred.
     */
    public static final int SHIFT_MASK = 1 << 0;

    /**
     * This flag indicates that the Control key was down when the event
     * occurred.
     */
    public static final int CTRL_MASK = 1 << 1;

    /**
     * This flag indicates that the Meta key was down when the event
     * occurred. For mouse events, this flag indicates that the right
     * button was pressed or released.
     */
    public static final int META_MASK = 1 << 2;

    /**
     * This flag indicates that the Alt key was down when
     * the event occurred. For mouse events, this flag indicates that the
     * middle mouse button was pressed or released.
     */
    public static final int ALT_MASK = 1 << 3;

    /* Action keys */

    /**
     * The Home key, a non-ASCII action key.
     */
    public static final int HOME = 1000;

    /**
     * The End key, a non-ASCII action key.
     */
    public static final int END = 1001;

    /**
     * The Page Up key, a non-ASCII action key.
     */
    public static final int PGUP = 1002;

    /**
     * The Page Down key, a non-ASCII action key.
     */
    public static final int PGDN = 1003;

    /**
     * The Up Arrow key, a non-ASCII action key.
     */
    public static final int UP = 1004;

    /**
     * The Down Arrow key, a non-ASCII action key.
     */
    public static final int DOWN = 1005;

    /**
     * The Left Arrow key, a non-ASCII action key.
     */
    public static final int LEFT = 1006;

    /**
     * The Right Arrow key, a non-ASCII action key.
     */
    public static final int RIGHT = 1007;

    /**
     * The F1 function key, a non-ASCII action key.
     */
    public static final int F1 = 1008;

    /**
     * The F2 function key, a non-ASCII action key.
     */
    public static final int F2 = 1009;

    /**
     * The F3 function key, a non-ASCII action key.
     */
    public static final int F3 = 1010;

    /**
     * The F4 function key, a non-ASCII action key.
     */
    public static final int F4 = 1011;

    /**
     * The F5 function key, a non-ASCII action key.
     */
    public static final int F5 = 1012;

    /**
     * The F6 function key, a non-ASCII action key.
     */
    public static final int F6 = 1013;

    /**
     * The F7 function key, a non-ASCII action key.
     */
    public static final int F7 = 1014;

    /**
     * The F8 function key, a non-ASCII action key.
     */
    public static final int F8 = 1015;

    /**
     * The F9 function key, a non-ASCII action key.
     */
    public static final int F9 = 1016;

    /**
     * The F10 function key, a non-ASCII action key.
     */
    public static final int F10 = 1017;

    /**
     * The F11 function key, a non-ASCII action key.
     */
    public static final int F11 = 1018;

    /**
     * The F12 function key, a non-ASCII action key.
     */
    public static final int F12 = 1019;

    /**
     * The Print Screen key, a non-ASCII action key.
     */
    public static final int PRINT_SCREEN = 1020;

    /**
     * The Scroll Lock key, a non-ASCII action key.
     */
    public static final int SCROLL_LOCK = 1021;

    /**
     * The Caps Lock key, a non-ASCII action key.
     */
    public static final int CAPS_LOCK = 1022;

    /**
     * The Num Lock key, a non-ASCII action key.
     */
    public static final int NUM_LOCK = 1023;

    /**
     * The Pause key, a non-ASCII action key.
     */
    public static final int PAUSE = 1024;

    /**
     * The Insert key, a non-ASCII action key.
     */
    public static final int INSERT = 1025;

    /* Non-action keys */

    /**
     * The Enter key.
     */
    public static final int ENTER = '\n';

    /**
     * The BackSpace key.
     */
    public static final int BACK_SPACE = '\b';

    /**
     * The Tab key.
     */
    public static final int TAB = '\t';

    /**
     * The Escape key.
     */
    public static final int ESCAPE = 27;

    /**
     * The Delete key.
     */
    public static final int DELETE = 127;


    /* Base for all window events. */
    private static final int WINDOW_EVENT = 200;

    /**
     * The user has asked the window manager to kill the window.
     */
    public static final int WINDOW_DESTROY = 1 + WINDOW_EVENT;

    /**
     * The user has asked the window manager to expose the window.
     */
    public static final int WINDOW_EXPOSE = 2 + WINDOW_EVENT;

    /**
     * The user has asked the window manager to iconify the window.
     */
    public static final int WINDOW_ICONIFY = 3 + WINDOW_EVENT;

    /**
     * The user has asked the window manager to de-iconify the window.
     */
    public static final int WINDOW_DEICONIFY = 4 + WINDOW_EVENT;

    /**
     * The user has asked the window manager to move the window.
     */
    public static final int WINDOW_MOVED = 5 + WINDOW_EVENT;

    /* Base for all keyboard events. */
    private static final int KEY_EVENT = 400;

    /**
     * The user has pressed a normal key.
     */
    public static final int KEY_PRESS = 1 + KEY_EVENT;

    /**
     * The user has released a normal key.
     */
    public static final int KEY_RELEASE = 2 + KEY_EVENT;

    /**
     * The user has pressed a non-ASCII <em>action</em> key.
     * The <code>key</code> field contains a value that indicates
     * that the event occurred on one of the action keys, which
     * comprise the 12 function keys, the arrow (cursor) keys,
     * Page Up, Page Down, Home, End, Print Screen, Scroll Lock,
     * Caps Lock, Num Lock, Pause, and Insert.
     */
    public static final int KEY_ACTION = 3 + KEY_EVENT;

    /**
     * The user has released a non-ASCII <em>action</em> key.
     * The <code>key</code> field contains a value that indicates
     * that the event occurred on one of the action keys, which
     * comprise the 12 function keys, the arrow (cursor) keys,
     * Page Up, Page Down, Home, End, Print Screen, Scroll Lock,
     * Caps Lock, Num Lock, Pause, and Insert.
     */
    public static final int KEY_ACTION_RELEASE = 4 + KEY_EVENT;

    /* Base for all mouse events. */
    private static final int MOUSE_EVENT = 500;

    /**
     * The user has pressed the mouse button. The <code>ALT_MASK</code>
     * flag indicates that the middle button has been pressed.
     * The <code>META_MASK</code>flag indicates that the
     * right button has been pressed.
     *
     * @see java.awt.Event#ALT_MASK
     * @see java.awt.Event#META_MASK
     */
    public static final int MOUSE_DOWN = 1 + MOUSE_EVENT;

    /**
     * The user has released the mouse button. The <code>ALT_MASK</code>
     * flag indicates that the middle button has been released.
     * The <code>META_MASK</code>flag indicates that the
     * right button has been released.
     *
     * @see java.awt.Event#ALT_MASK
     * @see java.awt.Event#META_MASK
     */
    public static final int MOUSE_UP = 2 + MOUSE_EVENT;

    /**
     * The mouse has moved with no button pressed.
     */
    public static final int MOUSE_MOVE = 3 + MOUSE_EVENT;

    /**
     * The mouse has entered a component.
     */
    public static final int MOUSE_ENTER = 4 + MOUSE_EVENT;

    /**
     * The mouse has exited a component.
     */
    public static final int MOUSE_EXIT = 5 + MOUSE_EVENT;

    /**
     * The user has moved the mouse with a button pressed. The
     * <code>ALT_MASK</code> flag indicates that the middle
     * button is being pressed. The <code>META_MASK</code> flag indicates
     * that the right button is being pressed.
     *
     * @see java.awt.Event#ALT_MASK
     * @see java.awt.Event#META_MASK
     */
    public static final int MOUSE_DRAG = 6 + MOUSE_EVENT;


    /* Scrolling events */
    private static final int SCROLL_EVENT = 600;

    /**
     * The user has activated the <em>line up</em>
     * area of a scroll bar.
     */
    public static final int SCROLL_LINE_UP = 1 + SCROLL_EVENT;

    /**
     * The user has activated the <em>line down</em>
     * area of a scroll bar.
     */
    public static final int SCROLL_LINE_DOWN = 2 + SCROLL_EVENT;

    /**
     * The user has activated the <em>page up</em>
     * area of a scroll bar.
     */
    public static final int SCROLL_PAGE_UP = 3 + SCROLL_EVENT;

    /**
     * The user has activated the <em>page down</em>
     * area of a scroll bar.
     */
    public static final int SCROLL_PAGE_DOWN = 4 + SCROLL_EVENT;

    /**
     * The user has moved the bubble (thumb) in a scroll bar,
     * moving to an "absolute" position, rather than to
     * an offset from the last postion.
     */
    public static final int SCROLL_ABSOLUTE = 5 + SCROLL_EVENT;

    /**
     * The scroll begin event.
     */
    public static final int SCROLL_BEGIN = 6 + SCROLL_EVENT;

    /**
     * The scroll end event.
     */
    public static final int SCROLL_END = 7 + SCROLL_EVENT;

    /* List Events */
    private static final int LIST_EVENT = 700;

    /**
     * An item in a list has been selected.
     */
    public static final int LIST_SELECT = 1 + LIST_EVENT;

    /**
     * An item in a list has been deselected.
     */
    public static final int LIST_DESELECT = 2 + LIST_EVENT;

    /* Misc Event */
    private static final int MISC_EVENT = 1000;

    /**
     * This event indicates that the user wants some action to occur.
     */
    public static final int ACTION_EVENT = 1 + MISC_EVENT;

    /**
     * A file loading event.
     */
    public static final int LOAD_FILE = 2 + MISC_EVENT;

    /**
     * A file saving event.
     */
    public static final int SAVE_FILE = 3 + MISC_EVENT;

    /**
     * A component gained the focus.
     */
    public static final int GOT_FOCUS = 4 + MISC_EVENT;

    /**
     * A component lost the focus.
     */
    public static final int LOST_FOCUS = 5 + MISC_EVENT;

    /**
     * The target component. This indicates the component over which the
     * event occurred or with which the event is associated.
     * This object has been replaced by AWTEvent.getSource()
     *
     * @serial
     * @see java.awt.AWTEvent#getSource()
     */
    public Object target;

    /**
     * The time stamp.
     * Replaced by InputEvent.getWhen().
     *
     * @serial
     * @see java.awt.event.InputEvent#getWhen()
     */
    public long when;

    /**
     * Indicates which type of event the event is, and which
     * other <code>Event</code> variables are relevant for the event.
     * This has been replaced by AWTEvent.getID()
     *
     * @serial
     * @see java.awt.AWTEvent#getID()
     */
    public int id;

    /**
     * The <i>x</i> coordinate of the event.
     * Replaced by MouseEvent.getX()
     *
     * @serial
     * @see java.awt.event.MouseEvent#getX()
     */
    public int x;

    /**
     * The <i>y</i> coordinate of the event.
     * Replaced by MouseEvent.getY()
     *
     * @serial
     * @see java.awt.event.MouseEvent#getY()
     */
    public int y;

    /**
     * The key code of the key that was pressed in a keyboard event.
     * This has been replaced by KeyEvent.getKeyCode()
     *
     * @serial
     * @see java.awt.event.KeyEvent#getKeyCode()
     */
    public int key;

    /**
     * The key character that was pressed in a keyboard event.
     */
//    public char keyChar;

    /**
     * The state of the modifier keys.
     * This is replaced with InputEvent.getModifiers()
     * In java 1.1 MouseEvent and KeyEvent are subclasses
     * of InputEvent.
     *
     * @serial
     * @see java.awt.event.InputEvent#getModifiers()
     */
    public int modifiers;

    /**
     * For <code>MOUSE_DOWN</code> events, this field indicates the
     * number of consecutive clicks. For other events, its value is
     * <code>0</code>.
     * This field has been replaced by MouseEvent.getClickCount().
     *
     * @serial
     * @see java.awt.event.MouseEvent#getClickCount().
     */
    public int clickCount;

    /**
     * An arbitrary argument of the event. The value of this field
     * depends on the type of event.
     * <code>arg</code> has been replaced by event specific property.
     *
     * @serial
     */
    public Object arg;

    /**
     * The next event. This field is set when putting events into a
     * linked list.
     * This has been replaced by EventQueue.
     *
     * @serial
     * @see java.awt.EventQueue
     */
    public Event evt;

    /* table for mapping old Event action keys to KeyEvent virtual keys. */
    private static final int actionKeyCodes[][] = {
            /*    virtual key              action key   */
            {KeyEvent.VK_HOME, Event.HOME},
            {KeyEvent.VK_END, Event.END},
            {KeyEvent.VK_PAGE_UP, Event.PGUP},
            {KeyEvent.VK_PAGE_DOWN, Event.PGDN},
            {KeyEvent.VK_UP, Event.UP},
            {KeyEvent.VK_DOWN, Event.DOWN},
            {KeyEvent.VK_LEFT, Event.LEFT},
            {KeyEvent.VK_RIGHT, Event.RIGHT},
            {KeyEvent.VK_F1, Event.F1},
            {KeyEvent.VK_F2, Event.F2},
            {KeyEvent.VK_F3, Event.F3},
            {KeyEvent.VK_F4, Event.F4},
            {KeyEvent.VK_F5, Event.F5},
            {KeyEvent.VK_F6, Event.F6},
            {KeyEvent.VK_F7, Event.F7},
            {KeyEvent.VK_F8, Event.F8},
            {KeyEvent.VK_F9, Event.F9},
            {KeyEvent.VK_F10, Event.F10},
            {KeyEvent.VK_F11, Event.F11},
            {KeyEvent.VK_F12, Event.F12},
            {KeyEvent.VK_PRINTSCREEN, Event.PRINT_SCREEN},
            {KeyEvent.VK_SCROLL_LOCK, Event.SCROLL_LOCK},
            {KeyEvent.VK_CAPS_LOCK, Event.CAPS_LOCK},
            {KeyEvent.VK_NUM_LOCK, Event.NUM_LOCK},
            {KeyEvent.VK_PAUSE, Event.PAUSE},
            {KeyEvent.VK_INSERT, Event.INSERT}
    };

    private boolean consumed = false;


    static {
    }


    public Event(Object target, long when, int id, int x, int y, int key,
                 int modifiers, Object arg) {
        this.target = target;
        this.when = when;
        this.id = id;
        this.x = x;
        this.y = y;
        this.key = key;
        this.modifiers = modifiers;
        this.arg = arg;
        this.clickCount = 0;
        switch (id) {
            case ACTION_EVENT:
            case WINDOW_DESTROY:
            case WINDOW_ICONIFY:
            case WINDOW_DEICONIFY:
            case WINDOW_MOVED:
            case SCROLL_LINE_UP:
            case SCROLL_LINE_DOWN:
            case SCROLL_PAGE_UP:
            case SCROLL_PAGE_DOWN:
            case SCROLL_ABSOLUTE:
            case SCROLL_BEGIN:
            case SCROLL_END:
            case LIST_SELECT:
            case LIST_DESELECT:
                consumed = true; // these types are not passed back to peer
                break;
            default:
        }
    }

    public Event(Object target, long when, int id, int x, int y, int key, int modifiers) {
        this(target, when, id, x, y, key, modifiers, null);
    }

    public Event(Object target, int id, Object arg) {
        this(target, 0, id, 0, 0, 0, 0, arg);
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public boolean shiftDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }

    public boolean controlDown() {
        return (modifiers & CTRL_MASK) != 0;
    }

    public boolean metaDown() {
        return (modifiers & META_MASK) != 0;
    }

    void consume() {
        switch (id) {
            case KEY_PRESS:
            case KEY_RELEASE:
            case KEY_ACTION:
            case KEY_ACTION_RELEASE:
                consumed = true;
                break;
            default:
                // event type cannot be consumed
        }
    }

    boolean isConsumed() {
        return consumed;
    }

    static int getOldEventKey(KeyEvent e) {
        int keyCode = e.getKeyCode();
        for (int i = 0; i < actionKeyCodes.length; i++) {
            if (actionKeyCodes[i][0] == keyCode) {
                return actionKeyCodes[i][1];
            }
        }
        return (int) e.getKeyChar();
    }

    char getKeyEventChar() {
        for (int i = 0; i < actionKeyCodes.length; i++) {
            if (actionKeyCodes[i][1] == key) {
                return KeyEvent.CHAR_UNDEFINED;
            }
        }
        return (char) key;
    }

    protected String paramString() {
        String str = "id=" + id + ",x=" + x + ",y=" + y;
        if (key != 0) {
            str += ",key=" + key;
        }
        if (shiftDown()) {
            str += ",shift";
        }
        if (controlDown()) {
            str += ",control";
        }
        if (metaDown()) {
            str += ",meta";
        }
        if (target != null) {
            str += ",target=" + target;
        }
        if (arg != null) {
            str += ",arg=" + arg;
        }
        return str;
    }

    public String toString() {
        return getClass().getName() + "[" + paramString() + "]";
    }
}
