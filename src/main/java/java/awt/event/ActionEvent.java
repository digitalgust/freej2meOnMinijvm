package java.awt.event;

import java.awt.AWTEvent;
import java.lang.annotation.Native;

public class ActionEvent extends AWTEvent {

    /**
     * The first number in the range of ids used for action events.
     */
    public static final int ACTION_FIRST                = 1001;

    /**
     * The last number in the range of ids used for action events.
     */
    public static final int ACTION_LAST                 = 1001;

    /**
     * This event id indicates that a meaningful action occurred.
     */
    @Native
    public static final int ACTION_PERFORMED    = ACTION_FIRST; //Event.ACTION_EVENT

    public ActionEvent(Object source, int id, String command) {
        super(source, id);
    }
}
